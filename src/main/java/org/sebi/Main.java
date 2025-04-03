package org.sebi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sebi.indivTests.HtmlTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.InputMismatchException;
import java.util.Properties;
import java.util.Scanner;

public class Main {

    private static Logger log = LogManager.getLogger(Main.class.getName());

    public static void main(String[] args) {

//        String testFilesDir = System.getProperty("user.dir") + "/src/test/resources/testXml";
//        Suite testSuite = new Suite(testFilesDir);
//        testSuite.buildSuite();
//        testSuite.runAll();

        // would run all tests for a project.
        // testSuite.run("cantus");

        Properties prop = null;
        Scanner scanner = new Scanner(System.in);

        String testXmlDir;
        String urlStart;

        // read out dir of xml files OR give via scanner
        try {
            prop = loadPropertyFile(System.getProperty("user.dir") + System.getProperty("file.separator") + "settings.properties");
            testXmlDir = readOutProperty("testXmlDir",prop);
            if(testXmlDir.toLowerCase().equals("default")){
                testXmlDir = System.getProperty("user.dir") + System.getProperty("file.separator") + "testXml";
                System.out.println("Accessing default testXml directory as demanded from property file at: " + testXmlDir);
            } else {
                testXmlDir = Paths.get(testXmlDir).toString();  //validates path
            }

        } catch (InvalidPathException ex){
            System.out.println("Path for test-xml in property file is invalid. Make sure to use only single '/' or double '\\\\' slashes. Single backward slashes are NOT allowed. Given path was: ");
            System.out.println(ex.getMessage());
            testXmlDir = askForXmlPath(scanner);
        } catch (NullPointerException e){
            System.out.println(e.getMessage());
            testXmlDir = askForXmlPath(scanner);
        }

        // read out serverName or state via scanner
        if(prop==null){
            urlStart = askForUrlStart(scanner);
        } else {
            try {
                urlStart = readOutProperty("server", prop);
                if(urlStart.endsWith("/"))throw new InputMismatchException("Wrong input in propertyfile at property 'server'. It's value must not end with '/', but given param was: " + urlStart);
            } catch (NullPointerException | InputMismatchException e) {
                System.out.println(e.getMessage());
                urlStart = askForUrlStart(scanner);
            }
        }

        if(testXmlDir.contains(".xml"))throw new InputMismatchException("You have to state the path to the test-xml(s) dir and not to the xml itself. Given path was: " + testXmlDir);

        Suite testSuite = new Suite(testXmlDir);
        try {
            testSuite.buildSuite();
        } catch (NullPointerException e){
            System.out.println("Reading out the test-xml files failed. Please make sure you correctly stated the path to the test-xml's directory. Given path was: " + testXmlDir);
            log.error("Reading out the test-xml files failed. Please make sure you correctly stated the path to the test-xml's directory. Given path was: " + testXmlDir);
            throw e;
        }

        testSuite.addUrlStart(urlStart);
        System.out.println("Testsuite succesfully launched.");
        soutUsage();
        provideConsoleControl(testSuite, scanner, urlStart);


        scanner.close();
    }

    public static void provideConsoleControl(Suite testSuite, Scanner scanner, String serverName){

        //Scanner scanner = new Scanner(System.in);
        boolean quit = false;

        while(!quit){
            String userInput = scanner.nextLine().toLowerCase();

            switch (userInput){

                case "help":
                    soutUsage();
                    break;

                case "runall":  // FIXME: 17.02.2019 should display error when suite is not filled!
                    System.out.println("Running tests...");
                    testSuite.runAll();
                    testSuite.buildSuite();
                    testSuite.addUrlStart(serverName);
                    System.out.println("Testrun finished"); // TODO: 17.02.2019 display somehow how many testruns failed from?
                    break;

                case "gams":
                    testSuite.buildSuite();
                    testSuite.addUrlStart("https://gams.uni-graz.at");
                    System.out.println("Protocol and server of all tests changed to https://gams.uni-graz.at");
                    break;

                case "glossa":
                    testSuite.buildSuite();
                    testSuite.addUrlStart("https://glossa.uni-graz.at");
                    System.out.println("Protocol and server of all tests changed to https://glossa.uni-graz.at");
                    break;

                case "reset":
                    testSuite.buildSuite();
                    testSuite.addUrlStart(serverName);
                    System.out.println(String.format("Protocol and server of all tests changed to %s", serverName));
                    break;

                case "quit":
                    System.out.println("Closing program...");
                    quit = true;
                    break;

                case "howto":
                    explainTestWorkflow();
                    break;

                default:
                    System.out.println("Please enter a valid command. Type 'help' to see all possible commands.");
                    break;
            }

        }

    }

