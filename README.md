<p align="center">
  <img src="https://github.com/user-attachments/assets/09bd291d-6225-42b6-94ce-89d3fcc35536" width="100" alt="SFP Logo" />
</p>

<h1 align="center">SimpleFastPost</h1>

<img width="100%"  alt="image" src="https://github.com/user-attachments/assets/152e5298-4f1d-43e4-b6d4-1ca720641c44" />


# SimpleFastPost

SimpleFastPost started as a learning project to better understand how tools like Postman and Bruno work, while exploring system development, design, and HTTP communication.

It combines a CLI-based web crawler, an HTTP request tester, persistent request management, and an integrated user interface.

## Table of Contents

- [Features](#features)
- [How It Works](#how-it-works)
- [Motivation](#motivation)
- [CLI](#cli)
    - [CLI Features](#cli-features)
    - [Example Commands](#example-commands)
    - [HTTP Requests and Saved Commands](#http-requests-and-saved-commands)
    - [Folders and Headers](#folders-and-headers)
    - [Request History](#request-history)
    - [Web Crawling](#web-crawling)
    - [Synchronization](#synchronization)
    - [API Server](#api-server)
    - [Maintenance and Help](#maintenance-and-help)
- [Technical Overview](#technical-overview)
- [Web crawler](#web-crawler)
## Features

- **Web crawling** — Crawl websites, inspect HTML pages, discover links, and collect information about website structures.
- **HTTP request testing** — Send HTTP requests and inspect server responses, inspired by tools such as Postman and Bruno.
- **Website structure discovery** — Collect and save information about discovered pages and their relationships.
- **Local persistence** — Use SQLite to store application data and crawl results.
- **Saved request management** — Store, organize, update, and reuse HTTP requests.
- **Integrated user interface** — Interact with the application's functionality through its UI alongside the CLI workflow.
- **API server** — Run the application locally and access it through `http://localhost:8080`.

## How It Works

SimpleFastPost combines website crawling and HTTP request testing in one application.

The crawler visits pages, examines their HTML, and discovers links to other pages. The collected information can be stored locally, allowing crawl sessions and website structures to persist for later inspection.

The HTTP request tester sends requests to web servers and displays their responses. Requests can be saved as reusable commands, organized into folders, and executed individually or as a collection.

The application also provides an API server for local access and utilities for managing request definitions, execution history, and crawl sessions.

## Motivation

The project was developed to gain a deeper understanding of the concepts behind existing developer tools by building a simplified implementation from scratch.

Rather than relying entirely on established tools, SimpleFastPost provides an opportunity to explore how web crawling, HTTP communication, HTML processing, data persistence, and application interfaces can be combined into a single application.

## CLI

SimpleFastPost includes a command-line interface for HTTP request testing, web crawling, request management, and API process control.

### CLI Features

- **HTTP request testing** — Send requests using configurable HTTP methods and URLs.
- **Saved requests** — Store requests as reusable commands.
- **Folder organization** — Group related requests into folders.
- **Interactive request configuration** — Enter request bodies and headers when needed.
- **Web crawling** — Crawl websites and inspect saved crawl sessions.
- **Crawl-to-command conversion** — Generate reusable HTTP commands from crawl results.
- **Request synchronization** — Transfer request definitions through an `sfp.http` file.
- **History management** — Review and clear request history.
- **API process management** — Start the API server and manage running API processes.

### Example Commands

```bash
# Send an HTTP GET request
sfp -m GET -u https://example.com

# Save a POST request
sfp -m POST -u https://api.example.com/users -cmd createUser -sv

# List saved requests and folders
sfp ls

# Create a folder
sfp mkdir -f users

# Execute a saved request
sfp run -scmd createUser

# Execute all requests in a folder
sfp run -f users

# Crawl a website
sfp crawl https://example.com

# View crawl history
sfp crawl history

# View request history
sfp history

# Synchronize request definitions
sfp push
sfp sync

# Display CLI help and version
sfp --help
sfp --version
```

### HTTP Requests and Saved Commands

| Command | Description |
|---|---|
| `sfp -m <method> -u <url>` | Send an HTTP request using the specified method and URL. |
| `-sv` | Save the configured request. |
| `-cmd <name>` | Specify a name for a saved command. |
| `-scmd <name>` | Select a saved command for execution. |
| `-f <folder>` | Specify a folder for request organization or execution. |
| `sfp run -scmd <name>` | Execute a saved command. |
| `sfp run -f <folder>` | Execute requests in a folder. |
| `-t` | Enable test mode when running requests through the request controller. |

For methods such as POST, PUT, PATCH, and DELETE, the CLI supports interactive request-body input. Headers can also be configured interactively.

### Folders and Headers

| Command | Description |
|---|---|
| `sfp ls` | List saved folders and commands. |
| `sfp ls -f <folder>` | List commands in a specific folder. |
| `sfp mkdir -f <folder>` | Create a folder. |
| `sfp update command <name> --set <field>:<value>` | Update a saved command field. |
| `sfp update folder <name>` | Update a folder name. |
| `sfp delete -cmd <name>` | Delete a saved command. |
| `sfp delete -cmd <name> header <headerName>` | Delete a header from a saved command. |
| `sfp delete -f <folder>` | Delete a folder. |

The update functionality also supports changes to request bodies and command details. Header updates use an interactive workflow.

### Request History

SimpleFastPost records request-related activity so previous operations can be reviewed.

| Command | Description |
|---|---|
| `sfp history` | Display request history. |
| `sfp history reverse` | Display history in reverse order. |
| `sfp history purge` | Clear request history. |
| `sfp purge` | Clear all folders, saved commands, headers, and request history. |

**Note:** `sfp purge` is destructive. Crawl history is managed separately.

### Web Crawling

The crawler discovers pages and connections within a website and stores crawl sessions for later inspection.

| Command | Description |
|---|---|
| `sfp crawl <url>` | Start crawling a website. |
| `sfp crawl history` | List or inspect recent crawl history. |
| `sfp crawl history <sessionId>` | Inspect a specific crawl session. |
| `sfp crawl history purge` | Clear stored crawl history. |
| `sfp crawl delete <sessionId>` | Delete a stored crawl session. |
| `sfp crawl build <id> <name>` | Convert a crawl session into reusable HTTP commands. |

Crawl results include discovered entities and connections. The conversion command provides a way to turn discovered website structures into reusable requests.

### Synchronization

SimpleFastPost supports synchronization of request definitions through an `sfp.http` file in the current working directory.

| Command | Description |
|---|---|
| `sfp push` | Run the push workflow using `sfp.http`. |
| `sfp sync` | Run the synchronization workflow using `sfp.http`. |

The exact direction of data transfer depends on the implementation of the corresponding services.

### API Server

SimpleFastPost can run its API server as a background process, allowing the application to remain available while the terminal is used for other tasks.

- **Local access:** `http://localhost:8080`
- **Detached execution:** The CLI documentation includes a `-d` option for detached execution.
- **Process listing:** `sfp api ls`
- **Process termination:** `sfp api delete <pid>` or `sfp api kill <pid>`

Start the API server using the project's supported API startup command. The exact startup syntax should be confirmed against the current CLI implementation before documenting it here.

### Maintenance and Help

| Command | Description |
|---|---|
| `sfp --help` | Display CLI usage information. |
| `sfp --version` | Display the application version. |
| `sfp purge` | Remove saved folders, commands, headers, and request history. |
| `sfp api ls` | List running API processes. |
| `sfp api delete <pid>` | Stop an API process by its process ID. |
| `sfp api kill <pid>` | Alias for stopping an API process. |

The version output currently identifies the application as `0.0.1 alpha`.

## Technical Overview

The CLI acts as the entry point for request management and crawling workflows. It parses command-line arguments and delegates operations to dedicated services, controllers, and repositories.

The application uses a persistence layer for saved commands, folders, headers, execution history, and crawl sessions. SQLite provides local data storage, while the API server makes application functionality accessible locally.

SimpleFastPost is a practical exploration of HTTP communication, web crawling, persistence, command-line tooling, and API development.


## Web Crawler

SimpleFastPost includes a recursive web crawler built with Java, Jsoup, and a persistence layer for storing crawl sessions and discovered entities.

Unlike a basic crawler that only collects links, the crawler also scans JavaScript for `fetch()` calls to identify potential API endpoints used by a website.

### Features

- **Recursive crawling** — Visit discovered pages and follow their links, with a maximum crawl depth of three.
- **HTML parsing** — Use Jsoup to parse HTML documents and extract links from anchor elements.
- **JavaScript inspection** — Scan inline scripts and externally referenced JavaScript files for `fetch()` calls.
- **API endpoint discovery** — Extract endpoint URLs and HTTP methods, including GET, POST, PUT, DELETE, and PATCH.
- **URL resolution** — Resolve relative page links and API endpoints against the page URL.
- **Variable resolution** — Attempt to resolve JavaScript variables and `${...}` placeholders in discovered endpoint URLs.
- **Relationship tracking** — Associate discovered pages with their parent pages and attach API entities to the pages where they were found.
- **Persistent crawl sessions** — Store discovered entities and relationships for later inspection.
- **Console visualization** — Print a tree-like text representation of discovered pages and endpoints.
- **Progress logging** — Report visited pages, discovered endpoints, and crawl failures through a logging callback.

### How It Works

The crawler starts a new crawl session for a supplied base URL and recursively processes pages.

1. **Fetch the page:** Jsoup retrieves the HTML document using a configured user agent and a ten-second timeout.
2. **Discover links:** Extract anchor links and collect URLs that can be followed.
3. **Inspect JavaScript:** Scan inline scripts and attempt to retrieve external JavaScript files.
4. **Identify API endpoints:** Search for `fetch()` calls, extract their URL arguments, and infer their HTTP methods.
5. **Create entities:** Store pages as `PAGE` entities and discovered API endpoints as `API` entities.
6. **Build relationships:** Associate pages with their parent pages and attach API entities to the pages that reference them.
7. **Continue crawling:** Recursively visit discovered pages until the depth limit is reached or no further eligible pages remain.
8. **Display the results:** Print the collected page-to-link and page-to-endpoint information as a console graph.

### Example

Running the crawler through the CLI:

```bash
sfp crawl https://example.com
```

The crawler logs its progress and produces output conceptually similar to:

```text
===== DATA =====
https://example.com
|_ PAGE: https://example.com/about
|_ PAGE: https://example.com/contact
|_ GET https://example.com/api/users

https://example.com/about
|_ PAGE: https://example.com/team
```

*Example output for illustration; actual results depend on the website.*

### Technical Details

The crawler uses Java collections to track visited URLs, discovered connections, and API endpoints while preserving insertion order. A visited set helps prevent repeatedly processing the same URL during a session.

Crawl sessions and entities are managed through `CLICrawlerService` and `CLICrawlerRepository`, keeping crawling logic separate from persistence operations.

The current implementation uses a depth limit of three and a ten-second connection timeout. Endpoint discovery is based on regular-expression scanning rather than full JavaScript parsing, so it works best with relatively straightforward `fetch()` calls.

### Limitations

- **Static HTML and scripts:** The crawler does not execute JavaScript or render pages in a browser. Links and API calls created dynamically at runtime may therefore be missed.
- **Pattern-based endpoint detection:** Complex `fetch()` expressions, template literals, imported configuration values, and other JavaScript patterns may not be detected or resolved correctly.
- **Relative links and scope:** The crawler currently follows discovered links without restricting them to the original domain. External websites may also be crawled, subject to the depth limit.
- **Duplicate URLs:** URLs are tracked as strings, so variations such as trailing slashes, query parameters, and fragments may be treated as distinct URLs.
- **API discovery is not API execution:** Identifying a `fetch()` call does not mean the endpoint has been tested or that its response has been retrieved.
- **Network restrictions:** Timeouts, unavailable resources, access controls, and server errors can prevent pages or scripts from being retrieved.

The crawler is designed as a practical exploration of website discovery, API endpoint inspection, graph-like relationships, and persistent web data.
