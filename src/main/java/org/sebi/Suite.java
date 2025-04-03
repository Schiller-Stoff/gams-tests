package org.sebi;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sebi.indivTests.HtmlTest;
import org.sebi.indivTests.JsonTest;
import org.sebi.indivTests.StatusCodeTest;
import org.sebi.indivTests.XmlCountTest;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;

public class Suite {

    private XmlReader xReader;
    private Map<String, org.sebi.PojectTestCase> suite;

    private static Logger log = LogManager.getLogger(Suite.class.getName());

    private final static HashMap<String, String> namespaces = new HashMap<String, String>();
    static {
        
        namespaces.put("skos", "http://www.w3.org/2004/02/skos/core#");
        namespaces.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        namespaces.put("xhtml", "http://www.w3.org/1999/xhtml");
        namespaces.put("fits", "http://hul.harvard.edu/ois/xml/ns/fits/fits_output");
        namespaces.put("mets", "http://www.loc.gov/METS/");
        namespaces.put("tei", "http://www.tei-c.org/ns/1.0");
        namespaces.put("g2o", "http://gams.uni-graz.at/ontology/#");
        namespaces.put("hssf", "urn:schemas-microsoft-com:office:spreadsheet");
        namespaces.put("cocoon", "http://apache.org/cocoon/request/2.0");
        namespaces.put("bibtex", "http://bibtexml.sf.net/");
        namespaces.put("mods", "http://www.loc.gov/mods/v3");
        namespaces.put("sparql", "http://www.w3.org/2001/sw/DataAccess/rf1/result");
        namespaces.put("s", "http://www.w3.org/2005/sparql-results#");
        namespaces.put("dc", "http://purl.org/dc/elements/1.1/");
        namespaces.put("lido", "http://www.lido-schema.org");
        namespaces.put("kml", "http://www.opengis.net/kml/2.2");
        namespaces.put("cmd", "http://www.clarin.eu/cmd/1");
        namespaces.put("tcf", "http://www.dspin.de/data/textcorpus");      
        namespaces.put("oai", "http://www.openarchives.org/OAI/2.0/");
        namespaces.put("gml", "http://www.opengis.net/gml");
        namespaces.put("geo", "http://www.opengis.net/ont/geosparql#");
    }

    public Suite(String testXmlDir) {
        this.xReader = new XmlReader(testXmlDir);
        this.suite = new HashMap<>();
    }

    /**
     * Just calls readOutFiles() inside.
     */
    public void buildSuite(){
        readTestfiles();
    }

    /**
     * Uses the XmlReader class to load the xml files located in the folder directory given at instantiation (for the xmls).
     * Calls a for loop for the files and then calls assembleTests() to create individual ProectTestCase instances.
     * Adds these instances to the hashmap with the filename as key value.
     */
    public void readTestfiles(){

        // first reset
        xReader.reset();
        suite.clear();

        Queue<Document> xmlFiles = xReader.loadXml();

        //read + push onto hashmap
        for(Document xml : xmlFiles){

            // +filter out textNodes
            NodeList nodeList = xReader.retrieveNodes(xml, "test");
            nodeList = xReader.removeTextnodes(nodeList);

            //putting into hashmap
            PojectTestCase projectTestCase = null;
            String xmlPath = xml.getDocumentURI();  //used for err logs
            String projectName = xml.getDocumentElement().getAttribute("name");
            projectName = FilenameUtils.getBaseName(projectName);

            try {
                projectTestCase = assembleTests(nodeList, projectName);
            } catch (InputMismatchException e){
                log.error("Wrong input/statement in project: " + projectName + ". Xml path is: " + xml.getDocumentURI());
                throw e;
            }

            // applying hardcoded namespaces from above.
            projectTestCase.setProjectNameSpaces(namespaces);

            try{
                if(suite.containsKey(projectName)){
                    throw new IllegalArgumentException("Illegal Argument Exception. (projectname already assigned) There are two identical-named projects inside the xml directory, which is not valid.  Given projectname was: " + projectName + ". One Duplicate found in " + xmlPath);
                }
                suite.put(projectName,projectTestCase);
            } catch (IllegalArgumentException e){
                log.error("Illegal Argument Exception. (projectname already assigned) There might are identical-named projects inside the xml directory, which is not valid. Given projectname was: " + projectName + ". DupliactionError at xml: " + xmlPath);
                throw e;
            }

            log.info("Put a ProjectTestCase for file " + projectName + " into the hashmap.");
        }
    }

