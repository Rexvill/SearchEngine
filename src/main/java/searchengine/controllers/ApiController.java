package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.dto.crawler.CrawlerResponse;
import searchengine.dto.crawler.IndexPageResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.StatisticsService;
import searchengine.services.WebCrawlerService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final StatisticsService statisticsService;

    private final WebCrawlerService webCrawlerService;

//    public ApiController(StatisticsService statisticsService) {
//        this.statisticsService = statisticsService;
//    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<CrawlerResponse> startIndexing() throws InterruptedException {
        return ResponseEntity.ok(webCrawlerService.startSitesCrawling());
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<CrawlerResponse> stopIndexing() {
        return ResponseEntity.ok(webCrawlerService.stopSitesCrawling());
    }

    @PostMapping("/indexPage")
    public ResponseEntity<IndexPageResponse> indexPage(String url) {
        return ResponseEntity.ok(webCrawlerService.indexPage(url));
    }
}
