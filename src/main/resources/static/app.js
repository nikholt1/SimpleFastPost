const BASE_URL = "/api";

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

    await renderProjectInfo();

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



async function getDirectoryPath() {
    const res = await fetch(`${BASE_URL}/currentDir`);
    const data = await res.json();
    return data.currentDirectory;
}

function extractInitials(rootName) {
    let initials = rootName[0];  // Always take the first letter
    for (let i = 1; i < rootName.length; i++) {
        if (rootName[i] === rootName[i].toUpperCase() && rootName[i] !== rootName[i - 1].toUpperCase()) {
            initials += rootName[i];  // Add this uppercase letter if it's a new one
            break;  // Once we have two letters, we can stop
        }
    }
    return initials;
}

async function renderProjectInfo() {
    const directoryPath = document.getElementById("directoryPath");
    const pathRootName = document.getElementById("pathRootName");

    const directory = await getDirectoryPath();  // <-- MUST await
    directoryPath.textContent = directory;
    directoryPath.style.fontSize = "13px";
    directoryPath.style.color = "#bdbdbd";

    const rootName = directory.split(/[\\/]/).pop(); // get last folder
    const initials = extractInitials(rootName);

    const methodColors = {
        "GET": "#4aa35a",
        "POST": "#387bb5",
        "PUT": "#dc7e1f",
        "DELETE": "#dc4c59",
        "PATCH": "#4cb6ca",
        "OPTIONS": "#6c757d"
    };

// insert HTML
    pathRootName.innerHTML = `
    <div class="projectName" id="projectName">
        <span id="initial-box" class="initial-box">${initials}</span>${rootName}
    </div>
`;

    const projectDiv = document.getElementById("projectName");
    const initialBox = document.getElementById("initial-box");

// --- pick one random color ---
    const colors = Object.values(methodColors);
    const randomColor = colors[Math.floor(Math.random() * colors.length)];

// --- SOLID COLOR for initialBox ---
    initialBox.style.backgroundColor = randomColor;
    initialBox.style.color = "#fff"; // text stands out

// --- RADIAL GRADIENT for projectName ---
    function hexToRgba(hex, alpha = 0.41) {
        const bigint = parseInt(hex.slice(1), 16);
        const r = (bigint >> 16) & 255;
        const g = (bigint >> 8) & 255;
        const b = bigint & 255;
        return `rgba(${r}, ${g}, ${b}, ${alpha})`;
    }

    const rgbaColor = hexToRgba(randomColor, 0.41);
    projectDiv.style.background = `radial-gradient(circle at 0% 50%, ${rgbaColor} 0%, rgba(255, 255, 0, 0) 100%)`;
}
async function pushSync() {
    // const loadingBar = document.getElementById("loadingBar");
    const loadingBar2 = document.getElementById("loading-dots");
    loadingBar2.style.display="flex";
    const loadingbarState = document.getElementById("loadingbarState");
    loadingbarState.innerHTML="push"
    // still does not work for some reason
    // loadingBar.classList.remove("hidden");
    console.log("push")
    await fetch("http://localhost:8080/api/push", { method: "POST" });

    await new Promise(resolve => setTimeout(resolve, 500));

    loadingBar2.style.display="none";
    // loadingBar.classList.add("hidden");
    // window.location.reload();
}


