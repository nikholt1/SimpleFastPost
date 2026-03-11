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

    let homeButton = document.getElementById("homePress");
    let addFolder = document.getElementById("addFolder");
    let addCommand = document.getElementById("addCommand");
    let helpButton = document.getElementById("helpBtn");
    let syncBtn = document.getElementById("syncBtn");
    let pushBtn = document.getElementById("pushBtn");
    homeButton.addEventListener("click", () => renderHome());

    addFolder.addEventListener("click", () => renderNewFolder());
    addCommand.addEventListener("click", () => renderNewCommand());
    helpButton.addEventListener("click", () => renderHelp());

    syncBtn.addEventListener("click", () => sync());
    pushBtn.addEventListener("click", () => pushSync());
}

async function pushSync() {
    const loadingBar = document.getElementById("loadingBar");

    // still does not work for some reason
    loadingBar.classList.remove("hidden");
    console.log("push")
    await fetch("http://localhost:8080/api/push", { method: "POST" });

    await new Promise(resolve => setTimeout(resolve, 500));

    loadingBar.classList.add("hidden");
    // window.location.reload();
}


async function sync() {
    const loadingBar = document.getElementById("loadingBar");


    loadingBar.classList.remove("hidden");


    await new Promise(requestAnimationFrame);


    await fetch("http://localhost:8080/api/sync", { method: "POST" });


    await new Promise(resolve => setTimeout(resolve, 500));


    window.location.reload();
}
function renderHome() {

    const main = document.getElementById("main");
    if (!main) return;

    main.innerHTML = `
       <div id="startPage">
        <div>
            <img src="image/SFPLogoNoBackground.jpg" style="width: 200px">
        </div>
        <div class="startPageContent">
            <h1 style="color: white;">Welcome to SFP</h1>
            <p style="color: #bababa;">With SimpleFastPost you can simply run specified folders of commands and single commands from the terminal</p>

            <p>Checkout the repository</p>
            <a href="https://github.com/nikholt1/SimpleFastPost" target="_blank">
                <button class="addFolderButton" style="width: 200px;">Github repo</button>
            </a>
        </div>
    </div>
    `;
}
function renderHelp() {
    const main = document.getElementById("main");
    if (!main) return;

    const startPage = document.getElementById("startPage");
    startPage.style.justifyContent = "left";
    main.innerHTML = `
<div id="startPage">
            <div>

            </div>
            <div class="startPageContent">
                <h1><img src="image/SFPLogoNoBackground.jpg" style="width: 50px"> Help</h1>
                <h4>Commands</h4>
                <strong>Creating commands:</strong><br>
                <strong>Browser:</strong><br>
                <p>to create a new command in the browser, either choose the "+ Command" button at the bottom right corner of the side bar or
                unfold a folder and press the "+" button. <br><br>
                In the "Create command section" choose the methods and url that the command should point at, or input a custom command.
                <br><br>
                    from there choose the rest of the command setup, and or extend it with body.
                </p>
                <strong>CLI:</strong><br>
                <p>In cli mode run the application "sfp" command and choose the " -m " flag, to define the method and afterward define the method with POST, DELETE, PUT, GET, PATH or OPTIONS <br>
                after that define the new flag for the url " - u example.com " <br><br>
                For more flags and information use the " --help " flag</p>
                <br><br>
                <strong>Creating folders</strong><br>
                <strong>Browser:</strong><br>
                <p>Press the "+ folder " button in the bottom left corner of the sidebar, input the name of the folder and press "Add Folder" <br><br>
                </p>
                <strong>CLI: </strong>
                <p>in the command write "sfp" followed by "mkdir" and the foldername -> run the command</p>


            </div>
            </div>
    `;
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

    td.style.paddingLeft = `${level * 16}px`;

    tr.classList.add("dark-row");
    td.classList.add("dark-cell");

    const container = document.createElement("div");
    container.style.display = "flex";
    container.style.justifyContent = "space-between";
    container.style.alignItems = "center";

    const label = document.createElement("span");

    if (item.type === "folder") {
        const hasChildren = item.children.length > 0;
        const arrowIcon = hasChildren
            ? `<i class="bi ${item.expanded ? "bi-chevron-down" : "bi-chevron-right"}" style="font-size:0.7rem; margin-right:2px;"></i>`
            : "";

        label.innerHTML = `
        ${arrowIcon}
        <i class="bi bi-folder" style="color: lightgrey; margin-right:4px;"></i>
        ${item.name}
    `;

        label.classList.add("folder");

        tr.addEventListener("click", () => {
            item.expanded = !item.expanded;
            renderSidebar(sidebarTree);
        });
    } else {
        const methodColors = {
            "GET": "#4aa35a",      // green
            "POST": "#387bb5",     // blue
            "PUT": "#dc7e1f",      // yellow/orange
            "DELETE": "#dc4c59",   // red
            "PATCH": "#4cb6ca",    // teal
            "OPTIONS": "#6c757d"   // gray
        };

        // Find the HTTP method in the command
        let methodFound = null;
        for (const method of Object.keys(methodColors)) {
            if (item.command.includes(method)) {
                methodFound = method;
                break; // stop after first match
            }
        }

        // Clear previous content
        label.textContent = "";

        if (methodFound) {
            // Create a colored span for the method
            const methodSpan = document.createElement("span");
            methodSpan.textContent = methodFound;
            methodSpan.style.backgroundColor = methodColors[methodFound];
            methodSpan.style.color = "#fff";
            methodSpan.style.padding = "2px 4px";
            methodSpan.style.borderRadius = "4px";
            methodSpan.style.marginRight = "6px";
            methodSpan.style.fontSize = "12px"; // smaller than main label
            methodSpan.style.fontWeight = "bold";

            label.appendChild(methodSpan);
        } else {
            // No method found → show "{ }" prefix
            const emptySpan = document.createElement("span");
            emptySpan.textContent = "{ }";
            emptySpan.style.marginRight = "6px";
            label.appendChild(emptySpan);
        }

        // Append the item name as normal text
        const nameText = document.createTextNode(item.name);
        label.appendChild(nameText);

        // Common style
        label.style.fontSize = "15px";

        // Add click event
        tr.addEventListener("click", () => renderCommand(item));
    }

    const removeBtn = document.createElement("button");
    removeBtn.textContent = "-";
    removeBtn.classList.add("sidebar-btn", "remove-btn");


    removeBtn.addEventListener("click", async (e) => {
        e.stopPropagation();

        if (item.type === "folder") {
            await deleteFolder(item.id);
        } else {
            await deleteCommand(item.id);
        }
    });

    container.appendChild(label);
    container.appendChild(removeBtn);

    td.appendChild(container);
    tr.appendChild(td);
    tbody.appendChild(tr);

    if (item.type === "folder" && item.expanded) {

        item.children.forEach(child =>
            renderItem(child, tbody, level + 1)
        );

        const addRow = document.createElement("tr");
        const addTd = document.createElement("td");
        addTd.style.paddingLeft = `${(level + 1) * 16}px`;

        const addBtn = document.createElement("button");
        addBtn.textContent = "+";
        addBtn.classList.add("sidebar-btn", "add-btn");

        addBtn.addEventListener("click", (e) => {
            e.stopPropagation();
            renderNewCommand(item.id);
        });

        addTd.appendChild(addBtn);
        addRow.appendChild(addTd);
        tbody.appendChild(addRow);
    }
}
async function deleteCommand(id) {
    try {
        const response = await fetch(`${BASE_URL}/commands/${id}`, {
            method: "DELETE"
        });

        if (response.status !== 204) {
            throw new Error(`Unexpected status: ${response.status}`);
        }

        await refreshSidebar();

    } catch (err) {
        console.error(err);
        alert("Error deleting command");
    }
}
async function deleteFolder(id) {
    try {
        const response = await fetch(`${BASE_URL}/folders/${id}`, {
            method: "DELETE"
        });

        if (response.status !== 204) {
            throw new Error(`Unexpected status: ${response.status}`);
        }

        await refreshSidebar();

    } catch (err) {
        console.error(err);
        alert("Error deleting folder");
    }
}
async function refreshSidebar() {
    await Promise.all([
        getFolders(),
        getCommands()
    ]);

    sidebarTree = buildSidebarTree(listOfFolders, listOfCommands);
    renderSidebar(sidebarTree);
}
function renderNewCommand(folderId = null) {
    const main = document.getElementById("main");
    if (!main) return;

    main.innerHTML = `
        <form id="commandForm">
            <h5>Add Command</h5>
    
            <!-- Styled method dropdown -->
            <input type="text" id="commandName" placeholder="Command Name" required>
            <div>
                <select id="requestMethod" class="method-select">
                    <option class="method" value="GET" selected>GET</option>
                    <option class="method" value="POST">POST</option>
                    <option class="method" value="PUT">PUT</option>
                    <option class="method" value="DELETE">DELETE</option>
                    <option class="method" value="PATCH">PATCH</option>
                </select>
                <input style="width: 80%" type="text" id="commandURL" placeholder="URL">
    
            </div>
            <input type="text" id="commandCmd" placeholder="Command" required>
            <textarea id="commandBody" placeholder="Command Body (optional)" style="background-color: rgba(43,45,48,0.5); color: white; width: 100%; height: 200px; padding: 6px; font-size: 14px; box-sizing: border-box;"></textarea>
            <button class="hidden"
                    style="background-color: #815cd7; color: white; width: 200px">Add more headers</button>
    
            <button  type="submit" class="btn btn-primary"
                    style="background-color: #238536; color: white">
                Add Command
            </button>
        </form>
    `;
    const commandForm = document.getElementById("commandForm");

    const methodSelect = document.getElementById("requestMethod");
    const urlInput = document.getElementById("commandURL");
    const commandInput = document.getElementById("commandCmd");

// Function to keep the command synced
    function updateCommand() {
        const method = methodSelect.value;
        const url = urlInput.value.trim();

        commandInput.value = `-m ${method} -u ${url}`;
    }

// Initial value
    updateCommand();

// Update when method changes
    methodSelect.addEventListener("change", updateCommand);

// Update when URL changes
    urlInput.addEventListener("input", updateCommand);

    commandForm.addEventListener("submit", async (e) => {
        e.preventDefault();

        const name = document.getElementById("commandName").value.trim();
        const command = commandInput.value.trim();

        const bodyInput = document.getElementById("commandBody").value.trim();
        const body = bodyInput === "" ? null : bodyInput;

        await requestAddCommand({
            name,
            command,
            body,
            folderId
        });
    });
}

