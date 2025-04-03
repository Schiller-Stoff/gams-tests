package org.sebi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class XmlReader {

    private String dirPath;
    private Queue<Document> xmlQueue;

    private static Logger log = LogManager.getLogger(XmlReader.class.getName());


    public XmlReader(String dirPath) {
        this.dirPath = dirPath;
        xmlQueue = new LinkedList<>();
    }

    public Document loadXml(File file){
        log.trace("method call. ");

        //TODO validate file here?

        Document curXml = null;

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            curXml = db.parse(file);
        } catch (ParserConfigurationException e){
            log.error("Something went wrong at parsing the XML with name: " + file.getName() + " . Thrown in " + this.getClass() +" inside .loadXml()");
            e.printStackTrace();
        } catch (SAXException e){
            log.error("SAXParser can't parse the XML with name: " + file.getName() + " . Thrown in " + this.getClass() +" inside .loadXml()");
            e.printStackTrace();
        } catch (IOException e) {
            log.error("IO Error at parsing the XML with name: " + file.getName() + " . Thrown in " + this.getClass() +" inside .loadXml()");
            e.printStackTrace();
        }

        if(checkTestXml(curXml)){ // FIXME: 29.01.2019 Possibly remove? maybe to specific for current operations ...> make checkTestXml() more generic! would be better in a class SuiteBuilder or directly in Suite.java
            log.info("xml file succesfully read in.");
            return curXml;
        } else {
            curXml = null;
            log.error("curXML is faulty and therefore set to null.");
            return curXml;
        }
    }

    public void loadXml(String folderPath){
        log.trace("overloaded loadXml(Folderpath).");

        File folder = new File(folderPath);

        // filter out files starting with "."
        File[] filteredFiles = folder.listFiles(file -> !file.getName().startsWith("."));

        for (final File fileEntry : filteredFiles) {
            if (fileEntry.isDirectory()) {
                String path = fileEntry.getAbsolutePath();
                log.debug("encountered a folder ...> recursively call loop.");
                loadXml(path); //<... overloaded called with String (path) will expect a folder.
            } else {
                Document xml = loadXml(fileEntry);
                xmlQueue.add(xml);     // <... with file will expect a xml
                log.info("xml file detected at and added to the queue:" + fileEntry.getAbsolutePath());
            }
        }
    }

    /**
     * Uses the intern dirPath variable to call overloaded loadXml(String), which
     * assumes a folder full of xmls loops through the directory and saves the individual documents
     * in the intern xmlQueue.
     * @return the intern xml-Queue filled with the individual xmls.
     */
    public Queue<Document> loadXml(){
        log.trace("Default loadXml() without params.(Using the directory given at instantiation)");
        loadXml(dirPath);
        return xmlQueue;
    }

    /**
     * Uses .getElementsByTagName() and returns the NodeList.
     * @param xml Document to be analyzed.
     * @param tagName Tagname of the elements to be accessed.
     * @return nodelist containing the wished elements.
     */
    public NodeList retrieveNodes(Document xml, String tagName){
        log.trace("Calling retrieveNodes.");

        NodeList nodeList = xml.getElementsByTagName(tagName);
        return nodeList;
    }

    /**
     * Removes direct child - text node AND comment elements from given node. Uses .getChildren()
     * and filter it's return. Does only refine direct children not nodes
     * inside nodes.
     * @param node Node to be filtered
     * @return Filtered Node without "direct child" textnodes and comments.
     */
    public Node removeTextnodes(Node node){
        log.trace(".");

        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {

            Node curNode = childNodes.item(i);
            if(curNode.getNodeName().equals("#text") || curNode.getNodeName().equals("#comment")){
                log.debug("removing textNode from node with name: " + curNode.getNodeName());
                node.removeChild(curNode);
            }
        }

        return node;
    }

    /**
     * Overloaded call. Loops through the nodelist and removes from each node
     * the direct-child text-nodes AND comments. Does only refine direct children not nodes
     * inside nodes.
     * @param nodelist NodeList of nodes to be refined.
     * @return Refined NodeList.
     */
    public NodeList removeTextnodes(NodeList nodelist){
        log.trace("Overloaded removeTextnodes() via nodeList.");

        //awaits a nodelist -- filters children of that nodes for textnodes and comments
        for (int i = 0; i < nodelist.getLength(); i++) {
            Node curNode = nodelist.item(i);
            removeTextnodes(curNode);
        }

        return nodelist;
    }


    public String getNodeVal(Node node){
        return node.getTextContent();
    }


    /**
     * Verifies that given xml - document has the roottag  < TestCase>. Otherwise throws a NoSuchElementException.
     * @param curXml xml to be analyzed.
     * @return true if roottag is correct
     */
    private boolean checkTestXml(Document curXml){
        String rootTag =  curXml.getDocumentElement().getTagName();
        String expRoot = "TestCase";

        if(rootTag.equals("TestCase")){
            log.info("Correct XML with root "+ rootTag +" read in.");
            return true;
        } else {
            log.error("Wrong XML read in. The rootTag is " + rootTag +" but should be: " + expRoot);
            throw new NoSuchElementException(" Wrong XML read in. The rootTag is " + rootTag +" but should be: " + expRoot);
        }
    }

    /**
     * Resets the intern xml store. Methods like loadXml() will add
     * to the intern xmlQueue and return that queue ..> will lead to aggregation
     * of the different calls. This method here allows to reset the intern queue if
     * only newly assigned xmls should be returned.
     */
    public void reset(){
        log.trace(".");
        xmlQueue.clear();
    }

    public String getDirPath() {
        return dirPath;
    }

    public void setDirPath(String dirPath) {
        this.dirPath = dirPath;
    }

    public Queue<Document> getXmlQueue() {
        return xmlQueue;    //FIXME better to remove and adapt tests to that <... hide complexity!
    }
}