async function sync() {
    // const loadingBar = document.getElementById("loadingBar");
    const loadingBar2 = document.getElementById("loading-dots");
    loadingBar2.style.display="flex";
    const loadingbarState = document.getElementById("loadingbarState");
    loadingbarState.innerHTML="sync"

    // loadingBar.classList.remove("hidden");


    await new Promise(requestAnimationFrame);


    await fetch("http://localhost:8080/api/sync", { method: "POST" });


    await new Promise(resolve => setTimeout(resolve, 500));


    window.location.reload();
}
function renderHome() {

    const main = document.getElementById("main");
    if (!main) return;

    main.innerHTML = `
       <div id="startPage" style="justify-content: center; align-items: center">
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
                <div class="banner">
                
                
                
                </div>
                <div class="startPageContent" style="width: 80%;"><h1><img src="image/SFPLogoNoBackground.jpg" style="width: 50px"> Help</h1>
                <h4>Setup:</h4>
                <p>In the project you want to work on, place a empty "sfp.http" file</p>
                <p>When running the program:</p>
                <p>Make sure to navigate to the same directory as where the "sfp.http" file is placed.</p>
                <p>From here either use the CLI commands, to sync and push and or run the api with "--api" and "-d" in detatched mode, for the program to be able to locate the .http file</p>
                <p>All non sync and push related commands in the CLI can be run from anywhere. In other words, if you are not working with the "sfp.http" file, the system works from any directory.</p>
                
                <br><br>
                <h4>Sync and push:</h4>
                <p>For sync and push to work properly, the root directory where you started the api, must have a either empty or filled file called "sfp.http"</p>
                <p>When pressing the "sync" button, which is a round arrow, the system updates the database and reloads the page, allowing you to receive and build the commands and folders from the .http file.</p>
                <p>When pressing push, the system allows you to push your changes or the commands that are in your database to the .http file.</p>
                <p><strong>Use case:</strong> When you are working on a project, the sfp.http file serves as a "one truth" for request testing. when you update your project and receive the updated sft.http file. pressing sync allows you to receive the changes. 
                The UI allows you to easily modify, build new or delete unwanted commands, and then push to the one truth sfp.http file. When you then push the program to the master branch and the rest of the team pull the latest changes, they can easily sync their database and run all the requests.</p>
                <br><br><br><br>
                <h4>Browser</h4>
                <strong>Commands:</strong>
                <p>To build a command in root, press the + Command button, from there a form will pop up allowing you to
                input a name for the command. Thereafter you can set the method by the dropdown menu, which by default is set to "GET".
                After the method you can specify the url that the command should target.</p>
                <p>The headers are seperated by key and value. to add more headers, press the "+" under the empty header. To delete a header press the "-" next to the header you want to delete</p>
                <p>If the method allows it, you can specify the JSON body in the large field under the headers.</p>
                <p>To save the command, press the "Add Command" button</p>
                <p>To delete a command, press the "-" button in the sidebar that is next to the command you want to delete.</p>
                <br><br>
                <p>To update a command, press the command from the sidebar selection. From there a new window should appear, with the specifications for the command.</p>
                <p>In the specified command window, you can update the command name, by pressing the command name and input a new name.</p>
                <p>In the command you can change the commands actual command directly in the input field</p>
                <p>To add more headers. press the "+" under the "Headers" headline.</p>
                <p>To change the body of the header, simply press in the body textarea of the command and change it</p>
                <p>All of the update fields are saved when clicking out of the specific field, and or by pressing "Enter"</p>
                <p>The specific command allows you to send  the command  by pressing the blue "Send" button by the command name, and get the response in the response area at the bottom of the page.</p> 
                <br><br>
                <strong>Folders:</strong>
                <p>To create a new folder, press the "+ Folder" button and input the desired name of the folder in the input field. then press the "Add Folder" button.</p>
                <p>To add a command to a specific folder, unfold the folder and press the "+" button inside the folder, and follow the steps for creating a command.</p>
                <p>To delete a command, press the "-" button next to the command you want to delete</p>
                <p>The UI allows you to send all the commands in a folder, by pressing the two arrows next to the folder name, of which you want to run all commands in. from there, a window will open, showing you the commands names and their respective response.</p>
                <br><br>

                
                <h4>CLI</h4>
                <strong>sync and push</strong>
                <p>To sync the sfp database make sure to navigate same directory as the desired sfp.http folder you want to sync with, thereafter simply run "sync"</p>
                <p>To push to the sfp.http file, make sure to navigate to the same directory as the desired sfp.http folder you want to push to and run "push"</p>
                <br><br>
                <strong>Commands:</strong>
                <p>To see all flags and commands type "--help"</p>
                <p>To run a simple command, type "-m" flag for the method followed by the method for example "GET", thereafter type "-u" for the url followed by the url 
                example input: "sfp -m GET -u thisIsTheURL.com" <br>
                After running the command, the system asks you if you want to add headers, if not skip to the next step. <br>
                If you want to include headers, the system prompts you for a key and a value for the headers.
                If the method "-m" is a POST or PUT, the system automatically prompts you for a body.</p>
                <p>To save a command, input the command you want to save, followed by "-sv" and "-cmd" to specify save mode, and that its a command. thereafter input the desired name of the command. <br>
                If you want to save the command into a folder, specify it folder by "-f" for folder and the foldername <br>
                Example input "sfp -m POST -u thisIsTheUrl.com -sv -cmd ThisIsTheCommandName -f ThisIsTheFolderIWantToSaveInto"</p>
                <p>To delete a command, run "delete -cmd" followed by the command name</p>
                <p>To delete a header in a command run "delete -cmd" followed by the command name, then "header" followed by the headername <br>
                Example input: "delete -cmd commandName header headerName"</p>
                <p>To update a command, run "update -cmd" followed by the command name then specify which field of the command you want to update with "--set" "field:value" <br>
                Example input: "update commandName set url:theurl.com <br>
                The commands have the following fields: name, url, method and body <br>
                To update the commands header, run "update command commandName header headerKey/name <br>
                Thereafter you are prompted for a new key and value for the header <br>
                Example input: "update command newCommand header key"</p>
                <p>To simply update the name of the command run "update command commandName --name newCommandName"</p>
                <p>To move the command to a new folder, run "update command commandName --set folder:foldername" <br>
                If you want to move the command back to root, run "update command commandName --set folder:0" </p>
                <br><br>
                <strong>Folders:</strong>
                <p>To create a new folder simply type "mkdir" followed by the foldername.</p>
                <p>To delete a folder, run "delete -f" followed by the folder name</p>
                <p>To update the folder run "update folder foldername --name newFolderName"</p>

                <strong>API:</strong>
                <p>To start the api, make sure you are in the same directory of where the "sfp.http" folder is located. (it can also be run from anywhere else, but then the sync and push functionality will not work) <br>
                run "--api", this spins up the API on the default port 8080. <br>
                If you want to run the API in detached mode, simply run "--api -d" for detatched mode. <br>
                If you want to run the api at a specific port, run the api followed by the "-p" flag and input the desired port number.</p>
                <p>To list the running instances of the API, run "--api ls" this shows you the current running apis, and their pid/id's</p>
                <p>To stop the detatched API, run "--api stop" followed by the pid <br>
                Example input: "sfp --api stop 23qa1"</p>
            
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

const expandedFolders = new Set();

function renderSidebar(items) {
    const tbody = document.getElementById("tableBody");
    if (!tbody) return;

    tbody.innerHTML = "";


    if (!document.body.dataset.globalDropInitialized) {
        document.body.dataset.globalDropInitialized = "true";

        document.body.addEventListener("dragover", (e) => {
            e.preventDefault();
        });

        document.body.addEventListener("drop", async (e) => {
            e.preventDefault();

            const commandId = e.dataTransfer.getData("commandId");
            if (!commandId) return;


            const row = e.target.closest("tr");
            if (row && row.dataset.type === "folder") return;


            await updateCommandFolder(commandId, null);
            renderSidebar(sidebarTree);
        });
    }

    items.forEach(item => renderItem(item, tbody, 0));
}

function renderItem(item, tbody, level) {
    const tr = document.createElement("tr");
    const td = document.createElement("td");
    td.style.paddingLeft = `${level * 16}px`;
    tr.classList.add("dark-row");
    td.classList.add("dark-cell");

    tr.dataset.type = item.type;
    tr.dataset.id = item.id;

    const container = document.createElement("div");
    container.style.display = "flex";
    container.style.justifyContent = "space-between";
    container.style.alignItems = "center";

    const label = document.createElement("span");

    // 🔹 FOLDER
    if (item.type === "folder") {
        const isExpanded = expandedFolders.has(item.id);
        const hasChildren = item.children.length > 0;
        const arrowIcon = hasChildren
            ? `<i class="bi ${isExpanded ? "bi-chevron-down" : "bi-chevron-right"}" style="font-size:0.7rem;margin-right:2px;"></i>`
            : "";

        label.innerHTML = `
            <div style="display:flex;align-items:center;">
                ${arrowIcon}
                <i class="bi bi-folder" style="color:lightgrey;margin-right:4px;"></i>
                ${item.name}
                <img class="runAllInFolder" 
                     style="margin-left:15px;width:17px;height:17px;cursor:pointer" 
                     src="image/runAll.png">
            </div>
        `;
        label.classList.add("folder");

        tr.addEventListener("click", () => {
            if (expandedFolders.has(item.id)) expandedFolders.delete(item.id);
            else expandedFolders.add(item.id);
            renderSidebar(sidebarTree);
        });

        const runAllBtn = label.querySelector(".runAllInFolder");
        if (runAllBtn) {
            runAllBtn.addEventListener("click", (e) => {
                e.stopPropagation();
                runAllCommandsInFolder(item);
            });
        }

        // Folder drop
        tr.addEventListener("dragover", (e) => e.preventDefault());
        tr.addEventListener("drop", async (e) => {
            e.preventDefault();
            e.stopPropagation();
            const commandId = e.dataTransfer.getData("commandId");
            if (!commandId) return;
            await updateCommandFolder(commandId, item.id);
            renderSidebar(sidebarTree);
        });

    } else {
        // 🔹 COMMAND
        const methodColors = {
            "GET": "#4aa35a",
            "POST": "#387bb5",
            "PUT": "#dc7e1f",
            "DELETE": "#dc4c59",
            "PATCH": "#4cb6ca",
            "OPTIONS": "#6c757d"
        };

        let methodFound = null;
        for (const method of Object.keys(methodColors)) {
            if (item.command.includes(method)) {
                methodFound = method;
                break;
            }
        }

        if (methodFound) {
            const methodSpan = document.createElement("span");
            methodSpan.textContent = methodFound;
            methodSpan.style.backgroundColor = methodColors[methodFound];
            methodSpan.style.color = "#fff";
            methodSpan.style.padding = "2px 4px";
            methodSpan.style.borderRadius = "4px";
            methodSpan.style.marginRight = "6px";
            methodSpan.style.fontSize = "12px";
            methodSpan.style.fontWeight = "bold";
            label.appendChild(methodSpan);
        } else {
            const emptySpan = document.createElement("span");
            emptySpan.textContent = "{ }";
            emptySpan.style.marginRight = "6px";
            label.appendChild(emptySpan);
        }

        label.appendChild(document.createTextNode(item.name));
        label.style.fontSize = "15px";

        tr.addEventListener("click", () => renderCommand(item));
    }

    // 🔹 REMOVE BUTTON
    const removeBtn = document.createElement("button");
    removeBtn.textContent = "-";
    removeBtn.classList.add("sidebar-btn", "remove-btn");
    removeBtn.addEventListener("click", async (e) => {
        e.stopPropagation();
        if (item.type === "folder") await deleteFolder(item.id);
        else await deleteCommand(item.id);
    });

    container.appendChild(label);
    container.appendChild(removeBtn);
    td.appendChild(container);
    tr.appendChild(td);
    tbody.appendChild(tr);

    // 🔹 DRAG
    tr.draggable = item.type !== "folder";
    tr.addEventListener("dragstart", (e) => {
        e.dataTransfer.setData("commandId", item.id);
    });

    // 🔹 CHILDREN
    if (item.type === "folder" && expandedFolders.has(item.id)) {
        item.children.forEach(child => renderItem(child, tbody, level + 1));

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

// 🔹 API
async function updateCommandFolder(commandId, folderId) {
    await fetch(`api/commands/${commandId}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ folderId })
    });
    window.location.reload();

}
// --- JSON syntax highlighting helper ---
function highlightJson(json) {
    const str = typeof json === "string" ? json : JSON.stringify(json, null, 2);
    return str
        .replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?)/g, match => {
            if (match.endsWith(":")) return `<span style="color:#d19a66;">${match}</span>`; // key
            return `<span style="color:#98c379;">${match}</span>`; // string value
        })
        .replace(/\b(true|false|null)\b/g, `<span style="color:#56b6c2;">$1</span>`) // boolean / null
        .replace(/\b-?\d+(\.\d+)?\b/g, `<span style="color:#61afef;">$&</span>`); // numbers
}
async function runAllCommandsInFolder(folder) {

    console.log("Running all commands in folder:", folder.name);

    const loadingBar2 = document.getElementById("loading-dots");
    loadingBar2.style.display = "flex";

    const loadingbarState = document.getElementById("loadingbarState");
    loadingbarState.innerHTML = "running all";

    const main = document.getElementById("main");

    main.innerHTML = `
        <div style="padding: 10px;">
        <h3>${folder.name}</h3>
        <div id="commandStatusList" style="margin-bottom:20px"></div>
    `;

    const statusList = document.getElementById("commandStatusList");
    const statusMap = {};

    // ---------- Status indicators ----------
    for (const child of folder.children) {
        if (child.type === "command") {

            const row = document.createElement("div");
            row.style.display = "flex";
            row.style.alignItems = "center";
            row.style.marginBottom = "4px";

            const icon = document.createElement("span");
            icon.textContent = "✖";
            icon.style.color = "#da3633";
            icon.style.marginRight = "8px";
            icon.style.fontWeight = "bold";

            const name = document.createElement("span");
            name.textContent = child.name;

            row.appendChild(icon);
            row.appendChild(name);

            statusList.appendChild(row);
            statusMap[child.id] = icon;
        }
    }

    // ---------- Run commands ----------
    for (const child of folder.children) {

        if (child.type === "command") {

            const result = await executeCommand(child);

            const icon = statusMap[child.id];

            const success =
                typeof result.status === "number" &&
                result.status >= 200 &&
                result.status < 300;

            if (icon) {
                icon.textContent = success ? "✔" : "✖";
                icon.style.color = success ? "#44bd4f" : "#da3633";
            }

            const responseColor =
                result.status >= 200 && result.status < 300 ? "#238636" :
                    result.status >= 400 && result.status < 500 ? "#d29922" :
                        result.status >= 500 ? "#da3633" :
                            result.status === "ERR" ? "#da3633" :
                                "#6e7681";

            const commandBlock = document.createElement("div");

            // ---------- Pretty-print JSON ----------
            let displayBody = result.body;
            try {
                const parsed = typeof result.body === "string"
                    ? JSON.parse(result.body)
                    : result.body;
                displayBody = JSON.stringify(parsed, null, 2);
            } catch {}

            const headerEntries = result.headers ? Object.entries(result.headers) : [];
            const headerCount = headerEntries.length;

            // ---------- Base HTML ----------
            commandBlock.innerHTML = `
            <div style="margin-top:10px; padding: 10px;">
                <p style="font-weight:bold">${child.name}</p>
            
                <div style="margin-bottom:10px;display:flex;align-items:center">
                    <p class="dark-text" style="margin:0">Response</p>
                    <span 
                        style="
                            background-color:${responseColor};
                            color:white;
                            border-radius:4px;
                            padding:2px 6px;
                            margin-left:6px;
                            font-size:12px;
                            font-weight:bold;
                            
                        ">
                        ${result.status}
                    </span>
                </div>
            
                <!-- DROPDOWN HEADER -->
                <div class="headers-toggle" style="cursor:pointer; user-select:none; margin-bottom:6px;">
                    ▶ Headers (${headerCount})
                </div>
            
                <!-- COLLAPSIBLE -->
                <div class="headers-container" style="display:none; margin-bottom:10px;"></div>
            
                <p class="dark-text">Body</p>
            
                <!-- Scrollable & resizable container -->
                <div style="width:100%; max-height:400px; overflow:auto; border-radius:4px; border:1px solid #3a3a3a;">
                    <pre 
                        class="dark-body"
                        contenteditable
                        style="
                            width:100%;
                            min-height:200px;
                            margin:0;
                            white-space: pre-wrap;
                            word-break: break-word;
                            background:#1e1e1e;
                            color:#abb2bf;
                            padding:10px;
                            resize: vertical;  /* draggable resize */
                            overflow:auto;
                            border-radius:4px;
                            box-sizing:border-box;
                        "
                    >${highlightJson(displayBody)}</pre>
                </div>
            </div>
            `;

            const toggle = commandBlock.querySelector(".headers-toggle");
            const headersContainer = commandBlock.querySelector(".headers-container");


            if (headerCount > 0) {

                headerEntries.forEach(([key, value]) => {

                    const row = document.createElement("div");
                    row.style.display = "flex";
                    row.style.gap = "6px";
                    row.style.marginBottom = "4px";

                    const keyInput = document.createElement("input");
                    keyInput.value = key;
                    keyInput.readOnly = true;
                    keyInput.className = "dark-pre";
                    keyInput.style.flex = "1";
                    keyInput.style.color = "white";
                    const valueInput = document.createElement("input");
                    valueInput.value = value;
                    valueInput.readOnly = true;
                    valueInput.className = "dark-pre";
                    valueInput.style.flex = "2";
                    valueInput.style.color = "white";
                    valueInput.addEventListener("click", () => valueInput.select());

                    row.appendChild(keyInput);
                    row.appendChild(valueInput);

                    headersContainer.appendChild(row);
                });

            } else {
                const empty = document.createElement("div");
                empty.textContent = "No headers";
                empty.style.color = "#6e7681";
                headersContainer.appendChild(empty);
            }


            let open = false;


            const numberColor = headerCount > 0 ? "#36ca53" : "white";


            toggle.innerHTML = `<i style="font-size:0.7rem;margin-right:2px;" class="bi bi-chevron-right"></i> Headers <span style="color:${numberColor};">(${headerCount})</span>`;


            toggle.addEventListener("click", () => {
                open = !open;
                headersContainer.style.display = open ? "block" : "none";


                toggle.innerHTML = `<i style="font-size:0.7rem;margin-right:2px;" class="bi ${open ? "bi-chevron-down" : "bi-chevron-right"}"></i> Headers <span style="color:${numberColor};">(${headerCount})</span>`;
            });

            main.appendChild(commandBlock);
        }

        if (child.type === "folder") {
            await runAllCommandsInFolder(child);
        }
    }

    loadingBar2.style.display = "none";
}
async function executeCommand(command) {
    const loadingBar2 = document.getElementById("loading-dots");
    loadingBar2.style.display = "flex";

    const { method, url } = parseCommand(command.command);
    if (!url) return { status: "ERR", body: "Invalid command: Missing URL (-u)" };

    const headersRes = await fetch(`${BASE_URL}/headers/byCommand/${command.id}`);
    const headersData = await headersRes.json();
    const headers = {};
    headersData.forEach(h => { if (h.key) headers[h.key] = h.value; });

    let body = command.body?.trim() || undefined;

    try {
        const res = await fetch(`${BASE_URL}/executeCommand`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ method, url, headers, body })
        });

        // 🔥 EXTRACT HEADERS HERE
        const responseHeaders = {};
        res.headers.forEach((value, key) => {
            responseHeaders[key] = value;
        });

        let responseBody;
        const contentType = res.headers.get("content-type") || "";

        if (contentType.includes("application/json")) {
            const json = await res.json();
            responseBody = JSON.stringify(json, null, 2);
        } else {
            responseBody = await res.text();
        }

        loadingBar2.style.display = "none";

        return {
            status: res.status,
            ok: res.ok,
            body: responseBody,
            headers: responseHeaders   // ✅ NOW INCLUDED
        };

    } catch (err) {
        loadingBar2.style.display = "none";
        return {
            status: "ERR",
            ok: false,
            body: err.message,
            headers: {}
        };
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
    
            <input class="formInput" type="text" id="commandName" placeholder="Command Name" required>

            <div>
                <select id="requestMethod" class="method-select">
                    <option value="GET" selected>GET</option>
                    <option value="POST">POST</option>
                    <option value="PUT">PUT</option>
                    <option value="DELETE">DELETE</option>
                    <option value="PATCH">PATCH</option>
                </select>

                <input style="border-radius:4px;width:80%;background-color:rgb(30,31,34);" 
                       type="text" id="commandURL" placeholder="URL">
            </div>

            <input type="text" id="commandCmd" placeholder="Command" required>

            <p class="dark-text" style="margin-bottom: 0px;">Headers</p>
            
            <div id="headersContainer">
                <div class="headerRow" style="margin-top: 0px;">
                    <input class="headersStyle headerKey" type="text" placeholder="Key">
                    <input class="headersStyle headerValue" type="text" placeholder="Value">
                    <button type="button" class="removeHeaderBtn">-</button>
                </div>
            </div>
            
            <button type="button" id="addHeaderBtn" class="addHeaderBtn">+</button>
            <p class="dark-text">Body</p>

            <textarea id="commandBody"
                placeholder="Command Body (optional)"
                style="border-radius:4px;background-color:rgb(30,31,34);color: #8ab23d;width:100%;height:200px;padding:6px;font-size:14px;box-sizing:border-box;">
            </textarea>

            <button type="submit" class="btn btn-primary"
                style="background-color:#238536;color:white;width:200px;">
                Add Command
            </button>
        </form>
    `;

    const headersContainer = document.getElementById("headersContainer");
    const addHeaderBtn = document.getElementById("addHeaderBtn");

    /* ---------------- ADD HEADER ROW ---------------- */

    addHeaderBtn.addEventListener("click", (e) => {
        e.preventDefault();

        const row = document.createElement("div");
        row.className = "headerRow";

        row.innerHTML = `
            <input class="headersStyle headerKey" type="text" placeholder="Key">
            <input class="headersStyle headerValue" type="text" placeholder="Value">
            <button type="button" class="removeHeaderBtn">-</button>
        `;

        headersContainer.appendChild(row);
    });

    /* ---------------- REMOVE HEADER ROW ---------------- */

    headersContainer.addEventListener("click", (e) => {
        if (e.target.classList.contains("removeHeaderBtn")) {
            e.preventDefault();

            if (headersContainer.children.length > 1) {
                e.target.parentElement.remove();
            }
        }
    });

    const commandForm = document.getElementById("commandForm");

    const methodSelect = document.getElementById("requestMethod");
    const urlInput = document.getElementById("commandURL");
    const commandInput = document.getElementById("commandCmd");

    /* ---------------- AUTO BUILD COMMAND ---------------- */

    function updateCommand() {
        const method = methodSelect.value;
        const url = urlInput.value.trim();

        commandInput.value = `-m ${method} -u ${url}`;
    }

    updateCommand();

    methodSelect.addEventListener("change", updateCommand);
    urlInput.addEventListener("input", updateCommand);

    /* ---------------- SUBMIT ---------------- */

    commandForm.addEventListener("submit", async (e) => {
        e.preventDefault();

        const name = document.getElementById("commandName").value.trim();
        const command = commandInput.value.trim();
        const bodyInput = document.getElementById("commandBody").value.trim();
        const body = bodyInput === "" ? null : bodyInput;

        /* -------- collect headers -------- */

        const headers = [];

        document.querySelectorAll(".headerRow").forEach(row => {
            const key = row.querySelector(".headerKey").value.trim();
            const value = row.querySelector(".headerValue").value.trim();

            if (key && value) {
                headers.push({ key, value });
            }
        });

        /* -------- create command first -------- */

        const createdCommand = await requestAddCommand({
            name,
            command,
            body,
            folderId
        });

        const commandId = createdCommand.id;

        /* -------- save headers -------- */

        for (const header of headers) {
            await fetch(BASE_URL + "/headers", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    commandId: commandId,
                    key: header.key,
                    value: header.value
                })
            });
        }

        console.log("Command and headers saved successfully");
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

        const createdCommand = await response.json(); // ← important

        await refreshSidebar();

        const main = document.getElementById("main");
        main.innerHTML = "";

        return createdCommand; // ← THIS FIXES EVERYTHING

    } catch (err) {
        console.error(err);
        alert("Error creating command: " + err.message);
    }
}
async function getHeadersForCommand(command) {
    const res = await fetch(`${BASE_URL}/headers/byCommand/${command.id}`);
    if (!res.ok) return [];

    const data = await res.json(); // ✅ parse first
    console.log("HEADERS DATA:", data); // ✅ log actual content

    return data;
}
async function renderCommand(command) {
    const main = document.getElementById("main");
    if (!main) return;

    main.innerHTML = `
    <div class="dark-main-content">
        <div style="display: flex;align-items: center">
            <h4
                class="dark-text"
                id="commandTitle"
                style="font-size: 20px; height: 15px; padding: 2px;"
                contenteditable="true"
                spellcheck="false"
            >${command.name}</h4>
            <button id="sendBtn" class="sendBtn">Send</button>
        </div>
        <hr style="border-color: #3a3a3a;">
        <p class="dark-text">Command</p>
        <textarea id="commandCmd" class="dark-pre">${command.command}</textarea>

        <p class="dark-text" style="margin-bottom: 0px; margin-top: 10px">Headers</p>
        <div id="headersContainer" style="margin-top: 5px"></div>
        <button id="addHeaderBtn" class="addHeaderBtn">+</button>

        <p class="dark-text">Body</p>
        <textarea id="commandBody" class="dark-body" style="min-height:120px;">${command.body ?? ""}</textarea>

        <div style="margin-top: 10px; margin-bottom: 10px; display: flex; align-items: center">
            <p class="dark-text" style="padding-bottom: 0px; margin-bottom: 0;">Response</p>
            <span id="responseNumber" style="background-color: #885ebb; color:white; border-radius: 4px;padding: 2px 4px; margin-left: 6px; font-size: 12px; font-weight: bold;">ready</span>
        </div>

        <div class="headers-toggle" style="cursor:pointer;"></div>
        <div class="headers-container" style="display:none; padding: 10px;"></div>

        <div style="width:100%; max-height:400px; overflow:auto; border-radius:4px; border:1px solid #3a3a3a; margin-top:10px;">
            <pre
                id="responseBody"
                class="dark-body"
                contenteditable
                style="
                    width:100%;
                    min-height:200px;
                    margin:0;
                    padding:10px;
                    background:#1e1e1e;
                    color:#abb2bf;
                    white-space: pre-wrap;
                    word-break: break-word;
                    resize: vertical;
                    overflow:auto;
                    border-radius:4px;
                    box-sizing:border-box;
                "
            >${highlightJson(command.response ?? '')}</pre>
        </div>
    </div>
`;

    const headersContainer = document.getElementById("headersContainer");
    const addHeaderBtn = document.getElementById("addHeaderBtn");
    const cmdInput = document.getElementById("commandCmd");
    const bodyInput = document.getElementById("commandBody");
    const sendBtn = document.getElementById("sendBtn");
    const responseBody = document.getElementById("responseBody");
    const headersToggle = document.querySelector(".headers-toggle");
    const headersDiv = document.querySelector(".headers-container");
    const responseNumber = document.getElementById("responseNumber");
    const commandTitle = document.getElementById("commandTitle");

    let responseHeaders = [];
    let open = false;

    // ----------------- SAVE COMMAND NAME / COMMAND / BODY -----------------
    const saveCommand = async (updatedFields) => {
        try {
            const res = await fetch(`${BASE_URL}/commands/${command.id}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ ...command, ...updatedFields })
            });
            if (res.ok) Object.assign(command, updatedFields);
        } catch (err) {
            console.error("Failed to save command:", err);
        }
    };

    commandTitle.addEventListener("blur", () => {
        const newName = commandTitle.textContent.trim();
        if (newName && newName !== command.name) saveCommand({ name: newName });
    });
    commandTitle.addEventListener("keydown", (e) => {
        if (e.key === "Enter") {
            e.preventDefault();
            commandTitle.blur();
        }
    });

    cmdInput.addEventListener("blur", () => {
        const newCmd = cmdInput.value.trim();
        if (newCmd !== command.command) saveCommand({ command: newCmd });
    });

    bodyInput.addEventListener("blur", () => {
        const newBody = bodyInput.value.trim();
        if (newBody !== command.body) saveCommand({ body: newBody });
    });

    // ----------------- HEADER MANAGEMENT -----------------
    const renderHeaderRow = (header) => {
        const row = document.createElement("div");
        row.className = "headerRow";
        row.style.display = "flex";
        row.style.gap = "6px";
        row.style.marginBottom = "4px";

        row.innerHTML = `
            <input style="height: 30px" class="headersStyle headerKey" type="text" placeholder="Key" value="${header?.key ?? ''}">
            <input style="height: 30px" class="headersStyle headerValue" type="text" placeholder="Value" value="${header?.value ?? ''}">
            <button type="button" class="removeHeaderBtn">-</button>
        `;
        headersContainer.appendChild(row);

        const keyInput = row.querySelector(".headerKey");
        const valueInput = row.querySelector(".headerValue");
        const removeBtn = row.querySelector(".removeHeaderBtn");

        const saveHeader = async () => {
            const key = keyInput.value.trim();
            const value = valueInput.value.trim();
            if (key && value) {
                if (header?.id) {
                    // Update existing
                    await fetch(`${BASE_URL}/headers/${header.id}`, {
                        method: "PUT",
                        headers: { "Content-Type": "application/json" },
                        body: JSON.stringify({ ...header, key, value })
                    });
                    header.key = key;
                    header.value = value;
                } else {
                    // Create new
                    const res = await fetch(`${BASE_URL}/headers`, {
                        method: "POST",
                        headers: { "Content-Type": "application/json" },
                        body: JSON.stringify({ commandId: command.id, key, value })
                    });
                    const createdHeader = await res.json();
                    row.remove();
                    renderHeaderRow(createdHeader);
                }
            }
        };

        keyInput.addEventListener("blur", saveHeader);
        valueInput.addEventListener("blur", saveHeader);
        removeBtn.addEventListener("click", async () => {
            if (header?.id) await fetch(`${BASE_URL}/headers/${header.id}`, { method: "DELETE" });
            row.remove();
        });
    };

    // Load existing headers
    const headersOnCommand = await getHeadersForCommand(command);
    headersOnCommand.forEach(renderHeaderRow);

    addHeaderBtn.addEventListener("click", (e) => {
        e.preventDefault();
        renderHeaderRow();
    });

    function updateHeadersToggle() {
        const count = responseHeaders.length;
        const color = count > 0 ? "#36ca53" : "white";
        const arrow = open ? "bi-chevron-down" : "bi-chevron-right";
        headersToggle.innerHTML = `<i class="bi ${arrow}"></i> Headers <span style="color:${color};">(${count})</span>`;
    }

    headersToggle.addEventListener("click", () => {
        open = !open;
        headersDiv.style.display = open ? "block" : "none";
        updateHeadersToggle();
    });

    // ----------------- SEND COMMAND -----------------
    sendBtn.addEventListener("click", async () => {
        const { url } = parseCommand(cmdInput.value);
        if (!url) {
            responseNumber.textContent = "ERR";
            responseNumber.style.backgroundColor = "#da3633";
            responseBody.innerHTML = highlightJson("Invalid command: Missing URL (-u)");
            return;
        }

        const headers = {};
        document.querySelectorAll(".headerRow").forEach(row => {
            const key = row.querySelector(".headerKey").value.trim();
            const value = row.querySelector(".headerValue").value.trim();
            if (key) headers[key] = value;
        });

        const body = bodyInput.value.trim() || "";

        try {
            const result = await executeCommand({ command: cmdInput.value, body, id: command.id });

            let displayBody;
            try {
                const parsed = typeof result.body === "string" ? JSON.parse(result.body) : result.body;
                displayBody = JSON.stringify(parsed, null, 2);
            } catch {
                displayBody = result.body;
            }

            responseBody.innerHTML = highlightJson(displayBody);

            responseNumber.textContent = result.status;
            const bgColor =
                result.status >= 200 && result.status < 300 ? "#238636" :
                    result.status >= 400 && result.status < 500 ? "#d29922" :
                        result.status >= 500 ? "#da3633" :
                            result.status === "ERR" ? "#da3633" : "#6e7681";
            responseNumber.style.backgroundColor = bgColor;

            headersDiv.innerHTML = "";
            responseHeaders = result.headers ? Object.entries(result.headers) : [];
            if (responseHeaders.length > 0) {
                responseHeaders.forEach(([key, value]) => {
                    const row = document.createElement("div");
                    row.style.display = "flex";
                    row.style.gap = "6px";
                    row.style.marginBottom = "4px";

                    const keyInput = document.createElement("input");
                    keyInput.value = key;
                    keyInput.readOnly = true;
                    keyInput.className = "dark-pre";
                    keyInput.style.flex = "1";
                    keyInput.style.color = "white";

                    const valueInput = document.createElement("input");
                    valueInput.value = value;
                    valueInput.readOnly = true;
                    valueInput.className = "dark-pre";
                    valueInput.style.flex = "2";
                    valueInput.style.color = "white";

                    row.appendChild(keyInput);
                    row.appendChild(valueInput);
                    headersDiv.appendChild(row);
                });
            } else {
                const empty = document.createElement("div");
                empty.textContent = "No headers";
                empty.style.color = "#6e7681";
                headersDiv.appendChild(empty);
            }

            updateHeadersToggle();

        } catch (err) {
            responseBody.innerHTML = highlightJson(err.message);
            responseNumber.textContent = "ERR";
            responseNumber.style.backgroundColor = "#da3633";
            responseHeaders = [];
            updateHeadersToggle();
        }
    });

    responseHeaders = [];
    updateHeadersToggle();
}
function parseCommand(cmd) {
    const methodMatch = cmd.match(/-m\s+(\w+)/i);
    const urlMatch = cmd.match(/-u\s+([^\s]+)/i);

    return {
        method: methodMatch ? methodMatch[1].toUpperCase() : "GET",
        url: urlMatch ? urlMatch[1] : null
    };
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
            <input class="formInput" type="text" id="folderName" placeholder="Folder Name" required>
            <button type="submit" class="btn btn-primary" 
                style="background-color: #238536; color: white; width: 200px;">
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