async function requestAddCommand({ name, command, body, folderId = null }) {
    try {
        const response = await fetch(BASE_URL + "/command", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                name,
                command,
                body,
                folderId
            }),
        });

        if (!response.ok) throw new Error("Failed to create command");

        await refreshSidebar();

        const main = document.getElementById("main");
        main.innerHTML = "";

    } catch (err) {
        console.error(err);
        alert("Error creating command: " + err.message);
    }
}

function renderCommand(command) {
    const main = document.getElementById("main");
    if (!main) return;

    main.innerHTML = `
        <div class="dark-main-content">

            <h4 class="dark-text" id="commandTitle">${command.name}</h4>

            <hr style="border-color: #3a3a3a;">

            <p class="dark-text"><strong>Command</strong></p>
            <textarea id="commandCmd" class="dark-pre">${command.command}</textarea>

            <p class="dark-text"><strong>Body</strong></p>
            <textarea id="commandBody" class="dark-body">${command.body ?? ""}</textarea>

        </div>
    `;

    const title = document.getElementById("commandTitle");
    const cmdInput = document.getElementById("commandCmd");
    const bodyInput = document.getElementById("commandBody");

    /*
    NAME RENAME
    */
    title.addEventListener("dblclick", () => {

        const input = document.createElement("input");
        input.type = "text";
        input.value = command.name;
        input.classList.add("dark-input");

        title.replaceWith(input);
        input.focus();

        input.addEventListener("keydown", async (e) => {
            if (e.key === "Enter") {

                const newName = input.value.trim();
                if (!newName) return;

                await updateCommandField(command, {
                    name: newName
                });

                command.name = newName;
                renderCommand(command);
            }
        });
    });

    /*
    UPDATE COMMAND
    */
    cmdInput.addEventListener("keydown", async (e) => {
        if (e.key === "Enter" && !e.shiftKey) {
            e.preventDefault();

            const newCommand = cmdInput.value.trim();

            await updateCommandField(command, {
                command: newCommand
            });

            command.command = newCommand;
        }
    });

    /*
    UPDATE BODY
    */
    bodyInput.addEventListener("keydown", async (e) => {
        if (e.key === "Enter" && !e.shiftKey) {
            e.preventDefault();

            const newBody = bodyInput.value.trim() || null;

            await updateCommandField(command, {
                body: newBody
            });

            command.body = newBody;
        }
    });
}
async function updateCommandField(command, updates) {
    try {
        const response = await fetch(BASE_URL + `/commands/${command.id}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                name: updates.name ?? command.name,
                command: updates.command ?? command.command,
                body: updates.body ?? command.body,
                folderId: command.folderId ?? null
            })
        });

        if (!response.ok) throw new Error("Failed to update command");

        const updated = await response.json();

        await refreshSidebar();

        return updated;

    } catch (err) {
        console.error(err);
        alert("Error updating command: " + err.message);
    }
}
function renderNewFolder() {
    const main = document.getElementById("main");
    if (!main) return;

    main.innerHTML = `
        <form id="folderForm">
            <h5>Add Folder</h5>
            <input type="text" id="folderName" placeholder="Folder Name" required>
            <button type="submit" class="btn btn-primary" 
                style="background-color: #238536; color: white">
                Add Folder
            </button>
        </form>
    `;

    const folderForm = document.getElementById("folderForm");

    folderForm.addEventListener("submit", async (e) => {
        e.preventDefault(); // prevent page reload
        console.log("Submitting folder form"); // debug

        const name = document.getElementById("folderName").value.trim();

        if (!name) return; // safety check

        await requestAddFolder({ name });
    });
}

async function requestAddFolder({ name }) {
    try {
        console.log("Sending request to backend for new folder"); // debug

        const response = await fetch(BASE_URL + "/folder", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ projectName: name })
        });

        console.log("Response status:", response.status); // debug

        if (!response.ok) throw new Error("Failed to create folder");

        const data = await response.json();
        console.log("Folder created:", data);

        // Refresh folders and sidebar
        await getFolders();
        sidebarTree = buildSidebarTree(listOfFolders, listOfCommands);
        renderSidebar(sidebarTree);

        // Clear the form
        const main = document.getElementById("main");
        main.innerHTML = "";

    } catch (err) {
        console.error(err);
        alert("Error creating folder: " + err.message);
    }
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