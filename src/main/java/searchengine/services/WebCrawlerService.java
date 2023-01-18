package searchengine.services;

import searchengine.crawler.Node;
import searchengine.dto.crawler.CrawlerResponse;
import searchengine.dto.crawler.IndexPageResponse;
import searchengine.model.SiteModel;

import java.io.IOException;

public interface WebCrawlerService {

    CrawlerResponse startSitesCrawling() throws InterruptedException;

    CrawlerResponse stopSitesCrawling();

    IndexPageResponse indexPage(String url);


    //    @Transactional
//    void truncateTable();

//    void updateSiteWhenError(Exception ex, SiteModel siteModel);

   void updateSiteWhenError(String message, SiteModel siteModel);

    boolean savePage(Node child, SiteModel siteModel) throws IOException;

    void updateSiteStatusTime(SiteModel siteModel);
}
