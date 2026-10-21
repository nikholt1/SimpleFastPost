export function ansiToHtml(input) {
    const ANSI_REGEX = /\x1b\[((?:\d+;?)+)m/g;

    const colors = {
        30: "black",
        31: "red",
        32: "green",
        33: "yellow",
        34: "blue",
        35: "magenta",
        36: "cyan",
        37: "white",
        90: "gray",
        91: "lightcoral",
        92: "lightgreen",
        93: "lightyellow",
        94: "lightblue",
        95: "violet",
        96: "lightcyan",
        97: "white"
    };

    const bgColors = {
        40: "black",
        41: "red",
        42: "green",
        43: "yellow",
        44: "blue",
        45: "magenta",
        46: "cyan",
        47: "white",
        100: "gray",
        101: "lightcoral",
        102: "lightgreen",
        103: "lightyellow",
        104: "lightblue",
        105: "violet",
        106: "lightcyan",
        107: "white"
    };

    function ansi256(n) {
        if (n < 16) {
            const basic = [
                "#000000", "#800000", "#008000", "#808000",
                "#000080", "#800080", "#008080", "#c0c0c0",
                "#808080", "#ff0000", "#00ff00", "#ffff00",
                "#0000ff", "#ff00ff", "#00ffff", "#ffffff"
            ];
            return basic[n] || "#ffffff";
        }

        if (n >= 16 && n <= 231) {
            const c = n - 16;
            const r = Math.floor(c / 36);
            const g = Math.floor((c % 36) / 6);
            const b = c % 6;

            const toHex = v => v === 0 ? 0 : v * 40 + 55;
            return `rgb(${toHex(r)}, ${toHex(g)}, ${toHex(b)})`;
        }

        const gray = (n - 232) * 10 + 8;
        return `rgb(${gray},${gray},${gray})`;
    }

    let result = "";
    let lastIndex = 0;

    let openSpan = false;

    const closeSpan = () => {
        if (openSpan) {
            result += "</span>";
            openSpan = false;
        }
    };

    let match;

    while ((match = ANSI_REGEX.exec(input)) !== null) {
        const chunk = input.substring(lastIndex, match.index);
        result += chunk;

        const codes = match[1].split(";").map(Number);

        for (let i = 0; i < codes.length; i++) {
            const code = codes[i];

            // RESET
            if (code === 0) {
                closeSpan();
                continue;
            }

            // BOLD
            if (code === 1) {
                result += `<span style="font-weight:bold;">`;
                openSpan = true;
                continue;
            }

            // 256 FG: 38;5;n
            if (code === 38 && codes[i + 1] === 5) {
                const n = codes[i + 2];
                i += 2;

                closeSpan();
                result += `<span style="color:${ansi256(n)}">`;
                openSpan = true;
                continue;
            }

            // 256 BG: 48;5;n
            if (code === 48 && codes[i + 1] === 5) {
                const n = codes[i + 2];
                i += 2;

                closeSpan();
                result += `<span style="background:${ansi256(n)}">`;
                openSpan = true;
                continue;
            }

            // BASIC FG
            if (colors[code]) {
                closeSpan();
                result += `<span style="color:${colors[code]}">`;
                openSpan = true;
                continue;
            }

            // BASIC BG
            if (bgColors[code]) {
                closeSpan();
                result += `<span style="background:${bgColors[code]}">`;
                openSpan = true;
                continue;
            }
        }

        lastIndex = ANSI_REGEX.lastIndex;
    }

    result += input.substring(lastIndex);
    closeSpan();

    return result;
}