package org.example.simplefastpost.controller;


import org.example.simplefastpost.crawler.Crawler;
import org.example.simplefastpost.crawler.cli.CLICrawlerService;
import org.example.simplefastpost.model.CrawlerSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/crawler")
public class CrawlerController {
    public Crawler crawler;

    public CrawlerController(Crawler crawler) {
        this.crawler = crawler;

    }

    @GetMapping("/run")
    public SseEmitter run(@RequestParam String url) {
        SseEmitter emitter = new SseEmitter(0L);
        new Thread(() -> {
            try {
                emitter.send("\u001B[48;5;129mReached backend ready for crawl: " + url + "\u001B[0m");

                crawler.run(url, message -> {
                    try {
                        emitter.send(message);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

                emitter.complete();

            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();

        return emitter;
    }

    @GetMapping("/getAllSessions")
    public List<CrawlerSession> getAllSessions() {
        return crawler.getAllSessions();
    }

}
