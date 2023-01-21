package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.crawler.Node;
import searchengine.crawler.WebCrawler;
import searchengine.dto.crawler.CrawlerResponse;
import searchengine.dto.crawler.IndexPageResponse;
import searchengine.model.Page;
import searchengine.model.SiteModel;
import searchengine.model.Status;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class WebCrawlerServiceImpl implements WebCrawlerService {

    private final SiteRepository siteRepository;

    private final PageRepository pageRepository;

    private final SitesList sites;

    private ThreadPoolExecutor executor;

    private CrawlerResponse response;

    private IndexPageResponse indexPageResponse;

    private volatile boolean crawlingUp = false;

    @Override
    public boolean isCrawlingUp() {
        return crawlingUp;
    }

    @Override
    public CrawlerResponse startSitesCrawling() {
        clearTables();
        executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        response = new CrawlerResponse();

        if (!crawlingUp) {
            crawlingUp = true;
            for (Site site : sites.getSites()) {
                executor.submit(() -> {
                    try {
                        siteCrawling(site);
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
    public CrawlerResponse stopSitesCrawling() {

        response = new CrawlerResponse();
        if (crawlingUp) {
            /*TODO Метод останавливает текущий процесс индексации (переиндексации). Если в настоящий момент индексация
                или переиндексация не происходит, метод возвращает соответствующее сообщение об ошибке. */

            for (Site site : sites.getSites()) {
                updateSiteWhenError("Операция прервана пользователем", getSiteModel(site.getUrl()));
            }
            crawlingUp = false;
            executor.shutdownNow();
            response.setResult(true);
        } else {
            response.setResult(false);
            response.setError(1);
        }
        return response;
    }

    private SiteModel getSiteModel(String url) {
        return siteRepository.findByUrl(url);
    }

    @Override
    public IndexPageResponse indexPage(String url) {
        indexPageResponse = new IndexPageResponse();
        if (0 == 0) {
            /*TODO Метод добавляет в индекс или обновляет отдельную страницу, адрес которой передан в параметре. Если
                адрес страницы передан неверно, метод должен вернуть соответствующую ошибку. */
            indexPageResponse.setResult(true);
        } else {
            indexPageResponse.setResult(false);
            indexPageResponse.setError(2);
        }
        return indexPageResponse;
    }

    @Override
    @Modifying
    @Transactional
    public void clearTables() {
        /*TODO https://losst.pro/kak-ochistit-tablitsu-v-mysql */
        siteRepository.truncateTableWithFK();
        pageRepository.resetSequenceTable();
        pageRepository.truncateTable();
    }

    private void siteCrawling(Site site) throws ExecutionException, InterruptedException, MalformedURLException {
        System.out.println(Thread.currentThread().getName() + " started");
        Future future;
        ForkJoinPool pool = new ForkJoinPool(8);

        URL rootUrl = new URL(site.getUrl());
        SiteModel siteModel = getSiteModelAndSave(site);

        WebCrawler crawler = null;
        try {
            crawler = new WebCrawler(rootUrl, siteModel, this);
        } catch (MalformedURLException ex) {
            updateSiteWhenError(ex.getMessage(), siteModel);
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
        future = pool.submit(crawler);

        try {
            future.get();
        } catch (InterruptedException ex/* ExecutionException ex*/) {
            updateSiteWhenError(ex.getMessage(), siteModel);
            stop(pool);
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
        if (future.isDone()) {
            if (future.isCancelled()) {
                siteModel.setStatus(Status.FAILED);
                siteModel.setLastError("Обход сайта был завершен некорректно");
            } else /**//*if (siteModel.getStatus() != Status.FAILED)*/ {
                siteModel.setStatus(Status.INDEXED);
            }
            siteRepository.saveAndFlush(siteModel);
            stop(pool);
        }

    }

    private SiteModel getSiteModelAndSave(Site site) {

        SiteModel siteModel = new SiteModel();
        siteModel.setUrl(site.getUrl());
        siteModel.setName(site.getName());
        siteModel.setStatus(Status.INDEXING);
        siteModel.setLastError("");/*TODO какие писать ошибки?*/
        siteModel.setStatusTime(Date.from(Instant.now()));
        siteRepository.saveAndFlush(siteModel);
        return siteModel;
    }

    @Override
    public void updateSiteWhenError(String message, SiteModel siteModel) {
        siteModel.setStatus(Status.FAILED);
        siteModel.setStatusTime(Date.from(Instant.now()));
        siteModel.setLastError(message);
        siteRepository.saveAndFlush(siteModel);
    }

    @Override
    public boolean savePage(Node child, SiteModel siteModel) {
        boolean isSaved = false;
        String content = child.getContent();
        String path = child.getPath();
        int code = child.getStatusCode();
        if (!pageRepository.existsBySite_IdAndPathAllIgnoreCase(siteModel.getId(), path)) {
            Page page = new Page();
            page.setSite(siteModel);
            page.setPath(path);
            page.setCode(code);
            page.setContent(content);
            System.out.println(path);
            pageRepository.saveAndFlush(page);
            isSaved = true;
        }
        return isSaved;
    }

    @Override
    public void updateSiteStatusTime(SiteModel siteModel) {
        siteModel.setStatusTime(Date.from(Instant.now()));
        siteRepository.saveAndFlush(siteModel); //siteRepository.findById((long) page.getSite().getId()).get()
    }

    @Override
    public void checkRunningAndStopCrawling(ForkJoinPool pool) throws InterruptedException {
        if (!crawlingUp) {
//            pool.shutdownNow();
//            pool.awaitTermination(10, TimeUnit.SECONDS);
            stop(pool);
        }
    }

    public void stop(ForkJoinPool pool) {
        pool.shutdown();
        try {
            if (pool.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
                return;
            }
            pool.shutdownNow();
            pool.awaitTermination(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignore) {
            Thread.currentThread().interrupt();
        }
        crawlingUp = false;
    }

}
