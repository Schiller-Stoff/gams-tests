package org.sebi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PojectTestCase {

    private Queue<Test> testCases;
    private String projectKey;
    private Map<String, String> projectNameSpaces;

    private static Logger log = LogManager.getLogger(PojectTestCase.class.getName());

    public PojectTestCase(String projectKey) {
        this.testCases = new LinkedList<>();
        this.projectNameSpaces = null;
        setProjectKey(projectKey);
    }

    public PojectTestCase(String projectKey, Map<String,String>nameSpaces) {
        this(projectKey);
        this.projectNameSpaces = nameSpaces;
    }


    public void add(Test test){
        log.trace("Called add from inside ProjecTestcase class.");
        testCases.add(test);
    }

    public Test remove(){
        log.trace("Called remove from inside " + this.getClass() + " class.");
        return testCases.remove();
    }

    public int size(){
        return testCases.size();
    }

    /**
     * Empties intern Queue testcases and calls .run() for each individual test inside.
     * If a namespace for the project is defined ..> it will call the overloaded .run(namespaces)
     * @return boolean if one failing test was inside.
     */
    public boolean runTests(){
        log.trace("called.");

        int totalTested = 0;
        int failCounter = 0;

        // for console output via scanner
        System.out.println("*** " + this.projectKey.toUpperCase() + " ***");

        Iterator<Test> iterator = getIterator();
        boolean projectTestSuccesful = true;
        while (iterator.hasNext()){
            Test curTest = remove();
            boolean testSuccess;
            if(projectNameSpaces == null){  // TODO: 21.02.2019 test clause
                testSuccess = curTest.run();
            } else {
                testSuccess = curTest.run(projectNameSpaces);
            }

            if(testSuccess){
                totalTested++;
                log.info("Tested project: " + projectKey + ". test success. Type of test: " + curTest.getClass() + ". Tested url: " + curTest.getUrl());
            } else {
                totalTested++;
                failCounter++;
                log.error("Tested project: " + projectKey + ". test failed. Type of test: " + curTest.getClass() + ". Tested url: " + curTest.getUrl());
                projectTestSuccesful = false;
            }
        }
        System.out.println("***** " + (totalTested - failCounter) + "/" + totalTested + " TestCases succeeded in project " + projectKey + "  *****\n");
        return projectTestSuccesful;
    }

    /**
     * Loops through stored Test class instances and adds given string
     * to their saved url at the beginning (url->PARAMurl)
     * Therefore uses method addUrlProtocolServer() from Test class.
     * @param protocolServer new name of server in url.
     */
    public void addToUrlStart(String protocolServer){
        log.trace("Called.");
        for (Test test:testCases) {
            test.addUrlProtocolServer(protocolServer);
        }
    }


    public Iterator<Test> getIterator(){
        return testCases.iterator();
    }   // TODO: 29.01.2019 really necessary?


    public Queue<Test> getTestCases() {
        return testCases;
    } // TODO: 28.01.2019 remove that method ...> will possibly break test methods.

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        // must later on match file names
        Pattern p = Pattern.compile("[^a-zA-Z0-9 ]");
        Matcher m = p.matcher(projectKey);
        boolean matchedSpecialCharacter = m.find();

        if(!matchedSpecialCharacter){
            this.projectKey = projectKey;
        } else {
            log.error("Matched a special character in a xml file's name, but isn't allowed! The input was: " + projectKey);
        }
    }

    public void setProjectNameSpaces(Map<String, String> projectNameSpaces) {
        this.projectNameSpaces = projectNameSpaces;
    }
}
