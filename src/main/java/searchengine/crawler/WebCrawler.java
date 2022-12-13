package searchengine.crawler;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.RecursiveAction;

public class WebCrawler extends RecursiveAction {

    private final Node node;

    public WebCrawler(Node node) {
        this.node = node;
    }

    @Override
    protected void compute() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }

        node.setChildren();

        List<WebCrawler> taskList = new LinkedList<>();
        ArrayList<Node> children = node.getChildren();

        for (Node child : children) {
            WebCrawler task = new WebCrawler(child);
            task.fork();
            taskList.add(task);
        }
        for (WebCrawler task : taskList) {
            task.join();
        }
    }
}
