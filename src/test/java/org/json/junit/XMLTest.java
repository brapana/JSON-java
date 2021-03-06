package org.json.junit;

/*
Copyright (c) 2020 JSON.org

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

The Software shall be used for Good, not Evil.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;

import org.json.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;


/**
 * Tests for JSON-Java XML.java
 * Note: noSpace() will be tested by JSONMLTest
 */
public class XMLTest {
    /**
     * JUnit supports temporary files and folders that are cleaned up after the test.
     * https://garygregory.wordpress.com/2010/01/20/junit-tip-use-rules-to-manage-temporary-files-and-folders/ 
     */
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();






    // Tests for Milestone 2:


    // Part 1 Tests
    /**
     * Get JSONObject from path, using a path that doesnt exist
     * Expects an empty jsonObject returned
     */
    @Test
    public void part1NonexistentPath() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("./xmls/Issue537.xml"));
            JSONPointer pointer = XML.buildJSONPointerFromPath("doesntexist");
            JSONObject jsonObject = XML.toJSONObject(reader, pointer);
            assertTrue("jsonObject should be empty", jsonObject.isEmpty());
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }

    }

    /**
     * Get JSONObject from path, using an empty path
     * Expects a jsonObject containing the entire XML file
     */
    @Test
    public void part1EmptyPath() {
        try {
            BufferedReader reader1 = new BufferedReader(new FileReader("./xmls/Issue537.xml"));
            BufferedReader reader2 = new BufferedReader(new FileReader("./xmls/Issue537.xml"));
            JSONPointer pointer1 = XML.buildJSONPointerFromPath("");

            // read xml with plain toJSONObject, ensure its the same as the empty path version
            JSONObject jsonObject = XML.toJSONObject(reader1, pointer1);
            JSONObject jsonObject2 = XML.toJSONObject(reader2);
            assertEquals(jsonObject.toString(), jsonObject2.toString());
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }

    }

    /**
     * Get JSONObject from path, using a valid path that is not an innermost key
     * Expects a jsonObject containing the last key in the paths' key and its contents {brief_summary: {[contents]}}
     */
    @Test
    public void part1ValidPath() {
        try {
            BufferedReader reader1 = new BufferedReader(new FileReader("./xmls/Issue537.xml"));
            JSONPointer pointer1 = XML.buildJSONPointerFromPath("clinical_study/brief_summary");

            // ensure the read in object at the specific path is equal to the json that should be found there
            JSONObject jsonObject = XML.toJSONObject(reader1, pointer1);
            JSONObject jsonObject2 = new JSONObject("{\"brief_summary\": {\"textblock\": \"CLEAR SYNERGY is an international multi center 2x2 randomized placebo controlled trial of\"}}");
            assertEquals(jsonObject.toString(), jsonObject2.toString());
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }

    }

    /**
     * Get JSONObject from path, using a valid path that is an innermost key
     * Expects a jsonObject containing the last key in the paths' key and its value {brief_summary: value}
     */
    @Test
    public void part1InnermostPath() {
        try {
            BufferedReader reader1 = new BufferedReader(new FileReader("./xmls/Issue537.xml"));
            JSONPointer pointer1 = XML.buildJSONPointerFromPath("clinical_study/brief_summary/textblock");

            // ensure the read in object at the specific path is equal to the json that should be found there
            JSONObject jsonObject = XML.toJSONObject(reader1, pointer1);
            JSONObject jsonObject2 = new JSONObject("{\"textblock\": \"CLEAR SYNERGY is an international multi center 2x2 randomized placebo controlled trial of\"}");
            assertEquals(jsonObject.toString(), jsonObject2.toString());
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }

    }

    /**
     * JSONObject from a malformed xml file
     * Expects an empty jsonObject
     */
    @Test
    public void part1MalformedXML() {
        try {
            BufferedReader reader1 = new BufferedReader(new FileReader("./xmls/Issue537-malformed.xml"));
            JSONPointer pointer1 = XML.buildJSONPointerFromPath("clinical_study/brief_summary");

            // ensure the read in object at the specific path is equal to the json that should be found there
            JSONObject jsonObject = XML.toJSONObject(reader1, pointer1);

            assertTrue(jsonObject.isEmpty());
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }
    }

    // Part 2 Tests

    /**
     * Get JSONObject from path, using a path that doesnt exist
     * Expects a JSONObject containing the entirely of the unchanged XML file
     */
    @Test
    public void part2NonexistentPath() {
        try {
            BufferedReader reader1 = new BufferedReader(new FileReader("./xmls/Issue537.xml"));
            BufferedReader reader2 = new BufferedReader(new FileReader("./xmls/Issue537.xml"));
            JSONPointer pointer1 = XML.buildJSONPointerFromPath("doesntexist");
            JSONObject replacement = new JSONObject("{\"newKey1\": \"newValue1\", \"newKey2\": \"newValue2\", \"newKey3\": \"newValue3\"}");

            // ensure the function when passed in a nonexistent path returns the same as the buit in function
            JSONObject jsonObject = XML.toJSONObject(reader1, pointer1, replacement);
            JSONObject jsonObject2 = XML.toJSONObject(reader2);

            assertEquals(jsonObject.toString(), jsonObject2.toString());
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }

    }

    /**
     * Get JSONObject from path, using an empty path
     * Expects a jsonObject containing the replacement json
     */
    @Test
    public void part2EmptyPath() {
        try {
            BufferedReader reader1 = new BufferedReader(new FileReader("./xmls/Issue537.xml"));
            JSONPointer pointer1 = XML.buildJSONPointerFromPath("");
            JSONObject replacement = new JSONObject("{\"newKey1\": \"newValue1\", \"newKey2\": \"newValue2\", \"newKey3\": \"newValue3\"}");

            // ensure the returned object is simply the replacement object
            JSONObject jsonObject = XML.toJSONObject(reader1, pointer1, replacement);
            assertEquals(jsonObject.toString(), replacement.toString());
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }

    }

    /**
     * Get JSONObject from path, using a valid path that is not an innermost key
     * Expects a jsonObject containing the xml contents replaced with the replacement object
     */
    @Test
    public void part2ValidPath() {
        try {
            BufferedReader reader1 = new BufferedReader(new FileReader("./xmls/Issue537.xml"));
            BufferedReader reader2 = new BufferedReader(new FileReader("./xmls/Issue537-part2ValidPath.json"));
            JSONPointer pointer1 = XML.buildJSONPointerFromPath("clinical_study/brief_summary");
            JSONObject replacement = new JSONObject("{\"newKey1\": \"newValue1\", \"newKey2\": \"newValue2\", \"newKey3\": \"newValue3\"}");

            // ensure the final JSONObject is equal to the original with the given path replaced
            JSONObject jsonObject = XML.toJSONObject(reader1, pointer1, replacement);
            JSONObject jsonObject2 = new JSONObject(new JSONTokener(reader2));
            assertEquals(jsonObject.toString(), jsonObject2.toString());
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }

    }

    /**
     * Get JSONObject from path, using a valid path that is the innermost key
     * Expects a jsonObject containing the key's contents replaced with the replacement object
     */
    @Test
    public void part2InnermostPath() {
        try {
            BufferedReader reader1 = new BufferedReader(new FileReader("./xmls/Issue537.xml"));
            BufferedReader reader2 = new BufferedReader(new FileReader("./xmls/Issue537-part2InnermostPath.json"));
            JSONPointer pointer1 = XML.buildJSONPointerFromPath("clinical_study/brief_summary/textblock");
            JSONObject replacement = new JSONObject("{\"newKey1\": \"newValue1\", \"newKey2\": \"newValue2\", \"newKey3\": \"newValue3\"}");

            // ensure the final JSONObject is equal to the original with the given innermost key path replaced
            JSONObject jsonObject = XML.toJSONObject(reader1, pointer1, replacement);
            JSONObject jsonObject2 = new JSONObject(new JSONTokener(reader2));
            assertEquals(jsonObject.toString(), jsonObject2.toString());
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }

    }

    /**
     * JSONObject from a malformed xml file
     * Expects a JSONException thrown
     */
    @Test(expected=JSONException.class)
    public void part2MalformedXML() {
        try {
            BufferedReader reader1 = new BufferedReader(new FileReader("./xmls/Issue537-malformed.xml"));
            JSONPointer pointer1 = XML.buildJSONPointerFromPath("clinical_study/brief_summary");
            JSONObject replacement = new JSONObject("{\"newKey1\": \"newValue1\", \"newKey2\": \"newValue2\", \"newKey3\": \"newValue3\"}");

            // ensure that reading in the malformed xml object throws a JSONException
            JSONObject jsonObject = XML.toJSONObject(reader1, pointer1, replacement);
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }
    }


    // END Milestone 2 Tests


    // Milestone 3 Tests

    /**
     * Read in XML file to JSONObject, adding "swe262_" to the beginning of each key
     * Expects a jsonObject containing the json version of the passed in XML, with each key being transformed by
     * the keyTransformer "simpleTransformer", that adds "swe262_" to the beginning of the string
     */
    @Test
    public void milestone3SimpleTransformer() {
        try {
            BufferedReader reader1 = new BufferedReader(new FileReader("./xmls/Issue537-snippet.xml"));
            BufferedReader reader2 = new BufferedReader(new FileReader("./xmls/Issue537-snippet-swe262Prefix.json"));

            Function<String, String> simpleTransformer = (String key)->( "swe262_" + key );

            // ensure that the read in xml (Issue537-snippet.xml) is properly transformed to resemble
            // Issue537-snippet-swe262Prefix.json (Issue537-snippet.xml converted to JSON by built in XML.toJSONObject()
            // with manually added swe262_ prefixes on each tag)
            JSONObject jsonObject1 = XML.toJSONObject(reader1, simpleTransformer);
            JSONObject jsonObject2 = new JSONObject(new JSONTokener(reader2));

            assertEquals(jsonObject2.toString(), jsonObject1.toString());

        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }

    }

    /**
     * Read in XML file to JSONObject, with an "identity" transformer that does not change the key
     * Expects a jsonObject that is the same as the jsonObject returned by the built-in XML.toJSONObject()
     */
    @Test
    public void milestone3IdentityTransformer() {
        try {
            BufferedReader reader1 = new BufferedReader(new FileReader("./xmls/Issue537.xml"));
            BufferedReader reader2 = new BufferedReader(new FileReader("./xmls/Issue537.xml"));

            // identity transformer that returns the same string that was passed in
            Function<String, String> identityTransformer = (String key)->( key );

            // ensure that the read in xml using the identityTransformer returns the exact same JSONObject
            // as the built in XML.toJSONObject would for the entire Issue537.xml
            JSONObject jsonObject1 = XML.toJSONObject(reader1, identityTransformer);
            JSONObject jsonObject2 = XML.toJSONObject(reader2);

            assertEquals(jsonObject2.toString(), jsonObject1.toString());

        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }
    }

    /**
     * JSONObject from a malformed xml file
     * Expects a JSONException thrown
     */
    @Test(expected=JSONException.class)
    public void milestone3MalformedXML() {
        try {
            BufferedReader reader1 = new BufferedReader(new FileReader("./xmls/Issue537-malformed.xml"));
            Function<String, String> simpleTransformer = (String key)->( "swe262_" + key );

            // ensure that reading in the malformed xml object throws a JSONException
            JSONObject jsonObject = XML.toJSONObject(reader1, simpleTransformer);
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }
    }

    // END Milestone 3 Tests


    // Milestone 5 Tests

    /**
     * Tests reading in two XMLs via the new concurrent XML.toJSONObject(reader, Consumer, Consumer).
     * large2.xml is 18MB while Issue537.xml is 8KB. Even though reading large2.xml starts first,
     * reader2.xml should finish first, and therefore its JSONObject will be placed/retrieved from
     * the blockingqueue before large2's. (blockingQueue take() blocks until a JSONObject is
     * added via the concurrent toJSONObject())
     */
    @Test
    public void milestone5ConcurrentOperations() {
        try {
            BufferedReader reader1 = new BufferedReader(new FileReader("./xmls/large2.xml"));
            BufferedReader reader2 = new BufferedReader(new FileReader("./xmls/Issue537.xml"));
            BufferedReader reader3 = new BufferedReader(new FileReader("./xmls/large2.xml"));
            BufferedReader reader4 = new BufferedReader(new FileReader("./xmls/Issue537.xml"));

            // blocking queue to add the JSONObject to
            BlockingQueue<JSONObject> bqueue = new LinkedBlockingQueue<>();

            // read in large2.xml and add it to bqueue
            XML.toJSONObject(reader1, (json)->{
                try {
                    bqueue.put(json);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, (e)->{e.printStackTrace();});

            // read in Issue537.xml and add it to bqueue
            XML.toJSONObject(reader2, (json)->{
                try {
                    bqueue.put(json);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, (e)->{e.printStackTrace();});

            // assert the first JSONObject added to the blockingqueue is the smaller one (Issue537.xml)
            assertEquals(XML.toJSONObject(reader4).toString(), bqueue.take().toString());

            // assert the second JSONObject added to the blockingqueue is the larger one (large2.xml)
            assertEquals(XML.toJSONObject(reader3).toString(), bqueue.take().toString());

        } catch (IOException | InterruptedException x) {
            System.err.format("IOException: %s%n", x);
        }
    }

    /**
     * Tests reading in an XML via the new concurrent XML.toJSONObject(reader, Consumer, Consumer).
     * The JSONObject output is added to a blockingqueue which is then asserted against the same
     * XML converted to JSON via XML.toJSONObject(reader) (blockingQueue take() blocks until a JSONObject is
     * added via the concurrent toJSONObject())
     */
    @Test
    public void milestone5CorrectlyReadXML() {
        try {
            BufferedReader reader1 = new BufferedReader(new FileReader("./xmls/Issue537.xml"));
            BufferedReader reader2 = new BufferedReader(new FileReader("./xmls/Issue537.xml"));

            // blocking queue to add the JSONObject to
            BlockingQueue<JSONObject> bqueue = new LinkedBlockingQueue<>();

            // read in Issue537.xml and add it to bqueue
            XML.toJSONObject(reader1, (json)->{
                try {
                    bqueue.put(json);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, (e)->{e.printStackTrace();});

            // assert the JSONObject returned from the concurrent function is the same as the built-in function
            assertEquals(XML.toJSONObject(reader2).toString(), bqueue.take().toString());

        } catch (IOException | InterruptedException x) {
            System.err.format("IOException: %s%n", x);
        }
    }

    /**
     * Tests reading in an XML via the new concurrent XML.toJSONObject(reader, Consumer, Consumer).
     * The Issue537-malformed.xml conversion results in a JSONException that should be passed into the second
     * functional passed in (puts the exception into the blockingqueue). This exception placed in the blocking queue
     * is asserted to be of type JSONException
     * (blockingQueue take() blocks until an Exception is added via the concurrent toJSONObject())
     */
    @Test
    public void milestone5ExceptionLambda() {
        try {
            BufferedReader reader1 = new BufferedReader(new FileReader("./xmls/Issue537-malformed.xml"));

            // add the exception output
            BlockingQueue<Exception> bqueue = new LinkedBlockingQueue<>();

            // ensure that reading in the malformed xml object throws a JSONException that then results in the
            // second lambda passed in (onFailure) is ran with the Exception as an argument (should be JSONException)
            XML.toJSONObject(reader1, (json)->{System.out.println(json.toString());}, (e)->{
                try {
                    bqueue.put(e);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            });

            // assert the exception returned from the concurrent toJSONObject() is of class JSONException
            assertTrue(bqueue.take() instanceof JSONException);
        } catch (IOException | InterruptedException x) {
            System.err.format("IOException: %s%n", x);
        }
    }

    // END Milestone 5 Tests

    /**
     * JSONObject from a null XML string.
     * Expects a NullPointerException
     */
    @Test(expected=NullPointerException.class)
    public void shouldHandleNullXML() {
        String xmlStr = null;
        JSONObject jsonObject = XML.toJSONObject(xmlStr);
        assertTrue("jsonObject should be empty", jsonObject.isEmpty());
    }

    /**
     * Empty JSONObject from an empty XML string.
     */
    @Test
    public void shouldHandleEmptyXML() {

        String xmlStr = "";
        JSONObject jsonObject = XML.toJSONObject(xmlStr);
        assertTrue("jsonObject should be empty", jsonObject.isEmpty());
    }

    /**
     * Empty JSONObject from a non-XML string.
     */
    @Test
    public void shouldHandleNonXML() {
        String xmlStr = "{ \"this is\": \"not xml\"}";
        JSONObject jsonObject = XML.toJSONObject(xmlStr);
        assertTrue("xml string should be empty", jsonObject.isEmpty());
    }

    /**
     * Invalid XML string (tag contains a frontslash).
     * Expects a JSONException
     */
    @Test
    public void shouldHandleInvalidSlashInTag() {
        String xmlStr = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
            "<addresses xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""+
            "   xsi:noNamespaceSchemaLocation='test.xsd'>\n"+
            "    <address>\n"+
            "       <name/x>\n"+
            "       <street>abc street</street>\n"+
            "   </address>\n"+
            "</addresses>";
        try {
            XML.toJSONObject(xmlStr);
            fail("Expecting a JSONException");
        } catch (JSONException e) {
            assertEquals("Expecting an exception message",
                    "Misshaped tag at 176 [character 14 line 4]",
                    e.getMessage());
        }
    }

    /**
     * Invalid XML string ('!' char in tag)
     * Expects a JSONException
     */
    @Test
    public void shouldHandleInvalidBangInTag() {
        String xmlStr = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
            "<addresses xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""+
            "   xsi:noNamespaceSchemaLocation='test.xsd'>\n"+
            "    <address>\n"+
            "       <name/>\n"+
            "       <!>\n"+
            "   </address>\n"+
            "</addresses>";
        try {
            XML.toJSONObject(xmlStr);
            fail("Expecting a JSONException");
        } catch (JSONException e) {
            assertEquals("Expecting an exception message",
                    "Misshaped meta tag at 214 [character 12 line 7]",
                    e.getMessage());
        }
    }

    /**
     * Invalid XML string ('!' char and no closing tag brace)
     * Expects a JSONException
     */
    @Test
    public void shouldHandleInvalidBangNoCloseInTag() {
        String xmlStr = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
            "<addresses xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""+
            "   xsi:noNamespaceSchemaLocation='test.xsd'>\n"+
            "    <address>\n"+
            "       <name/>\n"+
            "       <!\n"+
            "   </address>\n"+
            "</addresses>";
        try {
            XML.toJSONObject(xmlStr);
            fail("Expecting a JSONException");
        } catch (JSONException e) {
            assertEquals("Expecting an exception message",
                    "Misshaped meta tag at 213 [character 12 line 7]",
                    e.getMessage());
        }
    }

    /**
     * Invalid XML string (no end brace for tag)
     * Expects JSONException
     */
    @Test
    public void shouldHandleNoCloseStartTag() {
        String xmlStr = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
            "<addresses xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""+
            "   xsi:noNamespaceSchemaLocation='test.xsd'>\n"+
            "    <address>\n"+
            "       <name/>\n"+
            "       <abc\n"+
            "   </address>\n"+
            "</addresses>";
        try {
            XML.toJSONObject(xmlStr);
            fail("Expecting a JSONException");
        } catch (JSONException e) {
            assertEquals("Expecting an exception message",
                    "Misplaced '<' at 193 [character 4 line 6]",
                    e.getMessage());
        }
    }

    /**
     * Invalid XML string (partial CDATA chars in tag name)
     * Expects JSONException
     */
    @Test
    public void shouldHandleInvalidCDATABangInTag() {
        String xmlStr = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
            "<addresses xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""+
            "   xsi:noNamespaceSchemaLocation='test.xsd'>\n"+
            "    <address>\n"+
            "       <name>Joe Tester</name>\n"+
            "       <![[]>\n"+
            "   </address>\n"+
            "</addresses>";
        try {
            XML.toJSONObject(xmlStr);
            fail("Expecting a JSONException");
        } catch (JSONException e) {
            assertEquals("Expecting an exception message",
                    "Expected 'CDATA[' at 204 [character 11 line 5]",
                    e.getMessage());
        }
    }

    /**
     * Null JSONObject in XML.toString()
     */
    @Test
    public void shouldHandleNullJSONXML() {
        JSONObject jsonObject= null;
        String actualXml=XML.toString(jsonObject);
        assertEquals("generated XML does not equal expected XML","\"null\"",actualXml);
    }

    /**
     * Empty JSONObject in XML.toString()
     */
    @Test
    public void shouldHandleEmptyJSONXML() {
        JSONObject jsonObject= new JSONObject();
        String xmlStr = XML.toString(jsonObject);
        assertTrue("xml string should be empty", xmlStr.isEmpty());
    }

    /**
     * No SML start tag. The ending tag ends up being treated as content.
     */
    @Test
    public void shouldHandleNoStartTag() {
        String xmlStr = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
            "<addresses xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""+
            "   xsi:noNamespaceSchemaLocation='test.xsd'>\n"+
            "    <address>\n"+
            "       <name/>\n"+
            "       <nocontent/>>\n"+
            "   </address>\n"+
            "</addresses>";
        String expectedStr = 
            "{\"addresses\":{\"address\":{\"name\":\"\",\"nocontent\":\"\",\""+
            "content\":\">\"},\"xsi:noNamespaceSchemaLocation\":\"test.xsd\",\""+
            "xmlns:xsi\":\"http://www.w3.org/2001/XMLSchema-instance\"}}";
        JSONObject jsonObject = XML.toJSONObject(xmlStr);
        JSONObject expectedJsonObject = new JSONObject(expectedStr);
        Util.compareActualVsExpectedJsonObjects(jsonObject,expectedJsonObject);
    }

    /**
     * Valid XML to JSONObject
     */
    @Test
    public void shouldHandleSimpleXML() {
        String xmlStr = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
            "<addresses xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""+
            "   xsi:noNamespaceSchemaLocation='test.xsd'>\n"+
            "   <address>\n"+
            "       <name>Joe Tester</name>\n"+
            "       <street>[CDATA[Baker street 5]</street>\n"+
            "       <NothingHere/>\n"+
            "       <TrueValue>true</TrueValue>\n"+
            "       <FalseValue>false</FalseValue>\n"+
            "       <NullValue>null</NullValue>\n"+
            "       <PositiveValue>42</PositiveValue>\n"+
            "       <NegativeValue>-23</NegativeValue>\n"+
            "       <DoubleValue>-23.45</DoubleValue>\n"+
            "       <Nan>-23x.45</Nan>\n"+
            "       <ArrayOfNum>1, 2, 3, 4.1, 5.2</ArrayOfNum>\n"+
            "   </address>\n"+
            "</addresses>";

        String expectedStr = 
            "{\"addresses\":{\"address\":{\"street\":\"[CDATA[Baker street 5]\","+
            "\"name\":\"Joe Tester\",\"NothingHere\":\"\",TrueValue:true,\n"+
            "\"FalseValue\":false,\"NullValue\":null,\"PositiveValue\":42,\n"+
            "\"NegativeValue\":-23,\"DoubleValue\":-23.45,\"Nan\":-23x.45,\n"+
            "\"ArrayOfNum\":\"1, 2, 3, 4.1, 5.2\"\n"+
            "},\"xsi:noNamespaceSchemaLocation\":"+
            "\"test.xsd\",\"xmlns:xsi\":\"http://www.w3.org/2001/"+
            "XMLSchema-instance\"}}";

        compareStringToJSONObject(xmlStr, expectedStr);
        compareReaderToJSONObject(xmlStr, expectedStr);
        compareFileToJSONObject(xmlStr, expectedStr);
    }

    /**
     * Tests to verify that supported escapes in XML are converted to actual values.
     */
    @Test
    public void testXmlEscapeToJson(){
        String xmlStr = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
            "<root>"+
            "<rawQuote>\"</rawQuote>"+
            "<euro>A &#8364;33</euro>"+
            "<euroX>A &#x20ac;22&#x20AC;</euroX>"+
            "<unknown>some text &copy;</unknown>"+
            "<known>&#x0022; &quot; &amp; &apos; &lt; &gt;</known>"+
            "<high>&#x1D122; &#x10165;</high>" +
            "</root>";
        String expectedStr = 
            "{\"root\":{" +
            "\"rawQuote\":\"\\\"\"," +
            "\"euro\":\"A €33\"," +
            "\"euroX\":\"A €22€\"," +
            "\"unknown\":\"some text &copy;\"," +
            "\"known\":\"\\\" \\\" & ' < >\"," +
            "\"high\":\"𝄢 𐅥\""+
            "}}";
        
        compareStringToJSONObject(xmlStr, expectedStr);
        compareReaderToJSONObject(xmlStr, expectedStr);
        compareFileToJSONObject(xmlStr, expectedStr);
    }
    
    /**
     * Tests that control characters are escaped.
     */
    @Test
    public void testJsonToXmlEscape(){
        final String jsonSrc = "{\"amount\":\"10,00 €\","
                + "\"description\":\"Ação Válida\u0085\","
                + "\"xmlEntities\":\"\\\" ' & < >\""
                + "}";
        JSONObject json = new JSONObject(jsonSrc);
        String xml = XML.toString(json);
        //test control character not existing
        assertFalse("Escaping \u0085 failed. Found in XML output.", xml.contains("\u0085"));
        assertTrue("Escaping \u0085 failed. Entity not found in XML output.", xml.contains("&#x85;"));
        // test normal unicode existing
        assertTrue("Escaping € failed. Not found in XML output.", xml.contains("€"));
        assertTrue("Escaping ç failed. Not found in XML output.", xml.contains("ç"));
        assertTrue("Escaping ã failed. Not found in XML output.", xml.contains("ã"));
        assertTrue("Escaping á failed. Not found in XML output.", xml.contains("á"));
        // test XML Entities converted
        assertTrue("Escaping \" failed. Not found in XML output.", xml.contains("&quot;"));
        assertTrue("Escaping ' failed. Not found in XML output.", xml.contains("&apos;"));
        assertTrue("Escaping & failed. Not found in XML output.", xml.contains("&amp;"));
        assertTrue("Escaping < failed. Not found in XML output.", xml.contains("&lt;"));
        assertTrue("Escaping > failed. Not found in XML output.", xml.contains("&gt;"));
    }

    /**
     * Valid XML with comments to JSONObject
     */
    @Test
    public void shouldHandleCommentsInXML() {

        String xmlStr = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
                "<!-- this is a comment -->\n"+
                "<addresses>\n"+
                "   <address>\n"+
                "       <![CDATA[ this is -- <another> comment ]]>\n"+
                "       <name>Joe Tester</name>\n"+
                "       <!-- this is a - multi line \n"+
                "            comment -->\n"+
                "       <street>Baker street 5</street>\n"+
                "   </address>\n"+
                "</addresses>";
        JSONObject jsonObject = XML.toJSONObject(xmlStr);
        String expectedStr = "{\"addresses\":{\"address\":{\"street\":\"Baker "+
                "street 5\",\"name\":\"Joe Tester\",\"content\":\" this is -- "+
                "<another> comment \"}}}";
        JSONObject expectedJsonObject = new JSONObject(expectedStr);
        Util.compareActualVsExpectedJsonObjects(jsonObject,expectedJsonObject);
    }

    /**
     * Valid XML to XML.toString()
     */
    @Test
    public void shouldHandleToString() {
        String xmlStr = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
            "<addresses xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""+
            "   xsi:noNamespaceSchemaLocation='test.xsd'>\n"+
            "   <address>\n"+
            "       <name>[CDATA[Joe &amp; T &gt; e &lt; s &quot; t &apos; er]]</name>\n"+
            "       <street>Baker street 5</street>\n"+
            "       <ArrayOfNum>1, 2, 3, 4.1, 5.2</ArrayOfNum>\n"+
            "   </address>\n"+
            "</addresses>";

        String expectedStr = 
                "{\"addresses\":{\"address\":{\"street\":\"Baker street 5\","+
                "\"name\":\"[CDATA[Joe & T > e < s \\\" t \\\' er]]\","+
                "\"ArrayOfNum\":\"1, 2, 3, 4.1, 5.2\"\n"+
                "},\"xsi:noNamespaceSchemaLocation\":"+
                "\"test.xsd\",\"xmlns:xsi\":\"http://www.w3.org/2001/"+
                "XMLSchema-instance\"}}";
        
        JSONObject jsonObject = XML.toJSONObject(xmlStr);
        String xmlToStr = XML.toString(jsonObject);
        JSONObject finalJsonObject = XML.toJSONObject(xmlToStr);
        JSONObject expectedJsonObject = new JSONObject(expectedStr);
        Util.compareActualVsExpectedJsonObjects(jsonObject,expectedJsonObject);
        Util.compareActualVsExpectedJsonObjects(finalJsonObject,expectedJsonObject);
    }

    /**
     * Converting a JSON doc containing '>' content to JSONObject, then
     * XML.toString() should result in valid XML.
     */
    @Test
    public void shouldHandleContentNoArraytoString() {
        String expectedStr = "{\"addresses\":{\"content\":\">\"}}";
        JSONObject expectedJsonObject = new JSONObject(expectedStr);
        String finalStr = XML.toString(expectedJsonObject);
        String expectedFinalStr = "<addresses>&gt;</addresses>";
        assertEquals("Should handle expectedFinal: ["+expectedStr+"] final: ["+
                finalStr+"]", expectedFinalStr, finalStr);
    }

    /**
     * Converting a JSON doc containing a 'content' array to JSONObject, then
     * XML.toString() should result in valid XML.
     * TODO: This is probably an error in how the 'content' keyword is used.
     */
    @Test
    public void shouldHandleContentArraytoString() {
        String expectedStr = 
            "{\"addresses\":{" +
            "\"content\":[1, 2, 3]}}";
        JSONObject expectedJsonObject = new JSONObject(expectedStr);
        String finalStr = XML.toString(expectedJsonObject);
        String expectedFinalStr = "<addresses>"+
                "1\n2\n3</addresses>";
        assertEquals("Should handle expectedFinal: ["+expectedStr+"] final: ["+
                finalStr+"]", expectedFinalStr, finalStr);
    }

    /**
     * Converting a JSON doc containing a named array to JSONObject, then
     * XML.toString() should result in valid XML.
     */
    @Test
    public void shouldHandleArraytoString() {
        String expectedStr = 
            "{\"addresses\":{"+
            "\"something\":[1, 2, 3]}}";
        JSONObject expectedJsonObject = new JSONObject(expectedStr);
        String finalStr = XML.toString(expectedJsonObject);
        String expectedFinalStr = "<addresses>"+
                "<something>1</something><something>2</something><something>3</something>"+
                "</addresses>";
        assertEquals("Should handle expectedFinal: ["+expectedStr+"] final: ["+
                finalStr+"]", expectedFinalStr, finalStr);
    }
    
    /**
     * Tests that the XML output for empty arrays is consistent.
     */
    @Test
    public void shouldHandleEmptyArray(){
        final JSONObject jo1 = new JSONObject();
        jo1.put("array",new Object[]{});
        final JSONObject jo2 = new JSONObject();
        jo2.put("array",new JSONArray());

        final String expected = "<jo></jo>";
        String output1 = XML.toString(jo1,"jo");
        assertEquals("Expected an empty root tag", expected, output1);
        String output2 = XML.toString(jo2,"jo");
        assertEquals("Expected an empty root tag", expected, output2);
    }
    
    /**
     * Tests that the XML output for arrays is consistent when an internal array is empty.
     */
    @Test
    public void shouldHandleEmptyMultiArray(){
        final JSONObject jo1 = new JSONObject();
        jo1.put("arr",new Object[]{"One", new String[]{}, "Four"});
        final JSONObject jo2 = new JSONObject();
        jo2.put("arr",new JSONArray(new Object[]{"One", new JSONArray(new String[]{}), "Four"}));

        final String expected = "<jo><arr>One</arr><arr></arr><arr>Four</arr></jo>";
        String output1 = XML.toString(jo1,"jo");
        assertEquals("Expected a matching array", expected, output1);
        String output2 = XML.toString(jo2,"jo");
        assertEquals("Expected a matching array", expected, output2);
    }
   
    /**
     * Tests that the XML output for arrays is consistent when arrays are not empty.
     */
    @Test
    public void shouldHandleNonEmptyArray(){
        final JSONObject jo1 = new JSONObject();
        jo1.put("arr",new String[]{"One", "Two", "Three"});
        final JSONObject jo2 = new JSONObject();
        jo2.put("arr",new JSONArray(new String[]{"One", "Two", "Three"}));

        final String expected = "<jo><arr>One</arr><arr>Two</arr><arr>Three</arr></jo>";
        String output1 = XML.toString(jo1,"jo");
        assertEquals("Expected a non empty root tag", expected, output1);
        String output2 = XML.toString(jo2,"jo");
        assertEquals("Expected a non empty root tag", expected, output2);
    }

    /**
     * Tests that the XML output for arrays is consistent when arrays are not empty and contain internal arrays.
     */
    @Test
    public void shouldHandleMultiArray(){
        final JSONObject jo1 = new JSONObject();
        jo1.put("arr",new Object[]{"One", new String[]{"Two", "Three"}, "Four"});
        final JSONObject jo2 = new JSONObject();
        jo2.put("arr",new JSONArray(new Object[]{"One", new JSONArray(new String[]{"Two", "Three"}), "Four"}));

        final String expected = "<jo><arr>One</arr><arr><array>Two</array><array>Three</array></arr><arr>Four</arr></jo>";
        String output1 = XML.toString(jo1,"jo");
        assertEquals("Expected a matching array", expected, output1);
        String output2 = XML.toString(jo2,"jo");
        assertEquals("Expected a matching array", expected, output2);
    }

    /**
     * Converting a JSON doc containing a named array of nested arrays to
     * JSONObject, then XML.toString() should result in valid XML.
     */
    @Test
    public void shouldHandleNestedArraytoString() {
        String xmlStr = 
            "{\"addresses\":{\"address\":{\"name\":\"\",\"nocontent\":\"\","+
            "\"outer\":[[1], [2], [3]]},\"xsi:noNamespaceSchemaLocation\":\"test.xsd\",\""+
            "xmlns:xsi\":\"http://www.w3.org/2001/XMLSchema-instance\"}}";
        JSONObject jsonObject = new JSONObject(xmlStr);
        String finalStr = XML.toString(jsonObject);
        JSONObject finalJsonObject = XML.toJSONObject(finalStr);
        String expectedStr = "<addresses><address><name/><nocontent/>"+
                "<outer><array>1</array></outer><outer><array>2</array>"+
                "</outer><outer><array>3</array></outer>"+
                "</address><xsi:noNamespaceSchemaLocation>test.xsd</xsi:noName"+
                "spaceSchemaLocation><xmlns:xsi>http://www.w3.org/2001/XMLSche"+
                "ma-instance</xmlns:xsi></addresses>";
        JSONObject expectedJsonObject = XML.toJSONObject(expectedStr);
        Util.compareActualVsExpectedJsonObjects(finalJsonObject,expectedJsonObject);
    }


    /**
     * Possible bug: 
     * Illegal node-names must be converted to legal XML-node-names.
     * The given example shows 2 nodes which are valid for JSON, but not for XML.
     * Therefore illegal arguments should be converted to e.g. an underscore (_).
     */
    @Test
    public void shouldHandleIllegalJSONNodeNames()
    {
        JSONObject inputJSON = new JSONObject();
        inputJSON.append("123IllegalNode", "someValue1");
        inputJSON.append("Illegal@node", "someValue2");

        String result = XML.toString(inputJSON);

        /*
         * This is invalid XML. Names should not begin with digits or contain
         * certain values, including '@'. One possible solution is to replace
         * illegal chars with '_', in which case the expected output would be:
         * <___IllegalNode>someValue1</___IllegalNode><Illegal_node>someValue2</Illegal_node>
         */
        String expected = "<123IllegalNode>someValue1</123IllegalNode><Illegal@node>someValue2</Illegal@node>";

        assertEquals("length",expected.length(), result.length());
        assertTrue("123IllegalNode",result.contains("<123IllegalNode>someValue1</123IllegalNode>"));
        assertTrue("Illegal@node",result.contains("<Illegal@node>someValue2</Illegal@node>"));
    }

    /**
     * JSONObject with NULL value, to XML.toString()
     */
    @Test
    public void shouldHandleNullNodeValue()
    {
        JSONObject inputJSON = new JSONObject();
        inputJSON.put("nullValue", JSONObject.NULL);
        // This is a possible preferred result
        // String expectedXML = "<nullValue/>";
        /**
         * This is the current behavior. JSONObject.NULL is emitted as 
         * the string, "null".
         */
        String actualXML = "<nullValue>null</nullValue>";
        String resultXML = XML.toString(inputJSON);
        assertEquals(actualXML, resultXML);
    }

    /**
     * Investigate exactly how the "content" keyword works
     */
    @Test
    public void contentOperations() {
        /*
         * When a standalone <!CDATA[...]] structure is found while parsing XML into a
         * JSONObject, the contents are placed in a string value with key="content".
         */
        String xmlStr = "<tag1></tag1><![CDATA[if (a < b && a > 0) then return]]><tag2></tag2>";
        JSONObject jsonObject = XML.toJSONObject(xmlStr);
        assertTrue("1. 3 items", 3 == jsonObject.length());
        assertTrue("1. empty tag1", "".equals(jsonObject.get("tag1")));
        assertTrue("1. empty tag2", "".equals(jsonObject.get("tag2")));
        assertTrue("1. content found", "if (a < b && a > 0) then return".equals(jsonObject.get("content")));

        // multiple consecutive standalone cdatas are accumulated into an array
        xmlStr = "<tag1></tag1><![CDATA[if (a < b && a > 0) then return]]><tag2></tag2><![CDATA[here is another cdata]]>";
        jsonObject = XML.toJSONObject(xmlStr);
        assertTrue("2. 3 items", 3 == jsonObject.length());
        assertTrue("2. empty tag1", "".equals(jsonObject.get("tag1")));
        assertTrue("2. empty tag2", "".equals(jsonObject.get("tag2")));
        assertTrue("2. content array found", jsonObject.get("content") instanceof JSONArray);
        JSONArray jsonArray = jsonObject.getJSONArray("content");
        assertTrue("2. array size", jsonArray.length() == 2);
        assertTrue("2. content array entry 0", "if (a < b && a > 0) then return".equals(jsonArray.get(0)));
        assertTrue("2. content array entry 1", "here is another cdata".equals(jsonArray.get(1)));

        /*
         * text content is accumulated in a "content" inside a local JSONObject.
         * If there is only one instance, it is saved in the context (a different JSONObject 
         * from the calling code. and the content element is discarded. 
         */
        xmlStr =  "<tag1>value 1</tag1>";
        jsonObject = XML.toJSONObject(xmlStr);
        assertTrue("3. 2 items", 1 == jsonObject.length());
        assertTrue("3. value tag1", "value 1".equals(jsonObject.get("tag1")));

        /*
         * array-style text content (multiple tags with the same name) is 
         * accumulated in a local JSONObject with key="content" and value=JSONArray,
         * saved in the context, and then the local JSONObject is discarded.
         */
        xmlStr =  "<tag1>value 1</tag1><tag1>2</tag1><tag1>true</tag1>";
        jsonObject = XML.toJSONObject(xmlStr);
        assertTrue("4. 1 item", 1 == jsonObject.length());
        assertTrue("4. content array found", jsonObject.get("tag1") instanceof JSONArray);
        jsonArray = jsonObject.getJSONArray("tag1");
        assertTrue("4. array size", jsonArray.length() == 3);
        assertTrue("4. content array entry 0", "value 1".equals(jsonArray.get(0)));
        assertTrue("4. content array entry 1", jsonArray.getInt(1) == 2);
        assertTrue("4. content array entry 2", jsonArray.getBoolean(2) == true);

        /*
         * Complex content is accumulated in a "content" field. For example, an element
         * may contain a mix of child elements and text. Each text segment is 
         * accumulated to content. 
         */
        xmlStr =  "<tag1>val1<tag2/>val2</tag1>";
        jsonObject = XML.toJSONObject(xmlStr);
        assertTrue("5. 1 item", 1 == jsonObject.length());
        assertTrue("5. jsonObject found", jsonObject.get("tag1") instanceof JSONObject);
        jsonObject = jsonObject.getJSONObject("tag1");
        assertTrue("5. 2 contained items", 2 == jsonObject.length());
        assertTrue("5. contained tag", "".equals(jsonObject.get("tag2")));
        assertTrue("5. contained content jsonArray found", jsonObject.get("content") instanceof JSONArray);
        jsonArray = jsonObject.getJSONArray("content");
        assertTrue("5. array size", jsonArray.length() == 2);
        assertTrue("5. content array entry 0", "val1".equals(jsonArray.get(0)));
        assertTrue("5. content array entry 1", "val2".equals(jsonArray.get(1)));

        /*
         * If there is only 1 complex text content, then it is accumulated in a 
         * "content" field as a string.
         */
        xmlStr =  "<tag1>val1<tag2/></tag1>";
        jsonObject = XML.toJSONObject(xmlStr);
        assertTrue("6. 1 item", 1 == jsonObject.length());
        assertTrue("6. jsonObject found", jsonObject.get("tag1") instanceof JSONObject);
        jsonObject = jsonObject.getJSONObject("tag1");
        assertTrue("6. contained content found", "val1".equals(jsonObject.get("content")));
        assertTrue("6. contained tag2", "".equals(jsonObject.get("tag2")));

        /*
         * In this corner case, the content sibling happens to have key=content
         * We end up with an array within an array, and no content element.
         * This is probably a bug. 
         */
        xmlStr =  "<tag1>val1<content/></tag1>";
        jsonObject = XML.toJSONObject(xmlStr);
        assertTrue("7. 1 item", 1 == jsonObject.length());
        assertTrue("7. jsonArray found", jsonObject.get("tag1") instanceof JSONArray);
        jsonArray = jsonObject.getJSONArray("tag1");
        assertTrue("array size 1", jsonArray.length() == 1);
        assertTrue("7. contained array found", jsonArray.get(0) instanceof JSONArray);
        jsonArray = jsonArray.getJSONArray(0);
        assertTrue("7. inner array size 2", jsonArray.length() == 2);
        assertTrue("7. inner array item 0", "val1".equals(jsonArray.get(0)));
        assertTrue("7. inner array item 1", "".equals(jsonArray.get(1)));

        /*
         * Confirm behavior of original issue
         */
        String jsonStr = 
                "{"+
                    "\"Profile\": {"+
                        "\"list\": {"+
                            "\"history\": {"+
                                "\"entries\": ["+
                                    "{"+
                                        "\"deviceId\": \"id\","+
                                        "\"content\": {"+
                                            "\"material\": ["+
                                                "{"+
                                                    "\"stuff\": false"+
                                                "}"+
                                            "]"+
                                        "}"+
                                    "}"+
                                "]"+
                            "}"+
                        "}"+
                    "}"+
                "}";
        jsonObject = new JSONObject(jsonStr);
        xmlStr = XML.toString(jsonObject);
        /*
         * This is the created XML. Looks like content was mistaken for
         * complex (child node + text) XML. 
         *  <Profile>
         *      <list>
         *          <history>
         *              <entries>
         *                  <deviceId>id</deviceId>
         *                  {&quot;material&quot;:[{&quot;stuff&quot;:false}]}
         *              </entries>
         *          </history>
         *      </list>
         *  </Profile>
         */
        assertTrue("nothing to test here, see comment on created XML, above", true);
    }

    /**
     * Convenience method, given an input string and expected result,
     * convert to JSONObject and compare actual to expected result.
     * @param xmlStr the string to parse
     * @param expectedStr the expected JSON string
     */
    private void compareStringToJSONObject(String xmlStr, String expectedStr) {
        JSONObject jsonObject = XML.toJSONObject(xmlStr);
        JSONObject expectedJsonObject = new JSONObject(expectedStr);
        Util.compareActualVsExpectedJsonObjects(jsonObject,expectedJsonObject);
    }

    /**
     * Convenience method, given an input string and expected result,
     * convert to JSONObject via reader and compare actual to expected result.
     * @param xmlStr the string to parse
     * @param expectedStr the expected JSON string
     */
    private void compareReaderToJSONObject(String xmlStr, String expectedStr) {
        JSONObject expectedJsonObject = new JSONObject(expectedStr);
        Reader reader = new StringReader(xmlStr);
        JSONObject jsonObject = XML.toJSONObject(reader);
        Util.compareActualVsExpectedJsonObjects(jsonObject,expectedJsonObject);
    }

    /**
     * Convenience method, given an input string and expected result, convert to
     * JSONObject via file and compare actual to expected result.
     * 
     * @param xmlStr
     *            the string to parse
     * @param expectedStr
     *            the expected JSON string
     * @throws IOException
     */
    private void compareFileToJSONObject(String xmlStr, String expectedStr) {
        try {
            JSONObject expectedJsonObject = new JSONObject(expectedStr);
            File tempFile = this.testFolder.newFile("fileToJSONObject.xml");
            FileWriter fileWriter = new FileWriter(tempFile);
            try {
                fileWriter.write(xmlStr);
            } finally {
                fileWriter.close();
            }

            Reader reader = new FileReader(tempFile);
            try {
                JSONObject jsonObject = XML.toJSONObject(reader);
                Util.compareActualVsExpectedJsonObjects(jsonObject,expectedJsonObject);
            } finally {
                reader.close();
            }
        } catch (IOException e) {
            fail("Error: " +e.getMessage());
        }
    }

    /**
     * JSON string lost leading zero and converted "True" to true.
     */
    @Test
    public void testToJSONArray_jsonOutput() {
        final String originalXml = "<root><id>01</id><id>1</id><id>00</id><id>0</id><item id=\"01\"/><title>True</title></root>";
        final JSONObject expectedJson = new JSONObject("{\"root\":{\"item\":{\"id\":\"01\"},\"id\":[\"01\",1,\"00\",0],\"title\":true}}");
        final JSONObject actualJsonOutput = XML.toJSONObject(originalXml, false);

        Util.compareActualVsExpectedJsonObjects(actualJsonOutput,expectedJson);
    }

    /**
     * JSON string cannot be reverted to original xml.
     */
    @Test
    public void testToJSONArray_reversibility() {
        final String originalXml = "<root><id>01</id><id>1</id><id>00</id><id>0</id><item id=\"01\"/><title>True</title></root>";
        final String revertedXml = XML.toString(XML.toJSONObject(originalXml, false));

        assertNotEquals(revertedXml, originalXml);
    }

    /**
     * test passes when using the new method toJsonArray.
     */
    @Test
    public void testToJsonXML() {
        final String originalXml = "<root><id>01</id><id>1</id><id>00</id><id>0</id><item id=\"01\"/><title>True</title></root>";
        final JSONObject expected = new JSONObject("{\"root\":{\"item\":{\"id\":\"01\"},\"id\":[\"01\",\"1\",\"00\",\"0\"],\"title\":\"True\"}}");

        final JSONObject actual = XML.toJSONObject(originalXml,true);
        
        Util.compareActualVsExpectedJsonObjects(actual, expected);
        
        final String reverseXml = XML.toString(actual);
        // this reversal isn't exactly the same. use JSONML for an exact reversal
        // the order of the elements may be differnet as well.
        final String expectedReverseXml = "<root><item><id>01</id></item><id>01</id><id>1</id><id>00</id><id>0</id><title>True</title></root>";

        assertEquals("length",expectedReverseXml.length(), reverseXml.length());
        assertTrue("array contents", reverseXml.contains("<id>01</id><id>1</id><id>00</id><id>0</id>"));
        assertTrue("item contents", reverseXml.contains("<item><id>01</id></item>"));
        assertTrue("title contents", reverseXml.contains("<title>True</title>"));
    }
    
    /**
     * test to validate certain conditions of XML unescaping.
     */
    @Test
    public void testUnescape() {
        assertEquals("{\"xml\":\"Can cope <;\"}",
                XML.toJSONObject("<xml>Can cope &lt;; </xml>").toString());
        assertEquals("Can cope <; ", XML.unescape("Can cope &lt;; "));

        assertEquals("{\"xml\":\"Can cope & ;\"}",
                XML.toJSONObject("<xml>Can cope &amp; ; </xml>").toString());
        assertEquals("Can cope & ; ", XML.unescape("Can cope &amp; ; "));

        assertEquals("{\"xml\":\"Can cope &;\"}",
                XML.toJSONObject("<xml>Can cope &amp;; </xml>").toString());
        assertEquals("Can cope &; ", XML.unescape("Can cope &amp;; "));

        // unicode entity
        assertEquals("{\"xml\":\"Can cope 4;\"}",
                XML.toJSONObject("<xml>Can cope &#x34;; </xml>").toString());
        assertEquals("Can cope 4; ", XML.unescape("Can cope &#x34;; "));

        // double escaped
        assertEquals("{\"xml\":\"Can cope &lt;\"}",
                XML.toJSONObject("<xml>Can cope &amp;lt; </xml>").toString());
        assertEquals("Can cope &lt; ", XML.unescape("Can cope &amp;lt; "));
        
        assertEquals("{\"xml\":\"Can cope &#x34;\"}",
                XML.toJSONObject("<xml>Can cope &amp;#x34; </xml>").toString());
        assertEquals("Can cope &#x34; ", XML.unescape("Can cope &amp;#x34; "));

   }

    /**
     * test passes when xsi:nil="true" converting to null (JSON specification-like nil conversion enabled)
     */
    @Test
    public void testToJsonWithNullWhenNilConversionEnabled() {
        final String originalXml = "<root><id xsi:nil=\"true\"/></root>";
        final String expectedJsonString = "{\"root\":{\"id\":null}}";

        final JSONObject json = XML.toJSONObject(originalXml,
                new XMLParserConfiguration()
                    .withKeepStrings(false)
                    .withcDataTagName("content")
                    .withConvertNilAttributeToNull(true));
        assertEquals(expectedJsonString, json.toString());
    }

    /**
     * test passes when xsi:nil="true" not converting to null (JSON specification-like nil conversion disabled)
     */
    @Test
    public void testToJsonWithNullWhenNilConversionDisabled() {
        final String originalXml = "<root><id xsi:nil=\"true\"/></root>";
        final String expectedJsonString = "{\"root\":{\"id\":{\"xsi:nil\":true}}}";

        final JSONObject json = XML.toJSONObject(originalXml, new XMLParserConfiguration());
        assertEquals(expectedJsonString, json.toString());
    }

    /**
     * Tests to verify that supported escapes in XML are converted to actual values.
     */
    @Test
    public void testIssue537CaseSensitiveHexEscapeMinimal(){
        String xmlStr = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
            "<root>Neutrophils.Hypersegmented &#X7C; Bld-Ser-Plas</root>";
        String expectedStr = 
            "{\"root\":\"Neutrophils.Hypersegmented | Bld-Ser-Plas\"}";
        JSONObject xmlJSONObj = XML.toJSONObject(xmlStr, true);
        JSONObject expected = new JSONObject(expectedStr);
        Util.compareActualVsExpectedJsonObjects(xmlJSONObj, expected);
    }

    /**
     * Tests to verify that supported escapes in XML are converted to actual values.
     */
    @Test
    public void testIssue537CaseSensitiveHexEscapeFullFile(){
        try {
            InputStream xmlStream = null;
            try {
                xmlStream = XMLTest.class.getClassLoader().getResourceAsStream("Issue537.xml");
                Reader xmlReader = new InputStreamReader(xmlStream);
                JSONObject actual = XML.toJSONObject(xmlReader, true);
                InputStream jsonStream = null;
                try {
                    jsonStream = XMLTest.class.getClassLoader().getResourceAsStream("Issue537.json");
                    final JSONObject expected = new JSONObject(new JSONTokener(jsonStream));
                    Util.compareActualVsExpectedJsonObjects(actual,expected);
                } finally {
                    if (jsonStream != null) {
                        jsonStream.close();
                    }
                }
            } finally {
                if (xmlStream != null) {
                    xmlStream.close();
                }
            }
        } catch (IOException e) {
            fail("file writer error: " +e.getMessage());
        }
    }

    /**
     * Tests to verify that supported escapes in XML are converted to actual values.
     */
    @Test
    public void testIssue537CaseSensitiveHexUnEscapeDirect(){
        String origStr = 
            "Neutrophils.Hypersegmented &#X7C; Bld-Ser-Plas";
        String expectedStr = 
            "Neutrophils.Hypersegmented | Bld-Ser-Plas";
        String actualStr = XML.unescape(origStr);
        
        assertEquals("Case insensitive Entity unescape",  expectedStr, actualStr);
    }

    /**
     * test passes when xsi:type="java.lang.String" not converting to string
     */
    @Test
    public void testToJsonWithTypeWhenTypeConversionDisabled() {
        String originalXml = "<root><id xsi:type=\"string\">1234</id></root>";
        String expectedJsonString = "{\"root\":{\"id\":{\"xsi:type\":\"string\",\"content\":1234}}}";
        JSONObject expectedJson = new JSONObject(expectedJsonString);
        JSONObject actualJson = XML.toJSONObject(originalXml, new XMLParserConfiguration());
        Util.compareActualVsExpectedJsonObjects(actualJson,expectedJson);
    }

    /**
     * test passes when xsi:type="java.lang.String" converting to String
     */
    @Test
    public void testToJsonWithTypeWhenTypeConversionEnabled() {
        String originalXml = "<root><id1 xsi:type=\"string\">1234</id1>"
                + "<id2 xsi:type=\"integer\">1234</id2></root>";
        String expectedJsonString = "{\"root\":{\"id2\":1234,\"id1\":\"1234\"}}";
        JSONObject expectedJson = new JSONObject(expectedJsonString);
        Map<String, XMLXsiTypeConverter<?>> xsiTypeMap = new HashMap<String, XMLXsiTypeConverter<?>>();
        xsiTypeMap.put("string", new XMLXsiTypeConverter<String>() {
            @Override public String convert(final String value) {
                return value;
            }
        });
        xsiTypeMap.put("integer", new XMLXsiTypeConverter<Integer>() {
            @Override public Integer convert(final String value) {
                return Integer.valueOf(value);
            }
        });
        JSONObject actualJson = XML.toJSONObject(originalXml, new XMLParserConfiguration().withXsiTypeMap(xsiTypeMap));
        Util.compareActualVsExpectedJsonObjects(actualJson,expectedJson);
    }

    @Test
    public void testToJsonWithXSITypeWhenTypeConversionEnabled() {
        String originalXml = "<root><asString xsi:type=\"string\">12345</asString><asInt "
                + "xsi:type=\"integer\">54321</asInt></root>";
        String expectedJsonString = "{\"root\":{\"asString\":\"12345\",\"asInt\":54321}}";
        JSONObject expectedJson = new JSONObject(expectedJsonString);
        Map<String, XMLXsiTypeConverter<?>> xsiTypeMap = new HashMap<String, XMLXsiTypeConverter<?>>();
        xsiTypeMap.put("string", new XMLXsiTypeConverter<String>() {
            @Override public String convert(final String value) {
                return value;
            }
        });
        xsiTypeMap.put("integer", new XMLXsiTypeConverter<Integer>() {
            @Override public Integer convert(final String value) {
                return Integer.valueOf(value);
            }
        });
        JSONObject actualJson = XML.toJSONObject(originalXml, new XMLParserConfiguration().withXsiTypeMap(xsiTypeMap));
        Util.compareActualVsExpectedJsonObjects(actualJson,expectedJson);
    }

    @Test
    public void testToJsonWithXSITypeWhenTypeConversionNotEnabledOnOne() {
        String originalXml = "<root><asString xsi:type=\"string\">12345</asString><asInt>54321</asInt></root>";
        String expectedJsonString = "{\"root\":{\"asString\":\"12345\",\"asInt\":54321}}";
        JSONObject expectedJson = new JSONObject(expectedJsonString);
        Map<String, XMLXsiTypeConverter<?>> xsiTypeMap = new HashMap<String, XMLXsiTypeConverter<?>>();
        xsiTypeMap.put("string", new XMLXsiTypeConverter<String>() {
            @Override public String convert(final String value) {
                return value;
            }
        });
        JSONObject actualJson = XML.toJSONObject(originalXml, new XMLParserConfiguration().withXsiTypeMap(xsiTypeMap));
        Util.compareActualVsExpectedJsonObjects(actualJson,expectedJson);
    }

    @Test
    public void testXSITypeMapNotModifiable() {
        Map<String, XMLXsiTypeConverter<?>> xsiTypeMap = new HashMap<String, XMLXsiTypeConverter<?>>();
        XMLParserConfiguration config = new XMLParserConfiguration().withXsiTypeMap(xsiTypeMap);
        xsiTypeMap.put("string", new XMLXsiTypeConverter<String>() {
            @Override public String convert(final String value) {
                return value;
            }
        });
        assertEquals("Config Conversion Map size is expected to be 0", 0, config.getXsiTypeMap().size());

        try {
            config.getXsiTypeMap().put("boolean", new XMLXsiTypeConverter<Boolean>() {
                @Override public Boolean convert(final String value) {
                    return Boolean.valueOf(value);
                }
            });
            fail("Expected to be unable to modify the config");
        } catch (Exception ignored) { }
    }
}
