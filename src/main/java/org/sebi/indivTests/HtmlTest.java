package org.sebi.indivTests;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.select.Selector;
import org.sebi.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.not;

public class HtmlTest extends Test {

    private static Logger log = LogManager.getLogger(HtmlTest.class.getName());
    String matcher;

    public HtmlTest(String link, String matcher) {
        super(link);
        this.matcher = matcher;
    }


    @Override
    public boolean run() {
        super.run();
        log.trace("Called.");

        String html;
        try {
            html = runner.httpGetResponseBody(url);
        } catch (NullPointerException e){
            log.error("NullPointerException: Wasn't able to retrieve html from url: " + url);
            System.out.println("*HtmlTest failed. Unable to receive html from url: " + url);
            return false;
        }

        boolean testSuccess = false;

        try {
            org.junit.Assert.assertThat(Jsoup.parse(html).select(matcher).size(), not(0));
            log.info("Succesfully matched regex: " + matcher + " in given html under url: " + url);
            testSuccess = true;
        } catch (AssertionError e){
            log.error("Failed to match: " + matcher + " at url: " + url);
            System.out.println("*HtmlTest failed. Matcher: " + matcher + ". URL: " + url);
        } catch (Selector.SelectorParseException ex){
            log.error("Failed to parse selector: " + matcher + ". At url: " + url);
            System.out.println("*HtmlTest failed. Malformed Matcher: " + matcher + ". URL: " + url);
            return false;
        }

        return testSuccess;
    }

    @Override
    public boolean run(Map<String,String>nameSpaces){
        return this.run();
    }
}
