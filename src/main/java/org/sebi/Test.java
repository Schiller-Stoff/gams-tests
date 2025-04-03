package org.sebi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.net.ssl.SslConfiguration;
import org.sebi.connection.TestRunner;

import java.util.Map;

public class Test { // TODO: 21.02.2019 make class abstract class <... better suited!

    protected String url;
    protected static TestRunner runner;

    static {
        runner = TestRunner.getInstance();
    }

    private static Logger log = LogManager.getLogger(Test.class.getName());

    public Test(String url) {
        this.url = url;
    }

    /**
     * Method to be overridden by the extending TestClasses. Returns false.
     * @return false by default
     */
    public boolean run(){
        System.out.println(String.format("Testing -> %s", url));
        return false;
    }

    public boolean run(Map<String,String> nameSpaces){
        System.out.println(String.format("Testing -> %s", url));
        return false;
    }


    /**
     * Adds given string at the beginning of the Test instances url. e.g. /search="Dog" would change
     * to input/search="Dog"
     * @param protocolServer new url-beginning to set. (usually protocol + sercerName)
     */
    public void addUrlProtocolServer(String protocolServer){
        log.trace("Called.");
        String newUrl = protocolServer + url;
        setUrl(newUrl);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
//        if(url.contains("http://")|| url.contains("https://")){
//            this.url = url;
//            log.debug("Setting Server url to: " + url);
//        } else {
//            log.error("Can't find http:// or https:// in the string. Take care to set a propper url. The input was: " + url);
//        }

        this.url = url;
    }

}
