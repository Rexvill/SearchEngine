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

import java.net.MalformedURLException;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

@Service
@RequiredArgsConstructor
public class WebCrawlerServiceImpl implements WebCrawlerService {


    private final SiteRepository siteRepository;

    private final PageRepository pageRepository;

    private final SitesList sites;

    private boolean crawlingUp = false;

    @Override
    public CrawlerResponse siteCrawling() {

        try {
            siteIndexing();
        } catch (ExecutionException | InterruptedException e) {
            System.out.println(e.getMessage());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        crawlingUp = true;
        CrawlerResponse response = new CrawlerResponse();
        response.setResult(true);
        return response;
    }

    private void siteIndexing() throws ExecutionException, InterruptedException, MalformedURLException {

        String[] errors = {
                "Ошибка индексации: главная страница сайта не доступна",
                "Ошибка индексации: сайт не доступен",
                ""};


        for (Site site : sites.getSites()) {
            new Thread(() -> {
                Future future;
                ForkJoinPool pool = new ForkJoinPool(4);

                String rootUrl = site.getUrl();
                SiteModel siteModel = getSiteModel(errors, site);

                System.out.println("Переходим на сайт " + siteModel.getUrl());
                System.out.println("Подождите, идет обработка...");

                WebCrawler crawler = null;
                try {
                    crawler = new WebCrawler(rootUrl, siteModel, pageRepository, siteRepository);
                } catch (MalformedURLException ex) {
                    System.out.println(ex.getMessage());
                }
                future = pool.submit(crawler);
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    crawler.updateSiteWhenError(e);
                }
                if (future.isDone()) {
                    if (future.isCancelled()) {
                        siteModel.setStatus(Status.FAILED);
                        siteRepository.saveAndFlush(siteModel);
                    } else if (siteModel.getStatus() != Status.FAILED) {
                        siteModel.setStatus(Status.INDEXED);
                        siteRepository.saveAndFlush(siteModel);
                    }
                }

            }).start();
        }
    }

    private SiteModel getSiteModel(String[] errors, Site site) {

        SiteModel siteModel = new SiteModel();
        siteModel.setUrl(site.getUrl());
        siteModel.setName(site.getName());
        siteModel.setStatus(Status.INDEXING);
        siteModel.setLastError(errors[2]);/*TODO какие писать ошибки?*/
        siteModel.setStatusTime(Date.from(Instant.now()));
        siteRepository.saveAndFlush(siteModel);
        return siteModel;
    }
}
