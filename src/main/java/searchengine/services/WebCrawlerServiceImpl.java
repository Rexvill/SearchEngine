package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.crawler.Node;
import searchengine.crawler.WebCrawler;
import searchengine.dto.crawler.CrawlerResponse;

import java.util.concurrent.ForkJoinPool;

@Service
@RequiredArgsConstructor
public class WebCrawlerServiceImpl implements WebCrawlerService {

    private final SitesList sites;

    @Override
    public CrawlerResponse siteCrawling() {
        CrawlerResponse response = new CrawlerResponse();
        if (response.isResult()){
            response.setResult(true);
        }
        for (Site site : sites.getSites()) {
            String rootUrl = site.getUrl();

            System.out.println("Переходим на сайт " + rootUrl);
            System.out.println("Подождите, идет обработка...");

            Node root = new Node(rootUrl);

            long start = System.currentTimeMillis();
            WebCrawler crawler = new WebCrawler(root);
            ForkJoinPool pool = new ForkJoinPool(4);
            pool.invoke(crawler);
            long duration = System.currentTimeMillis() - start;
            System.out.println(duration);
        }

        response.setResult(true);
        return response;
    }
}
