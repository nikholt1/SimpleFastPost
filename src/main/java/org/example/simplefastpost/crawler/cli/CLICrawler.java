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

    private static CrawlerSession session;
    private static final Set<String> visited = new LinkedHashSet<>();
    private static final Map<String, Set<String>> pageToEndpoints = new LinkedHashMap<>();
    private static final Pattern fetchPattern =
            Pattern.compile("fetch\\(([^)]*)\\)", Pattern.DOTALL);

    public CLICrawler(CLICrawlerService cliCrawlerService, CLICrawlerRepository cliCrawlerRepository) {

        this.cliCrawlerRepository = cliCrawlerRepository;
        this.cliCrawlerService = cliCrawlerService;
    }

    public void run(String baseUrl) {
        // Create DB session
        session = cliCrawlerService.createSession(baseUrl);

        // Start crawling
        crawl(baseUrl, null, 1);
        printAsciiWebGraph();
    }

    private void crawl(String url, CrawlerEntity parentEntity, int depth) {
        if (visited.contains(url) || depth > 3) return;

        try {
            visited.add(url);
            pageToEndpoints.putIfAbsent(url, new LinkedHashSet<>());
            System.out.println("Visiting: " + url);

            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (compatible; CLICrawler/1.0)")
                    .timeout(10000)
                    .get();

            // Create entity for the current page
            CrawlerEntity currentPageEntity = cliCrawlerService.createEntity(
                    session,
                    parentEntity,
                    url,                   // title = url
                    "PAGE",
                    url,
                    new ArrayList<>(),     // connections filled later
                    null,                  // method N/A for pages
                    null,                  // response N/A
                    0
            );

            // 1️⃣ Collect all links on the current page first
            Set<String> pageLinks = new LinkedHashSet<>();
            Elements links = doc.select("a[href]");
            for (Element link : links) {
                String nextUrl = link.absUrl("href");
                if (!nextUrl.isEmpty() && !nextUrl.startsWith("#") &&
                        !nextUrl.startsWith("mailto:") && !nextUrl.startsWith("javascript:")) {
                    pageLinks.add(nextUrl);
                    pageToEndpoints.get(url).add("PAGE: " + nextUrl); // record page
                    System.out.println("Found webpageUrl: " + nextUrl);
                }
            }

            // Save connections (links) for this page
            List<String> connections = new ArrayList<>();
            for (String linkUrl : pageLinks) connections.add(linkUrl);
            currentPageEntity.setConnections(connections);
            cliCrawlerService.updateEntity(currentPageEntity);

            // 2️⃣ Scan scripts for fetch endpoints
            Elements scripts = doc.select("script");
            for (Element script : scripts) {
                scanFetchEndpoints(script.html(), url, currentPageEntity);

                String src = script.absUrl("src");
                if (!src.isEmpty()) {
                    try {
                        String jsContent = Jsoup.connect(src)
                                .userAgent("Mozilla/5.0 (compatible; CLICrawler/1.0)")
                                .ignoreContentType(true)
                                .timeout(10000)
                                .execute()
                                .body();
                        scanFetchEndpoints(jsContent, url, currentPageEntity);
                    } catch (Exception e) {
                        System.out.println("Failed to fetch JS: " + src + " (" + e.getMessage() + ")");
                    }
                }
            }

            // 3️⃣ Recursively crawl all the links found on this page
            for (String nextUrl : pageLinks) {
                crawl(nextUrl, currentPageEntity, depth + 1);
            }

        } catch (Exception e) {
            System.out.println("Failed: " + url + " (" + e.getMessage() + ")");
        }
    }

    private void scanFetchEndpoints(String js, String pageUrl, CrawlerEntity parentEntity) {
        Matcher matcher = fetchPattern.matcher(js);

        while (matcher.find()) {
            String fetchCall = matcher.group(1);

            String endpoint = extractUrl(fetchCall);
            String method = extractMethod(fetchCall);

            if (endpoint != null) {
                endpoint = resolveUrl(pageUrl, endpoint);

                pageToEndpoints
                        .computeIfAbsent(pageUrl, k -> new LinkedHashSet<>())
                        .add(method + " " + endpoint);

                System.out.println("Found endpoint on page [" + pageUrl + "]: " + method + " " + endpoint);

                // Save endpoint as entity
                cliCrawlerService.createEntity(
                        session,
                        parentEntity,
                        endpoint,
                        "API",
                        endpoint,
                        new ArrayList<>(), // endpoints don’t have connections
                        method,
                        null,  // response unknown
                        0
                );
            }
        }
    }

    private static String extractUrl(String fetchCall) {
        Matcher m = Pattern.compile("([\"'`])([^\"'`]+)\\1").matcher(fetchCall);
        if (m.find()) return m.group(2);
        return null;
    }

    private static String extractMethod(String fetchCall) {
        Matcher m = Pattern.compile("method\\s*:\\s*[\"'](GET|POST|PUT|DELETE|PATCH)[\"']",
                Pattern.CASE_INSENSITIVE).matcher(fetchCall);
        if (m.find()) return m.group(1).toUpperCase();
        return "GET";
    }

    private static String resolveUrl(String base, String endpoint) {
        try {
            return new java.net.URL(new java.net.URL(base), endpoint).toString();
        } catch (Exception e) {
            return endpoint;
        }
    }

    private static void printAsciiWebGraph() {
        System.out.println("\n===== DATA =====");
        for (String page : pageToEndpoints.keySet()) {
            System.out.println(page);

            // Separate pages from API endpoints
            Set<String> pages = new LinkedHashSet<>();
            Set<String> endpoints = new LinkedHashSet<>();

            for (String entry : pageToEndpoints.get(page)) {
                if (entry.startsWith("PAGE: ")) pages.add(entry.substring(6));
                else endpoints.add(entry);
            }

            // Print linked pages first
            for (String linkedPage : pages) {
                System.out.println("|_ PAGE: " + linkedPage);
            }

            // Print endpoints after
            for (String endpoint : endpoints) {
                System.out.println("|_ " + endpoint);
            }
            System.out.println();
        }
    }
}