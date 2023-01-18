package searchengine.crawler;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public class Node {

    private static final CopyOnWriteArraySet<Integer> childrenLinks = new CopyOnWriteArraySet<>();

    private final URL url;

    private final ArrayList<Node> children = new ArrayList<>();

    private Connection.Response response;

    private Document doc;

    public Node(URL link) throws MalformedURLException {
        this.url = link;
        try {
            setResponse(link);
            setDoc();
        } catch (IOException ex) {
            System.out.println("Getting response exception - " + link + " " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void setResponse(URL url) throws IOException {
        response =
                Jsoup.newSession().url(url)
                        .ignoreContentType(true)
                        .ignoreHttpErrors(true)
                        .followRedirects(false)
                        .userAgent(" Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " + //"VaryFineSearchBot"
                                "(KHTML, like Gecko) Chrome/106.0.0.0 YaBrowser/22.11.5.715 Yowser/2.5 Safari/537.36")
                        .referrer("http://www.google.com")
//                    .timeout(10000)
                        .execute().bufferUp();
    }

    private void setDoc() throws IOException {
        doc = response.parse();
    }

    public int getStatusCode() {
        return response.statusCode();
    }

    public String getContent() {
        if (doc == null) {
            return "";
        }
        return doc.outerHtml();
    }

    public URL getLink() {
        return url;
    }

    public String getPath() {
        return url.getFile();
    }


    public ArrayList<Node> getChildren() {
        return children;
    }

    public void setChildren() throws IOException {
        Elements elements;
        if (doc != null) {
            elements = doc.select("a");
            doc = null;
            String domain = url.getProtocol() + "://" + url.getHost();
            URL link = null;
            try {
                for (Element element : elements) {
                    if (element.attr("abs:href").contains(domain)) {
                        link = new URL(element.attr("abs:href")); //.toURI().toURL()
                    }
                    if (link.toExternalForm().startsWith(domain)
                            && !childrenLinks.contains(link.hashCode())
                            && !link.sameFile(url)
                            && !link.toExternalForm().contains("#")
                            && !children.contains(link)) {
                        childrenLinks.add(link.hashCode());
                        addChild(new Node(link));
                        System.out.println(Thread.currentThread().getName() + " " + link);
                    }
                }
            } catch (MalformedURLException ex) {
                childrenLinks.add(link.hashCode());
//                throw new RuntimeException((Thread.currentThread().getName() + " " + link ));
                ex.printStackTrace();
            } /*catch (URISyntaxException ex) {
//                throw new RuntimeException(ex.getCause() + " " + link);
//            }*/
        }
    }

    private void addChild(Node node) {
        children.add(node);
    }

    public Document getDoc() {
        return doc;
    }
}
