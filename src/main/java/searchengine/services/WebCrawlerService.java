package searchengine.services;

import searchengine.dto.crawler.CrawlerResponse;
import searchengine.model.SiteModel;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ForkJoinPool;

public interface WebCrawlerService {

    boolean isCrawlingUp();

    CrawlerResponse startSitesCrawling() throws InterruptedException;

    CrawlerResponse stopSitesCrawling();

    void clearTables();

    SiteModel getSiteModel(String url);

    void updateSiteWhenError(String message, SiteModel siteModel);

    boolean checkIfNotExistAndSavePage(URL link, SiteModel siteModel) throws IOException;

    void updateSiteStatusTime(SiteModel siteModel);

    void checkRunningAndStopCrawling(ForkJoinPool pool) throws InterruptedException;

    boolean urlExistInDB(int siteId, String path);
}
