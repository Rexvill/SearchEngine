package searchengine.crawler;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public class Node {

    private static final CopyOnWriteArraySet<String> childrenLinks = new CopyOnWriteArraySet<>();

    private final String link;

    private final URL url;

    private final ArrayList<Node> children = new ArrayList<>();

    Connection.Response response;

    Document doc;

    int code;

    public Node(String link) throws MalformedURLException {

        this.link = link;
        this.url = new URL(link);
        try {
            response = Jsoup.newSession().url(link)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .followRedirects(false)
                    .userAgent(" Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " + //"VaryFineSearchBot"
                            "(KHTML, like Gecko) Chrome/106.0.0.0 YaBrowser/22.11.5.715 Yowser/2.5 Safari/537.36")
                    .referrer("http://www.google.com")
//                    .timeout(10000)
                    .execute()
                    .bufferUp();
            code = response.statusCode();
            doc = response.parse();
        } catch (IOException e) {
            System.out.println("Getting response exception - " + link + " " + e.getMessage());
        }
    }

    public static CopyOnWriteArraySet<String> getChildrenLinks() {

        return childrenLinks;
    }

    public int getStatusCode() {

        return code;
    }

    public String getContent() {

        if (doc == null) {
            return "Sorry, the Document was empty";
        }
        return doc.html();
    }

    public String getLink() {

        return link;
    }

    public String getPath() {

        System.out.println(url.getPath());
        return url.getFile();
    }

    public ArrayList<Node> getChildren() {

        return children;
    }

    public void setChildren() throws IOException {


        Elements elements = doc.select("a");
        String domain = url.getProtocol() + "://" + url.getHost();
        System.out.println("Host " + domain);
        System.out.println("Doc.baseUri" + doc.baseUri());

        elements.forEach(element -> {
            String link = element.attr("abs:href");
            if (link.startsWith(domain)
                    && !link.contains("#")
//                    && !url.equals(link)
                    && !childrenLinks.contains(link)) {
                try {
                    addChild(new Node(link));
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(Thread.currentThread().getName() + " " + link);
                childrenLinks.add(link);
            }
        });
    }

    private void addChild(Node node) {

        children.add(node);
    }
}
