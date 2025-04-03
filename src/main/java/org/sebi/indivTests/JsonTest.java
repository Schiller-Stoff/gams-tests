package org.sebi.indivTests;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sebi.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.ArraySizeComparator;

import java.util.Map;

public class JsonTest extends Test {

    private static Logger log = LogManager.getLogger(JsonTest.class.getName());
    String expJson;

    public JsonTest(String link, String expObj) {
        super(link);
        this.expJson = expObj;
    }

    @Override
    public boolean run() {  // TODO: 08.02.2019 atm works only against plain json objects NOT against json arrays
        super.run();
        log.trace("called.");

        boolean testSuccess = false;
        String actJson;
        try {
            actJson = runner.httpGetResponseBody(url);
        } catch (NullPointerException e){
            log.error("*JSONTest failed. Unable to get json from " + url + ". Tried to match json: " + expJson);
            System.out.println("*JSONTest failed. Unable to get json from " + url + ". Tried to match json: " + expJson);
            return false;
        }

        //check if incoming is json array or json object
        boolean isJsonArray = false;
        try {
            new JSONObject(actJson);
        } catch (JSONException e){
            // inside test if Json array
            try {
                new JSONArray(actJson);
                isJsonArray = true;
            } catch (JSONException ex) {
                log.error("Could not parse received JSON. The Json might not be valid from url: " + url);
                //throw new InputMismatchException("Could not parse received JSON. The Json might not be valid from url: " + url);
            }
        }

        //test for object else for array
        if(!isJsonArray){
            try {
                JSONAssert.assertEquals(expJson, actJson,false);
                log.info("Succesfully matched Json-object at url: " + url);
                testSuccess = true;
            } catch (AssertionError e) {
                log.error("AssertionError: Couldn't find expected jsonObject at url: " + url + ". Received Json: " + actJson + ". Json object that couldn't be found : " + expJson +  ". \n " + e.getMessage());
                System.out.println("*JsonTest failed. URL: " + url + ". Matcher: " + expJson);
                //e.printStackTrace();
            } catch (JSONException ex){
                log.error("JSONException: Unparsable Json " + ex.getMessage() + "Given url was: " + url + ". Json query is: " + expJson);
                System.out.println("*JSONTest failed. Can't parse given Json." + ex.getMessage() + ". Given url was: " + url +". Json query is " + expJson);
                //ex.printStackTrace();
            }
        } else {
            try {
                JSONAssert.assertEquals(expJson, actJson, new ArraySizeComparator(JSONCompareMode.LENIENT));
                testSuccess = true;
            } catch (AssertionError e){
                log.error("AssertionError: Json array has different size at url: " + url + ". Received Json: " + actJson + ". Json array that couldn't be found : " + expJson +  ". \n " + e.getMessage());
                System.out.println("*JsonTest (jsonArray) failed. URL: " + url + ". Expected size of array (=given Matcher): " + expJson);
                System.out.println(e.getMessage());
            } catch (JSONException ex){
                log.error("JSONException: Unparsable Json " + ex.getMessage() + "Given url was: " + url + ". Json query is: " + expJson);
                System.out.println("*JSONTest failed. (JsonArray) Can't parse given Json." + ex.getMessage() + ". Given url was: " + url +". Json query is " + expJson);
            }

        }


        return testSuccess;
    }

    @Override
    public boolean run(Map<String,String> nameSpaces){
        return this.run();
    }




    public String getExpJson() {
        return expJson;
    }

    public void setExpJson(String expJson) {
        this.expJson = expJson;
    }
}
