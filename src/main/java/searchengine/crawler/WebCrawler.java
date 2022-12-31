package searchengine.crawler;

import org.springframework.beans.factory.annotation.Autowired;
import searchengine.model.Page;
import searchengine.model.SiteModel;
import searchengine.model.Status;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.io.IOException;
import java.net.MalformedURLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.RecursiveAction;


public class WebCrawler extends RecursiveAction {

    @Autowired
    private final PageRepository pageRepository;

    @Autowired
    private final SiteRepository siteRepository;

    private final Node node;

//    private String rootUrl;

    private final SiteModel siteModel;

    public WebCrawler(String url, SiteModel siteModel, PageRepository pageRepository, SiteRepository siteRepository) throws MalformedURLException {

        node = new Node(url);
        this.siteModel = siteModel;
        this.pageRepository = pageRepository;
        this.siteRepository = siteRepository;
    }

    @Override
    protected void compute() {

        try {
            Thread.sleep(200);
        } catch (InterruptedException ex) {
            updateSiteWhenError(ex);
        }

        try {
            node.setChildren();
            List<WebCrawler> taskList = new LinkedList<>();
            ArrayList<Node> children = node.getChildren();

            for (Node child : children) {
                String link = child.getLink();
                WebCrawler task = new WebCrawler(link, siteModel, pageRepository, siteRepository);
                task.fork();
                taskList.add(task);

                savePage(child);
                updateSiteStatusTime();
            }
            for (WebCrawler task : taskList) {
                task.join();
            }
        } catch (IOException ex) {
            updateSiteWhenError(ex);
        }
    }

    private void savePage(Node child) throws IOException {

        String content = child.getContent();
        String path = child.getPath();
        int code = child.getStatusCode();
        Page page = new Page();
        page.setSite(siteModel);
        page.setPath(path);
        page.setCode(code);
        page.setContent(content);
        System.out.println(path);
        pageRepository.saveAndFlush(page);


    }

    private void updateSiteStatusTime() {

        siteModel.setStatusTime(Date.from(Instant.now()));
        siteRepository.saveAndFlush(siteModel); //siteRepository.findById((long) page.getSite().getId()).get()
    }

    public void updateSiteWhenError(Exception ex) {

        siteModel.setStatus(Status.FAILED);
        siteModel.setLastError(ex.getMessage());
        siteRepository.saveAndFlush(siteModel);
    }
}
