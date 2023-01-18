package searchengine.crawler;

import searchengine.model.SiteModel;
import searchengine.services.WebCrawlerService;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.RecursiveAction;


public class WebCrawler extends RecursiveAction {

    private final WebCrawlerService webCrawlerService;

    private final Node node;

    private final SiteModel siteModel;

    public WebCrawler(URL link, SiteModel siteModel, WebCrawlerService webCrawlerService) throws MalformedURLException {
        node = new Node(link);
        this.webCrawlerService = webCrawlerService;
        this.siteModel = siteModel;
    }

    @Override
    protected void compute() {

        try {
            Thread.sleep(200);
        } catch (InterruptedException ex) {
            webCrawlerService.updateSiteWhenError(ex.getMessage(), siteModel);
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }

        try {
            node.setChildren();
            List<WebCrawler> taskList = new LinkedList<>();


            for (Node child : node.getChildren()) {
                URL link = child.getLink();
                if (webCrawlerService.savePage(child, siteModel)) {
                    WebCrawler task = new WebCrawler(link, siteModel, webCrawlerService);
                    task.fork();
                    taskList.add(task);

                    webCrawlerService.updateSiteStatusTime(siteModel);
                }
            }
            for (WebCrawler task : taskList) {
                task.join();
            }
        } catch (IOException ex) {
            webCrawlerService.updateSiteWhenError(ex.getMessage(), siteModel);
        }
    }
}