    /**
     *
     *
     * @param nodeList awaits a List< nodes> "periodically" filled with < url>, < xpath> and < value>.
     * @return returns the Test-Object (HtmlTest etc.)
     */
    public PojectTestCase assembleTests(NodeList nodeList, String projectTestCaseKey) throws InputMismatchException{

        log.trace("Given parameter is: " + nodeList);

        String url = "";
        String xpath = "";
        String value = "";

        PojectTestCase projectTestCase = new PojectTestCase(projectTestCaseKey);

        for (int i = 0; i < nodeList.getLength(); i++) {

            Node curTestNode = nodeList.item(i);
            NodeList curChildren = curTestNode.getChildNodes();

            if(curChildren.getLength()<2){
                log.error("Given <test> in project " + projectTestCaseKey + " must at least have 2 child nodes, but found only: " + curChildren.getLength());
                throw new InputMismatchException("Given <test> in project " + projectTestCaseKey + " must at least have 2 child nodes, but found only: " + curChildren.getLength());
            }

            for (int j = 0; j < curChildren.getLength(); j++) {

                Node cur = curChildren.item(j);
                String curContent = cur.getTextContent(); //all user entries lower
                String tag = cur.getNodeName();

                // assuming <url> on first place inside <test> (except comments)
                if((j == 0)&&(!tag.equals("url"))){
                    if(tag.equals("#comment")){
                        log.debug("Comment detected on first position inside <test>. Verificitaion of nodes inside <test> set to ignore.");
                    } else {
                        log.error("The first node in given <test> tag must be <url> and not <" + tag + ">. ");
                        throw new InputMismatchException("The first node in given <test> tag must be <url> and not <" + tag + ">. ");   // FIXME: 28.01.2019 catch in readTestfiles! <... to get cur xml name!
                    }
                }

                // TODO: 28.01.2019 move to own method?!
                switch (tag){

                    case "url":
                        //here just fill url param.
                        url = cur.getTextContent();
                        break;

                    case "xpath":
                        projectTestCase.add(new XmlCountTest(url, curContent));
                        log.info("Created and added new XmlCountTest with parameters" + url + " and " + curContent + ".");
                        break;

                    case "json":
                        projectTestCase.add(new JsonTest(url,curContent));
                        log.info("Created and added new JsonTest with parameters" + url + " and " + curContent + ".");
                        break;

                    case "matcher":
                        projectTestCase.add(new HtmlTest(url, curContent));
                        log.info("Created and added new HtmlTest with parameters" + url + " and " + curContent + ".");
                        break;

                    case "statuscode":
                        projectTestCase.add(new StatusCodeTest(url,Integer.parseInt(curContent)));
                        log.info("Created and added new StatusCodeTest with parameters" + url + " and " + curContent + ".");
                        break;

                    case "#comment":
                        break;

                    case "#text":
                        break;

                    default:
                        log.error("Invalid node found in the current <test> in project:  " + projectTestCaseKey + ". There should be no node <" + tag + ">.");
                        throw new InputMismatchException("Invalid node found in the current <test> in project: " + projectTestCaseKey + ". There should be no node <" + tag + ">.");
                }
            }
        }
        return projectTestCase;
    }

