package com.taogen.hotcrawler.commons.crawler.impl;

import com.taogen.hotcrawler.commons.crawler.HotProcessor;
import com.taogen.hotcrawler.commons.entity.Info;
import com.taogen.hotcrawler.commons.util.OsUtils;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("BilibiliHotProcessor")
public class BilibiliHotProcessor implements HotProcessor
{
    private static final Logger log = LoggerFactory.getLogger(BilibiliHotProcessor.class);
    private static final String OS = System.getProperty("os.name").toLowerCase();

    @Autowired
    private BaseHotProcessor baseHotProcessor;

    @Value("${crawler.chromeDriver.enable}")
    private Boolean enable;

    @Value("${crawler.chromeDriver.linuxPath}")
    private String linuxPath;

    @Value("${crawler.chromeDriver.winPath}")
    private String winPath;

    public static final String DOMAIN = "https://www.bilibili.com";
    public static final String HOT_PAGE_URL = "https://www.bilibili.com/v/kichiku/guide/#/all/click/0/1/";
//    public static final String HOT_PAGE_URL = "https://www.bilibili.com/v/kichiku/guide/";
    public static final String ITEM_KEY = "l-item";


    @Override
    public List<Info> crawlHotList()
    {
        List<Info> list = new ArrayList<>();
        if (! enable)
        {
            return list;
        }
        String osType = OsUtils.getOsType();
        log.info("os type is {}", osType);
//        if (OsUtils.OS_TYPE_WINDOWS.equals(osType))
//        {
//            System.setProperty("webdriver.chrome.driver", winPath);
//            log.info("driver path: {}", winPath);
//        }
//        else
//        {
//            System.setProperty("webdriver.chrome.driver", linuxPath);
//            log.info("driver path: {}", linuxPath);
//        }

        // doc
        Document doc = null;
        try
        {
            ChromeDriverManager.chromedriver().setup();
            WebDriverManager.chromedriver().config().setProperties("classpath: webdrivermanager.properties");
            WebDriver driver = new ChromeDriver();
            driver.get(HOT_PAGE_URL);
            String pageSource = driver.getPageSource();
            doc = Jsoup.parse(pageSource);
            driver.close();
        }
        catch (RuntimeException e)
        {
            log.error("Something error!", e);
        }

        if (doc == null)
        {
            return list;
        }

        // elements
        Elements elements = doc.getElementsByClass(ITEM_KEY);
        log.debug("elements size is {}", elements.size());

        int i = 0;
        for (Element element : elements)
        {
            Element itemElement = null;
            try
            {
                itemElement = element.getElementsByClass("title").get(0);
            }
            catch (NullPointerException | IndexOutOfBoundsException e)
            {
                log.error("Can't found item element by attribute!", e);
                continue;
            }
            // id
            String id = String.valueOf(++i);

            // url
            String infoUrl = itemElement.attr("href").substring(2);
            infoUrl = "https://" + infoUrl;

            // title
            String infoTitle = itemElement.html();

            list.add(new Info(id, infoTitle, infoUrl));
        }
        log.debug("list size is {}", list.size());
        return list;
    }
}
