package searchengine.services;

import searchengine.dto.indexer.IndexPageResponse;
import searchengine.dto.indexer.IndexSiteResponse;
import searchengine.model.SiteModel;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ForkJoinPool;

public interface WebCrawlerService {

    boolean isCrawlingUp();

    IndexSiteResponse startSitesCrawling() throws InterruptedException;

    IndexSiteResponse stopSitesCrawling();

    IndexPageResponse indexPage(String url);

    void clearTables();

    SiteModel getSiteModel(String url);

    void updateSiteWhenError(String message, SiteModel siteModel);

    boolean checkIfNotExistAndSavePage(URL link, SiteModel siteModel) throws IOException;

    void updateSiteStatusTime(SiteModel siteModel);

    void checkRunningAndStopCrawling(ForkJoinPool pool) throws InterruptedException;

    boolean urlExistInDB(int siteId, String path);
}
