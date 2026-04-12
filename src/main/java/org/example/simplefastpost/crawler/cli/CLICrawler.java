package org.example.simplefastpost.crawler.cli;
import org.example.simplefastpost.model.CrawlerEntity;
import org.example.simplefastpost.model.CrawlerSession;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.*;
import java.util.regex.*;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

public class CLICrawler {

    private final CLICrawlerRepository cliCrawlerRepository;
    private final CLICrawlerService cliCrawlerService;

    private CrawlerSession session; // non-static now
    private final Set<String> visited = new LinkedHashSet<>();
    private final Map<String, Set<String>> pageToEndpoints = new LinkedHashMap<>();
    private static final Pattern fetchPattern = Pattern.compile("fetch\\(([^)]*)\\)", Pattern.DOTALL);

    public CLICrawler(CLICrawlerService cliCrawlerService, CLICrawlerRepository cliCrawlerRepository) {
        this.cliCrawlerRepository = cliCrawlerRepository;
        this.cliCrawlerService = cliCrawlerService;
    }

    public void run(String baseUrl) {
        if (baseUrl == null || baseUrl.isEmpty()) {
            throw new IllegalArgumentException("Base URL must not be null or empty");
        }

        session = cliCrawlerService.createSession(baseUrl);
        System.out.println("\u001B[48;5;129m" + "Starting crawl session on: " + baseUrl + " Session ID: " + session.getId() + "\u001B[0m");


        if (session == null) throw new RuntimeException("\u001B[48;5;52m" + "Failed to create crawler session."+ "\u001B[0m");

        crawl(baseUrl, null, 1);
        printAsciiWebGraph();
    }

    private void crawl(String url, CrawlerEntity parentEntity, int depth) {
        if (visited.contains(url) || depth > 3) return;

        try {
            visited.add(url);
            pageToEndpoints.putIfAbsent(url, new LinkedHashSet<>());
            System.out.println("\u001B[97;48;5;19m" + "Visiting: " + url + "\u001B[0m");

            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (compatible; CLICrawler/1.0)")
                    .timeout(10000)
                    .get();

            // 1️⃣ Collect page links
            Set<String> pageLinks = new LinkedHashSet<>();
            Elements links = doc.select("a[href]");
            for (Element link : links) {
                String nextUrl = link.absUrl("href");
                if (!nextUrl.isEmpty() && !nextUrl.startsWith("#") &&
                        !nextUrl.startsWith("mailto:") && !nextUrl.startsWith("javascript:")) {
                    pageLinks.add(nextUrl);
                    pageToEndpoints.get(url).add("PAGE: " + nextUrl);
                    System.out.println("Found webpageUrl: " + nextUrl);
                }
            }

            // 2️⃣ Scan scripts for fetch endpoints
            Set<CrawlerEntity> apiEndpoints = new LinkedHashSet<>();
            Elements scripts = doc.select("script");
            for (Element script : scripts) {
                apiEndpoints.addAll(scanFetchEndpoints(script.html(), url));
                String src = script.absUrl("src");
                if (!src.isEmpty()) {
                    try {
                        String jsContent = Jsoup.connect(src)
                                .userAgent("Mozilla/5.0 (compatible; CLICrawler/1.0)")
                                .ignoreContentType(true)
                                .timeout(10000)
                                .execute()
                                .body();
                        apiEndpoints.addAll(scanFetchEndpoints(jsContent, url));
                    } catch (Exception e) {
                        System.out.println("\u001B[48;5;52m" + "Failed to fetch JS: " + src + " (" + e.getMessage() + ")" + "\u001B[0m");
                    }
                }
            }

            // 3️⃣ Create page entity with all unique connections
            Set<String> connectionsSet = new LinkedHashSet<>(pageLinks);
            for (CrawlerEntity api : apiEndpoints) connectionsSet.add(api.getUrl()); // deduplicate

            List<String> connections = new ArrayList<>(connectionsSet);

            CrawlerEntity currentPageEntity = cliCrawlerService.createEntity(
                    session,
                    parentEntity,
                    url,
                    "PAGE",
                    url,
                    connections,
                    null,
                    null,
                    0
            );

            // 4️⃣ Set parent for all API endpoints
            for (CrawlerEntity api : apiEndpoints) {
                api.setParent(currentPageEntity);
                cliCrawlerService.updateEntity(api);
            }

            // 5️⃣ Recursively crawl all page links
            for (String nextUrl : pageLinks) {
                crawl(nextUrl, currentPageEntity, depth + 1);
            }

        } catch (Exception e) {
            System.out.println("\u001B[48;5;52m" + "Failed: " + url + " (" + e.getMessage() + ")"+ "\u001B[0m");
        }
    }

