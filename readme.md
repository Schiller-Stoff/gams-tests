

# "Integrationstest" as standalone

Main purpose of the tool is to provide logging for the given tests (defined in xml files). Usage
inside a test-framework is not intended.
The logs will be created at project's root and provide information if and why intended testCase failed or not.
At ERROR lvl the program logs failing tests and wrong input/problems within this program (e.g. flawed xpath/url etc. inside the .xml files).


## Usage

First create a folder with the .xml files inside.
For the demanded structure of the .xml please see inside test/resources.
The settings file for the logging is at main/resources/log4j2.xml


```java
// e.g. String testFilesDir = System.getProperty("user.dir") + "/src/test/resources/testXml";
Suite testSuite = new Suite(testFilesDir);
testSuite.buildSuite();
testSuite.runAll();
```

- First instantiate Suite.java and give the directory path of the testfiles as String parameter (as above)
- Then run .buildSuite() to "fill" the suite instance with the "testvalues" given via the separate .xml files in the testfile's dir.
- via .runAll() all stored tests will be executed.
- via .changeZimServer("glossa") ...> changes all url's servername to "glossa". ("gams" also valid)

- individual tests can be run via .run("PROJECTNAME").



## Basic Functionality / Design

- see uml inside (material/uml/) <... also accessible via draw.io.
- The "program" is designed as standalone and imitates a TestFramework.


The test .xml files are read out by XmlReader.java and stored inside the hashmap of Suite.java.
Suite.java creates instances of polymorph Test.java (e.g. XmlCountTest.java or JsonTest.java)
and stores them inside individual ProjectTestCase.java (using ArrayLists), which are stored inside the Suite.java's hashmap.

The implementation how a test should be run are in the .run() method of the poylmorph Test.java like XmlCountTest.

So that Suite .runAll() --> ProjectTestCases .runTests() ---> runs the individual Tests via .run().

Inside Testrunner.java lie the "helper methods" e.g. to http get websites etc. for testing them later in the individual tests.
The Testrunner is a static singleton inside Test.java and used via inheritance in the tests (e.g. for getting html as string/document etc.)

