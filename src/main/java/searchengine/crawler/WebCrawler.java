package searchengine.crawler;

import lombok.SneakyThrows;
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

    @SneakyThrows
    @Override
    protected void compute() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException ex) {
            throw new RuntimeException("Поток " + Thread.currentThread().getName() + " был неожиданно прерван во время " +
                    "ожидания");
//            webCrawlerService.updateSiteWhenError(ex.getMessage(), siteModel);
        }

        try {
            webCrawlerService.checkRunningAndStopCrawling(getPool());
            node.setChildren();
        } catch (IOException e) {
            throw new RuntimeException(Thread.currentThread().getName() + "Ошибка I/O во время установки дочерних " +
                    "ссылок страницы " + node.getLink().toString());
        }
        List<WebCrawler> taskList = new LinkedList<>();
        for (Node child : node.getChildren()) {
            URL link = child.getLink();
            webCrawlerService.checkRunningAndStopCrawling(getPool());
            if (webCrawlerService.savePage(child, siteModel)) {
                webCrawlerService.checkRunningAndStopCrawling(getPool());
                WebCrawler task = new WebCrawler(link, siteModel, webCrawlerService);
                task.fork();
                webCrawlerService.checkRunningAndStopCrawling(getPool());
                taskList.add(task);
                webCrawlerService.updateSiteStatusTime(siteModel);
            }
        }
        for (WebCrawler task : taskList) {
            task.join();
        }
    }
}
