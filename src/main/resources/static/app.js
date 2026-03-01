const sidebar = document.getElementById("sidebar");
const resizer = document.getElementById("resizer");
const toggleBtn = document.getElementById("toggleSidebar");

let isResizing = false;

resizer.addEventListener("mousedown", () => {
    isResizing = true;
    document.body.style.cursor = "col-resize";
});

document.addEventListener("mousemove", (e) => {
    if (!isResizing) return;
    sidebar.style.width = e.clientX + "px";
});

document.addEventListener("mouseup", () => {
    isResizing = false;
    document.body.style.cursor = "default";
});

toggleBtn.addEventListener("click", () => {
    sidebar.classList.toggle("hidden");
    resizer.style.display = sidebar.classList.contains("hidden") ? "none" : "block";
});


function renderItems(items, level = 0) {
    const tbody = document.getElementById("tableBody");

    items.forEach(item => {
        const tr = document.createElement("tr");
        const td = document.createElement("td");

        td.style.paddingLeft = `${level * 16}px`;

        if (item.type === "folder") {
            td.innerHTML = `📁 ${item.name}`;
            td.classList.add("folder");
            tr.appendChild(td);
            tbody.appendChild(tr);

            if (item.children) {
                renderItems(item.children, level + 1);
            }
        } else {
            td.innerHTML = `⚡ ${item.name}`;
            tr.appendChild(td);
            tbody.appendChild(tr);
        }
    });
}

renderItems(items);