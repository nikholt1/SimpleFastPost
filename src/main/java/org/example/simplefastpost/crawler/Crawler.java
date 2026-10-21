package org.example.simplefastpost.crawler;


import org.example.simplefastpost.cli.config.CLIconfig;
import org.example.simplefastpost.crawler.cli.CLICrawlerRepository;
import org.example.simplefastpost.crawler.cli.CLICrawlerService;
import org.example.simplefastpost.model.CrawlerEntity;
import org.example.simplefastpost.model.CrawlerSession;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class Crawler {

    private final CLICrawlerService cliCrawlerService;
    private final CLICrawlerRepository crawlerRepository;
    private CrawlerSession session;
    CLIconfig cliConfig = new CLIconfig();

    private final Set<String> visited = new LinkedHashSet<>();
    private final Map<String, Set<String>> pageToEndpoints = new LinkedHashMap<>();
    private final Pattern fetchPattern = Pattern.compile("fetch\\(([^)]*)\\)", Pattern.DOTALL);

    public Crawler() throws Exception {
        this.crawlerRepository = new CLICrawlerRepository(cliConfig.getDBUrl());
        this.cliCrawlerService = new CLICrawlerService(crawlerRepository);
    }

    public void run(String baseUrl, Consumer<String> log) {
        visited.clear();
        pageToEndpoints.clear();

        session = cliCrawlerService.createSession(baseUrl);

        if (session == null) {
            throw new RuntimeException("Failed to create crawler session.");
        }

        log.accept("Starting crawl session: " + session.getId());

        crawl(baseUrl, null, 1, log);

        printAsciiWebGraph(log);
    }

    private void crawl(String url, CrawlerEntity parentEntity, int depth, Consumer<String> log) {
        if (visited.contains(url) || depth > 3) return;

        try {
            visited.add(url);
            pageToEndpoints.putIfAbsent(url, new LinkedHashSet<>());

            log.accept("Visiting: " + url);

            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (compatible; CLICrawler/1.0)")
                    .timeout(10000)
                    .get();

            // -------------------------
            // 1. Extract page links
            // -------------------------
            Set<String> pageLinks = new LinkedHashSet<>();
            Elements links = doc.select("a[href]");

            for (Element link : links) {
                String nextUrl = link.absUrl("href");

                if (nextUrl.isEmpty()
                        || nextUrl.startsWith("#")
                        || nextUrl.startsWith("mailto:")
                        || nextUrl.startsWith("javascript:")) {
                    continue;
                }

                pageLinks.add(nextUrl);
                pageToEndpoints.get(url).add("PAGE: " + nextUrl);
            }

            // -------------------------
            // 2. Extract API endpoints
            // -------------------------
            Set<CrawlerEntity> apiEntities = new LinkedHashSet<>();
            Map<String, String> variables = new HashMap<>();

            Elements scripts = doc.select("script");

            for (Element script : scripts) {
                apiEntities.addAll(scanFetchEndpoints(script.html(), url, variables, log));

                String src = script.absUrl("src");
                if (!src.isEmpty()) {
                    try {
                        String jsContent = Jsoup.connect(src)
                                .userAgent("Mozilla/5.0 (compatible; CLICrawler/1.0)")
                                .ignoreContentType(true)
                                .timeout(10000)
                                .execute()
                                .body();

                        apiEntities.addAll(scanFetchEndpoints(jsContent, url, variables, log));

                    } catch (Exception e) {
                        log.accept("Failed to fetch JS: " + src + " (" + e.getMessage() + ")");
                    }
                }
            }

            // -------------------------
            // 3. Build connections list
            // -------------------------
            Set<String> connectionsSet = new LinkedHashSet<>(pageLinks);

            for (CrawlerEntity api : apiEntities) {
                connectionsSet.add(api.getUrl());
            }

            List<String> connections = new ArrayList<>(connectionsSet);

            // -------------------------
            // 4. Create PAGE entity
            // -------------------------
            CrawlerEntity pageEntity = cliCrawlerService.createEntity(
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

            // -------------------------
            // 5. Attach API entities to page
            // -------------------------
            for (CrawlerEntity api : apiEntities) {
                api.setParent(pageEntity);
                cliCrawlerService.updateEntity(api);
            }

            // -------------------------
            // 6. Recursively crawl pages
            // -------------------------
            for (String nextUrl : pageLinks) {
                crawl(nextUrl, pageEntity, depth + 1, log);
            }

        } catch (Exception e) {
            log.accept("Failed: " + url + " (" + e.getMessage() + ")");
        }
    }

    // -------------------------
    // Fetch scanner (same as v1 logic)
    // -------------------------
    private Set<CrawlerEntity> scanFetchEndpoints(
            String js,
            String pageUrl,
            Map<String, String> variables,
            Consumer<String> log
    ) {
        Set<CrawlerEntity> endpoints = new LinkedHashSet<>();

        // variable extraction
        Matcher varMatcher = Pattern.compile(
                "(?:const|let|var)\\s+(\\w+)\\s*=\\s*(?:([\"'`])([^\"'`]+)\\2|([^;\\n]+))"
        ).matcher(js);

        while (varMatcher.find()) {
            String name = varMatcher.group(1);

            String value = (varMatcher.group(3) != null)
                    ? varMatcher.group(3)
                    : varMatcher.group(4).trim();

            variables.put(name, value);
        }

        Matcher matcher = fetchPattern.matcher(js);

        while (matcher.find()) {
            String fetchCall = matcher.group(1);

            String endpoint = extractUrl(fetchCall);
            String method = extractMethod(fetchCall);

            if (endpoint != null) {
                endpoint = resolvePlaceholders(
                        resolveUrl(pageUrl, endpoint),
                        variables
                );

                pageToEndpoints
                        .computeIfAbsent(pageUrl, k -> new LinkedHashSet<>())
                        .add(method + " " + endpoint);

                log.accept("Found endpoint [" + pageUrl + "]: " + method + " " + endpoint);

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

    // -------------------------
    // helpers
    // -------------------------
    private String extractUrl(String fetchCall) {
        Matcher m = Pattern.compile("([\"'`])([^\"'`]+)\\1").matcher(fetchCall);
        return m.find() ? m.group(2) : null;
    }

    private String extractMethod(String fetchCall) {
        Matcher m = Pattern.compile(
                "method\\s*:\\s*[\"'](GET|POST|PUT|DELETE|PATCH)[\"']",
                Pattern.CASE_INSENSITIVE
        ).matcher(fetchCall);

        return m.find() ? m.group(1).toUpperCase() : "GET";
    }

    private String resolveUrl(String base, String endpoint) {
        try {
            return new java.net.URL(new java.net.URL(base), endpoint).toString();
        } catch (Exception e) {
            return endpoint;
        }
    }

    private static String resolvePlaceholders(String url, Map<String, String> variables) {
        if (url == null) return null;

        Matcher matcher = Pattern.compile("\\$\\{([^}]+)}").matcher(url);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String key = matcher.group(1).trim();
            String replacement = variables.getOrDefault(key, "");
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(sb);

        return sb.toString().replaceAll("(?<!:)//+", "/");
    }

    private void printAsciiWebGraph(Consumer<String> log) {
        log.accept("\n===== DATA =====");

        for (String page : pageToEndpoints.keySet()) {
            log.accept(page);

            for (String entry : pageToEndpoints.get(page)) {
                log.accept("|_ " + entry);
            }

            log.accept("");
        }
    }

    public List<CrawlerSession> getAllSessions() {
        return cliCrawlerService.getAllSessions();
    }
}
