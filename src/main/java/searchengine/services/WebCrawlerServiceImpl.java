package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.crawler.WebCrawler;
import searchengine.dto.crawler.CrawlerResponse;
import searchengine.model.SiteModel;
import searchengine.model.Status;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.time.LocalDateTime;
import java.util.concurrent.ForkJoinPool;

@Service
@RequiredArgsConstructor
public class WebCrawlerServiceImpl implements WebCrawlerService {


    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;

    private final SitesList sites;

    @Override
    public CrawlerResponse siteCrawling() {
        siteIndexing();
        CrawlerResponse response = new CrawlerResponse();
        response.setResult(true);


        return response;
    }

    private void siteIndexing() {

        String[] errors = {
                "Ошибка индексации: главная страница сайта не доступна",
                "Ошибка индексации: сайт не доступен",
                ""};

        for (Site site : sites.getSites()) {
            String rootUrl = site.getUrl();
            SiteModel siteModel = new SiteModel();
            siteModel .setUrl(rootUrl);
            siteModel.setName(site.getName());
            siteModel.setStatus(Status.INDEXING);
            siteModel.setLastError(errors[2]);/*TODO какие писать ошибки?*/
            siteModel.setStatusTime(LocalDateTime.now());
            siteRepository.save(siteModel);

            System.out.println("Переходим на сайт " + rootUrl);
            System.out.println("Подождите, идет обработка...");

            long start = System.currentTimeMillis();
            WebCrawler crawler = new WebCrawler(rootUrl, siteModel, pageRepository);
            ForkJoinPool pool = new ForkJoinPool(4);
            pool.invoke(crawler);
            long duration = System.currentTimeMillis() - start;
            System.out.println(duration);
        }
    }
}
