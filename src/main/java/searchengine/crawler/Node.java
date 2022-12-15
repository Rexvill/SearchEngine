package searchengine.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class Node {

    private static final CopyOnWriteArrayList<String> childrenLinks = new CopyOnWriteArrayList<>();
    private final String link;
    private final ArrayList<Node> children = new ArrayList<>();

    private Document doc;

    //    private int level;

    public Node(String link) {
        this.link = link;
        doc = getDoc();
    }

    public static CopyOnWriteArrayList<String> getChildrenLinks() {
        return childrenLinks;
    }

    public String getLink() {
        return link;
    }


    private void addChild(Node node) {
//        node.setLevel(level + 1);
        children.add(node);
    }

    //    private void setLevel(int level) {
//        this.level = level;
//    }
    public Document getDoc() {
        doc = null;
        try {
            doc = Jsoup.newSession().url(link)
                    .ignoreContentType(true)
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64)"
                            + " Chrome/100.0.4896.160 YaBrowser/22.5.4.904 Safari/537.36")
                    .referrer("http://www.google.com")
                    .get();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return doc;
    }

    public ArrayList<Node> getChildren() {
        return children;
    }

    public void setChildren() {


        Elements elements = new Elements();

            /*TODO пересмотреть определение и необходимость  domain
                (некоторые сайты  падают с
                java.lang.StringIndexOutOfBoundsException: begin 8, end 0, length 23)
             */
        String domain = "https://" + link.substring(link.indexOf("://") + 3, link.indexOf("/", 9) + 1);

        if (doc != null) {
            elements = doc.select("a");
        }
        elements.forEach(element -> {
            String url = element.attr("abs:href");/**/
            if (url.startsWith(domain)
                    && !url.contains("#")
                    && !url.equals(link)
                    && !childrenLinks.contains(url)) {
                System.out.println(Thread.currentThread().getName()+" "+url);
                addChild(new Node(url));
                childrenLinks.add(url);
            }
        });
    }
}