    private Set<CrawlerEntity> scanFetchEndpoints(String js, String pageUrl) {
        Set<CrawlerEntity> endpoints = new LinkedHashSet<>();

        // 🆕 Extract const/let/var variables
        Map<String, String> variables = new HashMap<>();

// Match const/let/var assignments (strings, template literals, simple values)
        Matcher varMatcher = Pattern.compile(
                "(?:const|let|var)\\s+(\\w+)\\s*=\\s*(?:([\"'`])([^\"'`]+)\\2|([^;\\n]+))"
        ).matcher(js);

        while (varMatcher.find()) {
            String name = varMatcher.group(1);
            String value;

            if (varMatcher.group(3) != null) {
                // مستقیم string: "value"
                value = varMatcher.group(3);
            } else {
                // fallback: raw expression (e.g. startUrl + "/api")
                value = varMatcher.group(4).trim();
            }

            variables.put(name, value);
        }

        Matcher matcher = fetchPattern.matcher(js);

        while (matcher.find()) {
            String fetchCall = matcher.group(1);
            String endpoint = extractUrl(fetchCall);
            String method = extractMethod(fetchCall);

            if (endpoint != null) {
                endpoint = resolvePlaceholders(resolveUrl(pageUrl, endpoint), variables);
                pageToEndpoints.computeIfAbsent(pageUrl, k -> new LinkedHashSet<>())
                        .add(method + " " + endpoint);

                System.out.println("Found endpoint on page [" + pageUrl + "]: " + method + " " + endpoint);

                CrawlerEntity apiEntity = cliCrawlerService.createEntity(
                        session,
                        null,
                        endpoint,
                        "API",
                        endpoint,
                        new ArrayList<>(),
                        method,
                        null,
                        0
                );
                endpoints.add(apiEntity);
            }
        }
        return endpoints;
    }

    // Replaces dynamic placeholders like ${BASE_URL}, ${id}, ${commandId} safely
    private static String resolvePlaceholders(String url, Map<String, String> variables) {
        if (url == null) return null;

        Matcher matcher = Pattern.compile("\\$\\{([^}]+)}").matcher(url);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String expr = matcher.group(1).trim();

            String replacement = variables.get(expr);

            if (replacement == null) {
                // Try simple fallback: strip unknown but keep path structure
                replacement = "";
            }

            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(sb);

        String result = sb.toString();

        // Optional cleanup: collapse double slashes (except protocol)
        result = result.replaceAll("(?<!:)//+", "/");

        return result;
    }

    private static String extractUrl(String fetchCall) {
        Matcher m = Pattern.compile("([\"'`])([^\"'`]+)\\1").matcher(fetchCall);
        return m.find() ? m.group(2) : null;
    }

    private static String extractMethod(String fetchCall) {
        Matcher m = Pattern.compile("method\\s*:\\s*[\"'](GET|POST|PUT|DELETE|PATCH)[\"']",
                Pattern.CASE_INSENSITIVE).matcher(fetchCall);
        return m.find() ? m.group(1).toUpperCase() : "GET";
    }

    private static String resolveUrl(String base, String endpoint) {
        try {
            return new java.net.URL(new java.net.URL(base), endpoint).toString();
        } catch (Exception e) {
            return endpoint;
        }
    }

    private void printAsciiWebGraph() {
        System.out.println("\n===== DATA =====");
        for (String page : pageToEndpoints.keySet()) {
            System.out.println(page);

            Set<String> pages = new LinkedHashSet<>();
            Set<String> endpoints = new LinkedHashSet<>();
            for (String entry : pageToEndpoints.get(page)) {
                if (entry.startsWith("PAGE: ")) pages.add(entry.substring(6));
                else endpoints.add(entry);
            }

            for (String linkedPage : pages) System.out.println("|_ PAGE: " + linkedPage);
            for (String endpoint : endpoints) System.out.println("|_ " + endpoint);
            System.out.println();
        }
    }
}