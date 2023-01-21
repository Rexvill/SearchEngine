package searchengine.services;

import searchengine.crawler.Node;
import searchengine.dto.crawler.CrawlerResponse;
import searchengine.dto.crawler.IndexPageResponse;
import searchengine.model.SiteModel;

import java.io.IOException;
import java.util.concurrent.ForkJoinPool;

public interface WebCrawlerService {

    boolean isCrawlingUp();

    CrawlerResponse startSitesCrawling() throws InterruptedException;

    CrawlerResponse stopSitesCrawling();

    IndexPageResponse indexPage(String url);


    //    @Transactional
//    void truncateTable();

//    void updateSiteWhenError(Exception ex, SiteModel siteModel);


    void clearTables();

    void updateSiteWhenError(String message, SiteModel siteModel);

    boolean savePage(Node child, SiteModel siteModel) throws IOException;

    void updateSiteStatusTime(SiteModel siteModel);

    void checkRunningAndStopCrawling(ForkJoinPool pool) throws InterruptedException;
}