    /**
     * todo javadoc
     * @param namespaceElems
     * @return
     */
    public Map<String,String> assembleNameSpace(NodeList namespaceElems, String projectTestCaseKey){   // TODO: 21.02.2019 test this
        log.trace(".");

        Map<String,String> nameSpaces = new HashMap<>();

        for (int i = 0; i < namespaceElems.getLength(); i++) {

            Node curNamespaceElem = namespaceElems.item(i);
            NodeList curNamespaceElemChildren = curNamespaceElem.getChildNodes();

            String prefix="";
            String uri="";

            if(curNamespaceElemChildren.getLength()<2){
                log.error("Given <namespace> must at least have 2 child nodes, but found only: " + curNamespaceElemChildren.getLength());
                throw new InputMismatchException("Given <namespace> must at least have 2 child nodes, but found only: " + curNamespaceElemChildren.getLength());
            }

            for (int j = 0; j < curNamespaceElemChildren.getLength(); j++) {

                Node curNode = curNamespaceElemChildren.item(j);
                String curContent = curNode.getTextContent().toLowerCase(); //all userEntries are lowerCase
                String curTagName = curNode.getNodeName();

                // only comment or prefix allowed as first node inside <namespace>
                if((j == 0)&&(!curTagName.equals("prefix"))){
                    if(curTagName.equals("#comment")){
                        log.debug("Comment detected on the first position in current <namespace>. Validation for <prefix> to be first ignored. In project: " + projectTestCaseKey);
                    } else {
                        log.error("The first node in given <namespace> tag must be <prefix> and not <" + curTagName + ">. ");
                        throw new InputMismatchException("The first node in given <namespace> tag must be <prefix> and not <" + curTagName + ">. Tested project: " + projectTestCaseKey);
                    }
                }

                switch (curTagName){
                    case "prefix":
                        prefix = curContent;
                        break;
                    case "uri":
                        uri = curContent;
                        break;
                    case "#text":
                        break;
                    case "#comment":
                        break;
                    default:
                        log.error("Invalid node found in the current <namespace> in project:  " + projectTestCaseKey + ". There should be no node <" + curTagName + ">.");
                        throw new InputMismatchException("Invalid node found in the current <namespace> in project: " + projectTestCaseKey + ". There should be no node <" + curContent + ">.");
                }
            }

            if(nameSpaces.containsKey(prefix)){
                String errMsg = "Prefix already assigned to the hashmap. No duplicated values are allowed. Please check the xml of the" +
                        " current project "+ projectTestCaseKey+" for duplicated namespaces. Doubled prefix: " + prefix;
                log.error(errMsg);
                throw new InputMismatchException(errMsg);
            }

            nameSpaces.put(prefix, uri);
            log.info("Namespace with prefix: " + prefix + " detected and put into the hashmap for project " + projectTestCaseKey + ". The uri is: " + uri);
        }

        return nameSpaces;
    }

    /**
     * Accesses intern hashmap with given keyvalue and runs returned ProjectTestcase.
     * @param fileName String keyValue = filename (without extension).
     * @return boolean if all tests inside the project ran succesfully
     */
    public boolean run(String fileName){
        PojectTestCase projectTests = suite.get(fileName);
        return projectTests.runTests();
    }

    /**
     * Loops through intern hashmap and runs all PojectTestCases and Tests inside.
     */
    public void runAll(){
        log.trace("called.");

        int failCounter = 0;

        System.out.println("\n+++++++++++ Test-Failure Report +++++++++++\n");
        Map<String, org.sebi.PojectTestCase> map = suite;
        for (Map.Entry<String, org.sebi.PojectTestCase> entry : map.entrySet())
        {
            log.debug("Running now tests for: " + entry.getKey());
            boolean testFail =  run(entry.getKey());
            if(!testFail)failCounter++;
        }
        if(failCounter>0) {
            System.out.println("\n+++++++++++ Testrun completed. Test failures were detected. +++++++++++\n");
        } else {
            System.out.println("\n+++++++++++ Testrun completed. All tests ran successfully. +++++++++++\n");
        }

    }

    /**
     * Adds to all test instances given string to the start of their
     * url. e.g. /o:example/home -> https://google.com/o:example/home when
     * start was given as param.
     * @param protocolServer String to add to the start of the test's url.
     *                       (usually protocoll and server)
     */
    public void addUrlStart(String protocolServer){
        log.trace("Called.");
        Map<String, org.sebi.PojectTestCase> map = suite;
        for (Map.Entry<String, org.sebi.PojectTestCase> entry : map.entrySet()) {
            entry.getValue().addToUrlStart(protocolServer);
        }
    }

    public XmlReader getxReader() {
        return xReader;
    }

    public Map<String, PojectTestCase> getSuite() {
        return suite;
    }

    public PojectTestCase getProjectTestCase(String key){

        if(!suite.containsKey(key)){
            log.error("Given projectname could not be found inside the suite hashmap. Given key is: " + key);
            throw new InputMismatchException("Given projectname could not be found inside the suite hashmap. Given key is: " + key);
        }
        return suite.get(key);
    }
}
