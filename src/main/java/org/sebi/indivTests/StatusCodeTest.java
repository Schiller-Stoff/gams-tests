package org.sebi.indivTests;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sebi.Test;

import java.util.Map;

public class StatusCodeTest extends Test {

    int expStatusCode;
    private static Logger log = LogManager.getLogger(StatusCodeTest.class.getName());

    public StatusCodeTest(String link) {
        super(link);
        expStatusCode = 200;
    }

    public StatusCodeTest(String link, int expStatusCode) {
        this(link);
        this.expStatusCode = expStatusCode;
    }

    @Override
    public boolean run() {
        super.run();
        int statusCode;
        try {
            statusCode = runner.getHttpStatusCode(url);
        } catch (NullPointerException e){
            System.out.println("*StatusCodeTest failed. Can't connect to " + url + ". Expected status: " + expStatusCode);
            log.error("Not able to receive statuscode from: " + url);
            return false;
        }

        try {   // TODO: 02.03.2019 remove unnecessary try catch block here ...> undefined statuscode is caught before
            if (statusCode == expStatusCode) {
                log.info("Http Status Code is as expected: " + expStatusCode + ". From url: " + url);
                return true;
            } else {
                log.error("Connection FAILED. At url " + url + " Status code is not " + expStatusCode + ". Status code is instead : " + statusCode);
                System.out.println("*StatusCodeTest failed. At url: " + url + " Actual Status Code: " + statusCode + ". Expected status: " + expStatusCode);
                return false;
            }
        } catch (NullPointerException e){
            log.error("Http/s connection failed. Statuscode is undefined. Awaited statusCode: " +  statusCode + ", at url: " + url);
            System.out.println("*StatusCodeTest failed. Can't connect to the website! Actual Status Code: " + statusCode + ". Expected status: " + expStatusCode);
            return false;
        }
    }

    @Override
    public boolean run(Map<String,String>nameSpaces){
        return this.run();
    }

    public int getExpStatusCode() {
        return expStatusCode;
    }

    public void setExpStatusCode(int expStatusCode) {
        this.expStatusCode = expStatusCode;
    }
}