    public static Properties loadPropertyFile(String propFilePath) throws NullPointerException{

        Properties prop = new Properties();

        /* read filestream */
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(propFilePath);
        } catch (FileNotFoundException e) {
            throw new NullPointerException("'settings.properties' could not be found. Searched for path: " + propFilePath +  ".\nMake sure that 'settings.properties' lies on same lvl as this .jar file.");
        }

        /* load file*/
        try {
            prop.load(fis);
            fis.close();        // ...> close FileInputStream again here!
            return prop;
        } catch (IOException e) {
            throw new NullPointerException("'settings.properties' could not be found. Searched for path: " + propFilePath +  ".\nMake sure that 'settings.properties' lies on same lvl as this .jar file.");
        }
    }

    public static String readOutProperty(String propName,Properties prop) throws NullPointerException {

        if(prop == null){
            throw new NullPointerException("Failed to access property-file.");
        }

        String propVal = prop.getProperty(propName);

        if(propVal == null){
            throw new NullPointerException("Failed to access property with name: " + propName + " in given property file.");
        }

        //remove whitespace at last
        propVal = propVal.replace(" ", "");

        return propVal;
    }

    public static String askForXmlPath(Scanner scanner){

        System.out.println("Please enter the dir in which the test .xml files are: ");
        String xmlFolderPath= "";
        //Scanner scanner = new Scanner(System.in);
        boolean quit = false;

        while (!quit){
            String userInput = scanner.nextLine();
            if(userInput.equals("quit")){
                throw new NullPointerException("User ended input. This is an intended error message.");
            }
            try {
                Paths.get(userInput);
                quit=true;
                xmlFolderPath = userInput;
            } catch (InvalidPathException | NullPointerException ex) {
                System.out.println("Invalid path given to the test-xml files. Please enter a valid path to the xml directory. Given input was: " + userInput);
                ex.getMessage();
            }
        }
        //scanner.close();      ...> do not close here! (causes errors later on otherwise)
        return xmlFolderPath;
    }

    /**
     * Uses scanner instance given as parameter to allow user to set the start of the urls of the
     * test instances inside the test suite. Url-start = protocol and servername like "https://google.com"
     * given string is not allowed to end with "/".
     * @param scanner scanner instance to handel console control
     * @return via console set url-start
     */
    public static String askForUrlStart(Scanner scanner){

        System.out.println("Please enter which protocol and server used for each testcase. E.g. \"https://google.com\" (Do not end input string with a \"/\")");
        String urlStart= "";
        boolean quit = false;

        while (!quit){
            String userInput = scanner.nextLine();
            if(userInput.equals("quit")){
                throw new NullPointerException("User ended input. This is an intended error message.");
            }
            if(!userInput.endsWith("/")){ // TODO: 02.03.2019 could also validate http or https occurence
                urlStart = userInput;
                System.out.println("Start of each urls set to " + urlStart + ".");
                quit = true;
            } else {
                System.out.println("Please do not end the input string with a \"/\".");
            }
        }
        return urlStart;
    }

    private static void soutUsage() {
        System.out.println("*** Available commands are ***");
        System.out.println(
            "'howto' -- explains how to add / update / share your project's test data'\n" +
            "'runAll' -- runs all tests in the suite.\n" +
            "'gams' -- changes protocol and server from all tests to 'https://gams.uni-graz.at'.\n" +
            "'glossa' -- changes protocol and server from all tests to 'https://glossa.uni-graz.at'.\n" +
            "'reset -- changes protocol and server from all tests to initally defined server in config or input.\n" +
            "'quit' -- closes the program.\n" +
            "'help' -- shows all possible commands.\n" +
            "***** ***** *****"
        );
    }

    private static void explainTestWorkflow(){
        System.out.println("1. SVN checkout project folder into the tool's testXml folder. (Like e.g.: /testXml/cantus) from location: https://gedra.uni-graz.at/svn/migrationstest/trunk/src/test/resources/org/emile/cirilo/gamstests-final/ (create one if none defined!)\n2. Refine / update / add meaningful tests. Compare with other project's tests if needed.\n3. Run tests via tool on different servers.\n4. SVN commit your changes!");
    }
}
