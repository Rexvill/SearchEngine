package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexer.IndexPageResponse;
import searchengine.dto.indexer.IndexSiteResponse;
import searchengine.indexer.Node;
import searchengine.indexer.SiteIndexer;
import searchengine.model.Page;
import searchengine.model.SiteModel;
import searchengine.model.Status;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class WebCrawlerServiceImpl implements WebCrawlerService {

    private static volatile boolean crawlingUp = false;

    private final SiteRepository siteRepository;

    private final PageRepository pageRepository;

    private final SitesList sites;

    private ThreadPoolExecutor executor;

    private IndexSiteResponse response;

    @Override
    public boolean isCrawlingUp() {
        return crawlingUp;
    }

    @Override
    public IndexSiteResponse startSitesCrawling() {
        clearTables();
        executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        response = new IndexSiteResponse();
        if (!crawlingUp) {
            crawlingUp = true;
            for (Site site : sites.getSites()) {
                executor.submit(() -> {
                    try {
                        SiteModel siteModel = new SiteModel();
                        saveSiteModel(site, siteModel);
                        siteCrawling(site, siteModel);
                    } catch (ExecutionException | InterruptedException ex) {
                        System.out.println(ex.getMessage());
                        ex.printStackTrace();
                    } catch (MalformedURLException ex) {
                        ex.printStackTrace();
                    }
                });
            }
            response.setResult(true);
        } else {
            response.setResult(false);
            response.setError(0);
        }
        return response;
    }

    @Override
    public IndexSiteResponse stopSitesCrawling() {
        response = new IndexSiteResponse();
        if (crawlingUp) {
            executor.shutdownNow();
            crawlingUp = false;
            response.setResult(true);
        } else {
            response.setResult(false);
            response.setError(1);
        }
        return response;
    }

    @Override
    @Modifying
    @Transactional
    public void clearTables() {
        siteRepository.truncateTableWithFK();
        pageRepository.truncateTableAndResetSequenceTable();
    }

    private void saveSiteModel(Site site, SiteModel siteModel) {
        siteModel.setUrl(site.getUrl());
        siteModel.setName(site.getName());
        siteModel.setStatus(Status.INDEXING);
        siteModel.setLastError("");
        siteModel.setStatusTime(Date.from(Instant.now()));
        siteRepository.saveAndFlush(siteModel);
    }

    private void siteCrawling(Site site, SiteModel siteModel) throws ExecutionException, InterruptedException, MalformedURLException {
        ForkJoinPool pool = new ForkJoinPool(4);
        URL rootUrl = new URL(site.getUrl());
        SiteIndexer crawler = new SiteIndexer(rootUrl, siteModel, this);
        Future<Void> future = pool.submit(crawler);
        while (!crawlingUp) {
            stop(pool);
        }
        future.get();
        if (future.isDone()) {
            if (future.isCancelled()) {
                System.out.println(Thread.currentThread().getName() + " Обход сайта не был завершен корректно");
                siteModel.setStatus(Status.FAILED);
                siteModel.setLastError("Обход сайта не был завершен корректно");
                siteRepository.saveAndFlush(siteModel);
            } else if (siteModel.getStatus() != Status.FAILED) {
                siteModel.setStatus(Status.INDEXED);
                siteRepository.saveAndFlush(siteModel);
            }
            stop(pool);
        }
    }

    private void stop(ForkJoinPool pool) {
        pool.shutdown();
        try {
            if (pool.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
                return;
            }
            pool.shutdownNow();
        } catch (InterruptedException ignore) {
            Thread.currentThread().interrupt();
        }
        for (Site site : sites.getSites()) {
            SiteModel siteModel = getSiteModel(site.getUrl());
            updateSiteWhenError("Операция прервана пользователем", siteModel);
        }
    }

    @Override
    public SiteModel getSiteModel(String url) {
        return siteRepository.findByUrl(url);
    }

    @Override
    @Modifying
    @Transactional
    public void updateSiteWhenError(String message, SiteModel siteModel) {
        siteModel.setStatus(Status.FAILED);
        siteModel.setStatusTime(Date.from(Instant.now()));
        siteModel.setLastError(message);
        siteRepository.saveAndFlush(siteModel);
    }

    @Override
    @Modifying
    @Transactional
    public boolean checkIfNotExistAndSavePage(URL link, SiteModel siteModel) throws IOException {
        boolean exist = true;
        Node child = new Node(link);
        String path = child.getPath();

        Connection.Response response = child.getResponse();
        int code = response.statusCode();
        String content;
        if (code == 200 && Objects.requireNonNull(response.contentType()).startsWith("text/html")) {
            content = response.parse().html();
        } else {
            content = "";
        }
        Page page = new Page(siteModel, path, code, content);
        synchronized (this) {
            if (!urlExistInDB(siteModel.getId(), path)) {
                pageRepository.saveAndFlush(page);
                exist = false;
            }
        }
        return exist;
    }

    @Override
    @Modifying
    @Transactional
    public void updateSiteStatusTime(SiteModel siteModel) {
        siteModel.setStatusTime(Date.from(Instant.now()));
        siteRepository.saveAndFlush(siteModel);
    }

    @Override
    public void checkRunningAndStopCrawling(ForkJoinPool pool) {
        if (!crawlingUp) {
            stop(pool);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public boolean urlExistInDB(int siteId, String path) {
        return pageRepository.existsBySite_IdAndPathAllIgnoreCase(siteId, path);
    }

    @Override
    public IndexPageResponse indexPage(String url) {
        /*TODO Метод добавляет в индекс или обновляет отдельную страницу, адрес которой передан в параметре. Если
                адрес страницы передан неверно, метод должен вернуть соответствующую ошибку. */
        
        IndexPageResponse indexPageResponse = new IndexPageResponse();
        
        /*TODO
           - найти в базе запись с этим УРЛ
           -если запись есть - удалить, обойти этот урл ещё раз
           - иначе (- проверить, что урл начинается с домена, присутствующего в базе
                (ДА - записать в базу и проиндексировать;
                НЕТ - выдать ошибку))
         */
        if (0 == 0) {
            
            indexPageResponse.setResult(true);
        } else {
            indexPageResponse.setResult(false);
            indexPageResponse.setError(0);
        }
        return indexPageResponse;
    }

}
