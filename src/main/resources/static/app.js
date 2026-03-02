const BASE_URL = "http://localhost:8080/api";

let listOfFolders = [];
let listOfCommands = [];
let sidebarTree = [];

document.addEventListener("DOMContentLoaded", initApp);


async function initApp() {
    initSidebarResize();

    await Promise.all([
        getFolders(),
        getCommands()
    ]);

    sidebarTree = buildSidebarTree(listOfFolders, listOfCommands);
    renderSidebar(sidebarTree);

    let addFolder = document.getElementById("addFolder");
    let addCommand = document.getElementById("addCommand");


    addFolder.addEventListener("click", () => renderNewFolder())
    addCommand.addEventListener("click", () => renderNewCommand());

}


async function getFolders() {
    const res = await fetch(`${BASE_URL}/folders`);
    listOfFolders = await res.json();
}

async function getCommands() {
    const res = await fetch(`${BASE_URL}/commands`);
    listOfCommands = await res.json();
}


function buildSidebarTree(folders, commands) {
    const roots = [];

    folders.forEach(folder => {
        roots.push({
            type: "folder",
            id: folder.id,
            name: folder.projectName,
            expanded: false,
            children: (folder.cmds || []).map(cmd => ({
                ...cmd,
                type: "command"
            }))
        });
    });

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


function renderSidebar(items) {
    const tbody = document.getElementById("tableBody");
    if (!tbody) return;

    tbody.innerHTML = "";
    items.forEach(item => renderItem(item, tbody, 0));
}

function renderItem(item, tbody, level) {
    const tr = document.createElement("tr");
    const td = document.createElement("td");

    // Add padding for hierarchy
    td.style.paddingLeft = `${level * 16}px`;

    // Apply dark theme class
    tr.classList.add("dark-row");
    td.classList.add("dark-cell");

    if (item.type === "folder") {
        const hasChildren = item.children.length > 0;
        td.textContent = `${hasChildren ? (item.expanded ? "▼" : "▶") : ""} 📁 ${item.name}`;
        td.classList.add("folder");

        tr.addEventListener("click", () => {
            item.expanded = !item.expanded;
            renderSidebar(sidebarTree);
        });
    } else {
        td.textContent = `-> ${item.name}`;
        tr.addEventListener("click", () => renderCommand(item));
    }

    tr.appendChild(td);
    tbody.appendChild(tr);

    if (item.type === "folder" && item.expanded) {
        item.children.forEach(child =>
            renderItem(child, tbody, level + 1)
        );
    }
}

function renderNewCommand() {
    const main = document.getElementById("main");
    if (!main) return;

    main.innerHTML = `
        <form id="commandForm">
            <h5>Add Command</h5>
            <input type="text" placeholder="Command Name" required>
            <input type="text" placeholder="Command" required>
            <input type="text" placeholder="Command Body" required>
            <button class="btn btn-primary">Add Command</button>
        </form>
    `;
}

function renderCommand(command) {
    const main = document.getElementById("main");
    if (!main) return;

    main.innerHTML = `
        <div class="dark-main-content">
            <h3 class="dark-text">${command.name}</h3>
            <hr style="border-color: #3a3a3a;">
            <p class="dark-text"><strong>Command</strong></p>
            <pre class="dark-pre">${command.command}</pre>
            ${
        command.body
            ? `<p class="dark-text"><strong>Body</strong></p><pre class="dark-pre">${command.body}</pre>`
            : ""
    }
        </div>
    `;
}

function renderNewFolder() {
    const main = document.getElementById("main");
    if (!main) return;

    main.innerHTML = `
        <form id="folderForm">
            <h5>Add Folder</h5>
            <input type="text" placeholder="Folder Name" required>
            <button class="btn btn-primary">Add Folder</button>
        </form>
    `;
}

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