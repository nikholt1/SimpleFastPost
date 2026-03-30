package org.example.simplefastpost.crawler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CLICrawler {

    private static final Set<String> visited = new LinkedHashSet<>();
    private static final Map<String, Set<String>> pageToEndpoints = new LinkedHashMap<>();
    private static final Pattern fetchPattern = Pattern.compile("fetch\\((\"|')([^\"']+)(\"|')");



    public void run(String baseUrl) {
        crawl(baseUrl, 1);

        printAsciiWebGraph();
    }


    public static void crawl(String url, int depth) {
        if (visited.contains(url) || depth > 3) return; // depth limit

        try {
            visited.add(url);
            pageToEndpoints.putIfAbsent(url, new LinkedHashSet<>());
            System.out.println("Visiting: " + url);

            // Fetch the page
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (compatible; CLICrawler/1.0)")
                    .timeout(10000)
                    .get();

            // Extract links to crawl
            Elements links = doc.select("a[href]");
            for (Element link : links) {
                String nextUrl = link.absUrl("href");
                if (nextUrl.isEmpty() || nextUrl.startsWith("#") ||
                        nextUrl.startsWith("mailto:") || nextUrl.startsWith("javascript:")) continue;
                crawl(nextUrl, depth + 1);
            }

            // Scan inline scripts for fetch endpoints
            Elements scripts = doc.select("script");
            for (Element script : scripts) {
                scanFetchEndpoints(script.html(), url);

                // Scan external JS files
                String src = script.absUrl("src");
                if (!src.isEmpty()) {
                    try {
                        String jsContent = Jsoup.connect(src)
                                .userAgent("Mozilla/5.0 (compatible; CLICrawler/1.0)")
                                .ignoreContentType(true)
                                .timeout(10000)
                                .execute()
                                .body();
                        scanFetchEndpoints(jsContent, url);
                    } catch (Exception e) {
                        System.out.println("Failed to fetch JS: " + src + " (" + e.getMessage() + ")");
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("Failed: " + url + " (" + e.getMessage() + ")");
        }
    }

    private static void scanFetchEndpoints(String js, String pageUrl) {
        Matcher matcher = fetchPattern.matcher(js);
        while (matcher.find()) {
            String endpoint = matcher.group(2);
            pageToEndpoints.get(pageUrl).add(endpoint);
            System.out.println("Found endpoint on page [" + pageUrl + "]: " + endpoint);
        }
    }

    private static void printAsciiWebGraph() {
        System.out.println("\n===== DATA =====");
        for (String page : pageToEndpoints.keySet()) {
            System.out.println(page);
            for (String endpoint : pageToEndpoints.get(page)) {
                System.out.println("|_ " + endpoint);
            }
            System.out.println(); // extra spacing between pages
        }
    }
}