package searchengine.crawler;

import org.springframework.beans.factory.annotation.Autowired;
import searchengine.model.Page;
import searchengine.model.SiteModel;
import searchengine.repositories.PageRepository;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.RecursiveAction;


public class WebCrawler extends RecursiveAction {

    @Autowired
    private PageRepository pageRepository;
    private Node node;
    private String rootUrl;

    private SiteModel siteModel;

    public WebCrawler(String url, SiteModel siteModel, PageRepository pageRepository) {
        this.rootUrl = url;
        this.siteModel = siteModel;
        this.pageRepository = pageRepository;
        node = new Node(rootUrl);
    }

    @Override
    protected void compute() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }

        node.setChildren();

        List<WebCrawler> taskList = new LinkedList<>();
        ArrayList<Node> children = node.getChildren();

        for (Node child : children) {
            String link = child.getLink();
            WebCrawler task = new WebCrawler(link, siteModel, pageRepository);
            task.fork();
            taskList.add(task);

            String content = child.getDoc().toString();
            Page page = new Page();
            page.setSite(siteModel);
            page.setPath(link);
            page.setCode(200);
            page.setContent(content);
            pageRepository.save(page);



        }
        for (WebCrawler task : taskList) {
            task.join();
        }
    }
}
