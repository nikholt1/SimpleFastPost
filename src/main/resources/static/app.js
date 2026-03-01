const BASE_URL = "http://localhost:8080/api";

let listOfFolders = [];
let listOfCommands = [];

document.addEventListener("DOMContentLoaded", initApp);

/* =========================
   APP ENTRY
========================= */
async function initApp() {
    initSidebarResize();

    await Promise.all([
        getFolders(),
        getCommands()
    ]);

    const tree = buildSidebarTree(listOfFolders, listOfCommands);
    renderSidebar(tree);
}

/* =========================
   API
========================= */
async function getFolders() {
    const res = await fetch(`${BASE_URL}/folders`);
    listOfFolders = await res.json();
}

async function getCommands() {
    const res = await fetch(`${BASE_URL}/commands`);
    listOfCommands = await res.json();
}

/* =========================
   TREE NORMALIZATION
========================= */
function buildSidebarTree(folders, commands) {
    const roots = [];

    // folders already contain their commands
    folders.forEach(folder => {
        roots.push({
            type: "folder",
            id: folder.id,
            name: folder.name,
            expanded: false,
            children: (folder.listOfCommands || []).map(cmd => ({
                ...cmd,
                type: "command"
            }))
        });
    });

    // root commands (no folderId)
    commands
        .filter(cmd => cmd.folderId == null)
        .forEach(cmd => {
            roots.push({
                ...cmd,
                type: "command"
            });
        });

    return roots;
}

/* =========================
   SIDEBAR RENDER
========================= */
function renderSidebar(items) {
    const tbody = document.getElementById("tableBody");
    if (!tbody) return;

    tbody.innerHTML = "";

    items.forEach(item => {
        renderItem(item, tbody, 0);
    });
}

function renderItem(item, tbody, level) {
    const row = createRow(item, level);
    tbody.appendChild(row);

    if (item.type === "folder" && item.expanded) {
        item.children.forEach(child => {
            renderItem(child, tbody, level + 1);
        });
    }
}

/* =========================
   ROW FACTORY
========================= */
function createRow(item, level) {
    const tr = document.createElement("tr");
    const td = document.createElement("td");

    td.style.paddingLeft = `${level * 16}px`;

    if (item.type === "folder") {
        const hasChildren = item.children.length > 0;
        td.textContent = `${hasChildren ? (item.expanded ? "▼" : "▶") : ""} 📁 ${item.name}`;
        td.classList.add("folder");

        tr.addEventListener("click", () => {
            item.expanded = !item.expanded;
            renderSidebar(buildSidebarTree(listOfFolders, listOfCommands));
        });
    } else {
        td.textContent = `⚡ ${item.name}`;
        tr.addEventListener("click", () => renderCommand(item));
    }

    tr.appendChild(td);
    return tr;
}

/* =========================
   MAIN VIEW
========================= */
function renderCommand(command) {
    const main = document.getElementById("main");
    if (!main) return;

    main.innerHTML = `
        <h3>${command.name}</h3>
        <hr />
        <p><strong>Command</strong></p>
        <pre>${command.command}</pre>
        ${
        command.body
            ? `<p><strong>Body</strong></p><pre>${command.body}</pre>`
            : ""
    }
    `;
}

/* =========================
   SIDEBAR RESIZE
========================= */
function initSidebarResize() {
    const sidebar = document.getElementById("sidebar");
    const resizer = document.getElementById("resizer");
    const toggleBtn = document.getElementById("toggleSidebar");

    if (!sidebar || !resizer || !toggleBtn) return;

    let resizing = false;

    resizer.addEventListener("mousedown", () => {
        resizing = true;
        document.body.style.cursor = "col-resize";
    });

    document.addEventListener("mousemove", e => {
        if (!resizing) return;
        sidebar.style.width = `${e.clientX}px`;
    });

    document.addEventListener("mouseup", () => {
        resizing = false;
        document.body.style.cursor = "default";
    });

    toggleBtn.addEventListener("click", () => {
        sidebar.classList.toggle("hidden");
    });
}