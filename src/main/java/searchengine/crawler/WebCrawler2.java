package searchengine.crawler;

import lombok.SneakyThrows;
import org.jsoup.Connection;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.model.SiteModel;
import searchengine.services.WebCrawlerService;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.RecursiveAction;

public class WebCrawler2 extends RecursiveAction {

    private final WebCrawlerService webCrawlerService;

    private final URL rootUrl;

    private final SiteModel siteModel;

    public WebCrawler2(URL link, SiteModel siteModel, WebCrawlerService webCrawlerService) throws MalformedURLException {
        rootUrl = link;
        this.webCrawlerService = webCrawlerService;
        this.siteModel = siteModel;
    }

    @Override
    @SneakyThrows
    protected void compute() {

        try {
            Thread.sleep(200);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex.getMessage() + "Поток " + Thread.currentThread().getName()
                    + " был неожиданно прерван во время ожидания");
        }
        Node2 node = new Node2(rootUrl);
        setNodeChildren(node);
        List<WebCrawler2> taskList = new LinkedList<>();
        for (URL childUrl : node.getChildren()) {
            webCrawlerService.checkRunningAndStopCrawling(getPool());
            WebCrawler2 task = new WebCrawler2(childUrl, siteModel, webCrawlerService);
            task.fork();
            taskList.add(task);
            webCrawlerService.updateSiteStatusTime(siteModel);
        }
        for (WebCrawler2 task : taskList) {
            task.join();
        }
    }

    private void setNodeChildren(Node2 node) throws IOException, InterruptedException {
        Connection.Response response = node.getResponse();
        Elements elements = response.parse().select("a[href]");
        String domain = rootUrl.getProtocol() + "://" + rootUrl.getHost();

        for (Element element : elements) {
            webCrawlerService.checkRunningAndStopCrawling(getPool());
            URL link = element.attr("abs:href").startsWith(domain)
                    ? new URL(element.attr("abs:href"))
                    : null;

            if (link != null
                    && !link.sameFile(rootUrl)
                    && !link.toExternalForm().contains("#")
                    && !webCrawlerService.urlExistInDB(siteModel.getId(), link.getFile())
            ) {
                webCrawlerService.checkIfNotExistAndSavePage(link, siteModel);
                if (link.openConnection().getContentType().contains("text/html")) {
                    node.addChild(link);
                }
            }
        }
    }


}