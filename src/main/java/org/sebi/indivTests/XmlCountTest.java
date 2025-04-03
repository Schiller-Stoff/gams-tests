package org.sebi.indivTests;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sebi.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xmlunit.XMLUnitException;
import org.xmlunit.assertj.MultipleNodeAssert;
import org.xmlunit.assertj.XmlAssert;
import org.xmlunit.xpath.XPathEngine;

import java.util.Map;

public class XmlCountTest extends Test {

    private static Logger log = LogManager.getLogger(XmlCountTest.class.getName());
    private String xPath;

    public XmlCountTest(String link, String xPath) {
        super(link);
        this.xPath = xPath;
    }

    /**
     * Calls Testrunner class: Uses intern url and xpath to get given xml (via url) and validates
     * existence of given xpath element. todo old method ...> allowed to run tests without namespace (not possible anymore) ..> remove / refactor!
     * @return boolean if element is found.
     */
    @Override
    public boolean run() {
        super.run();
        log.trace("called.");

        String xml;
        try {
            xml = runner.httpGetResponseBody(url);
        } catch (NullPointerException e){
            log.error("NullPointerException: No xml string received to analyze");
            System.out.println("*XmlTest failed. Failed to connect to website. Tested XPATH: " + xPath + ". Tested URL: " + url);
            return false;
        }

        // Via xmUnit assertThat
        boolean isInside;
        try {
            XmlAssert.assertThat(xml).hasXPath(xPath);
            log.info("Found xpath " + xPath + " at url: " + url);
            isInside = true;
        } catch (AssertionError e){
            log.error("follwoing xpath: \"" + xPath + "\" failed at url: " + url);
            log.error(e.getMessage());
            System.out.println("*XmlTest failed. Can't find element with xpath: " + xPath + ". Tested URL: " + url);
            isInside = false;
        }

        return isInside;
    }

    /**
     * Todo add javadoc here
     * @param namespaces
     * @return
     */
    public boolean run(Map<String, String> namespaces){ // TODO: 21.02.2019 unittest if works
        log.trace("called with namespace context.");
        super.run(namespaces);

        String xml;
        try {
            xml = runner.httpGetResponseBody(url);
        } catch (NullPointerException e){
            log.error("NullPointerException: No xml string received to analyze");
            System.out.println("*XmlTest failed. Failed to connect to website. Tested XPATH: " + xPath + ". Tested URL: " + url);
            return false;
        }

        //via xml unit assertthat
        boolean isInside = false;
        try {
            XmlAssert.assertThat(xml).withNamespaceContext(namespaces).hasXPath(xPath);
            isInside = true;
            log.info("Element(s) found in current file. Tested url: " + url + ". Tested xpath: " + xPath + ".");
        } catch (AssertionError e){
            log.error("AssertionError: Specified element(s) NOT found in analyzed xml! Tested url: " + url + ". Tested xpath: " + xPath +  ". " + e.getMessage());
            System.out.println("*XmlTest failed. Cannot find element.Tested xpath:  " + xPath + ". Under url: " + url);
        }


        return isInside;

    }

}
