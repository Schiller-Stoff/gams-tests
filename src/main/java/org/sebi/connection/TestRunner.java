package org.sebi.connection;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class TestRunner {

    private static Logger log = LogManager.getLogger(TestRunner.class.getName());

    private static TestRunner instance;
    private TestRunner() {

    }

    public static TestRunner getInstance() {
        if (TestRunner.instance == null) {
            TestRunner.instance = new TestRunner();
        }
        return TestRunner.instance;
    }

    /**
     * Asks given website for the returning statuscode.
     * @param urlStr url to be checked given as String.
     * @return int of the statusCode.
     */
    public int getHttpStatusCode(String urlStr) throws NullPointerException{

        HttpResponse response = null;
        try {
            CloseableHttpClient http = SSLConnectionFactory.createTrustSelfSigned();
            response = http.execute(new HttpGet(urlStr));
            http.close();
        } catch (HttpHostConnectException ex){
            log.error("HttpHostConnectException: Connection to url: " + urlStr + " failed. Original message: " + ex.getMessage());
            throw new NullPointerException("HttpHostConnectException: Connection to url: " + urlStr + " failed. Original message: " + ex.getMessage());
        } catch (SSLException essl){
            log.error("SSLException at trying to connect to: " + urlStr + ". Origninal message: " + essl.getMessage());
            throw new NullPointerException("SSLException at trying to connect to: " + urlStr + ". Origninal message: " + essl.getMessage());
        } catch (Exception e){
            log.error("Unknown Exception at trying to connect to: " + urlStr + ". Error message: " + e.getMessage());
            throw new NullPointerException("Unknown Exception at trying to connect to: " + urlStr + ". Error message: " + e.getMessage());
        }

        int statusCode = response.getStatusLine().getStatusCode();

        return statusCode; // FIXME: 29.01.2019 might return 0 as statuscode!
    }

    /**
     * todo javadoc
     * @param url
     * @return
     * @throws NullPointerException
     */
    public String httpGetResponseBody(String url) throws NullPointerException{

        String body;
        try {
            body = SSLConnectionFactory.createTrustSelfSigned().execute(new HttpGet(url), new DefaultResponseHandler());
        } catch (KeyManagementException e){
            log.error("KeymanagementException at trying to connect to: " + url + ". There might be a problem with the TSL certification. " + e.getMessage());
            throw new NullPointerException("KeymanagementException at trying to connect to: " + url + ". There might be a problem with the TSL certification. " + e.getMessage());
        } catch (NoSuchAlgorithmException ex){
            log.error("NoSuchAlgorithmException at trying to connect to: " + url + ". There might be a problem with the TSL certification. " + ex.getMessage());
            throw new NullPointerException("NoSuchAlgorithmException at trying to connect to: " + url + ". There might be a problem with the TSL certification. " + ex.getMessage());
        } catch (KeyStoreException kex){
            log.error("KeyStoreException at trying to connect to: " + url + ". There might be a problem with the TSL certification. " + kex.getMessage());
            throw new NullPointerException("KeyStoreException at trying to connect to: " + url + ". There might be a problem with the TSL certification. " + kex.getMessage());
        } catch (IOException ioe){
            log.error("IOException at trying to connect to: " + url + ". Unable to get response. " + ioe.getMessage());
            throw new NullPointerException("IOException at trying to connect to: " + url + ". There might be a problem with the TSL certification. " + ioe.getMessage());
        } catch (IllegalArgumentException illa){
            log.error("IllegalArgumentException at trying to connect to: " + url + ". The url is malformed. " + illa.getMessage());
            throw new NullPointerException("IllegalArgumentException at trying to connect to: " + url + ". The url is malformed. " + illa.getMessage());
        }

        return body;
    }
}
