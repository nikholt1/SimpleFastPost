package org.example.simplefastpost.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/crawler")
public class CrawlerController {


    @GetMapping("/run")
    public SseEmitter run(@RequestParam String url) {
        SseEmitter emitter = new SseEmitter(0L); // no timeout

        new Thread(() -> {
            try {
                emitter.send("\u001B[48;5;129mReached backend ready for crawl: " + url + "\u001B[0m");
                Thread.sleep(500);
                emitter.send("Fetching page...");

                Thread.sleep(500);
                emitter.send("Parsing HTML...");

                Thread.sleep(500);
                emitter.send("Extracting links...");

                Thread.sleep(500);
                emitter.send("Done crawling: " + url);

                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();

        return emitter;
    }

}
