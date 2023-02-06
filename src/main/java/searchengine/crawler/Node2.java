package searchengine.crawler;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class Node2 {

    private final URL url;

    private final Set<URL> children = new HashSet<>();

    private final Connection.Response response;

    public Node2(URL url) throws IOException {
        this.url = url;
        response = Jsoup.newSession().url(url)
                .ignoreContentType(true)
                .ignoreHttpErrors(true)
                .followRedirects(false)
                .userAgent(//"VeryFineSearchBot")
                        " Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                                "(KHTML, like Gecko) Chrome/106.0.0.0 YaBrowser/22.11.5.715 Yowser/2.5 Safari/537.36")
                .referrer("http://www.google.com")
                .execute().bufferUp();
    }

    public Set<URL> getChildren() {
        return children;
    }

    public Connection.Response getResponse() {
        return response;
    }

    public String getPath() {
        return url.getFile();
    }

    public void addChild(URL url) {
        children.add(url);
    }

}
