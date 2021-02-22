# SWE 262P Project Milestone 4

Changes made within JSONObject.java starting on line 99 and ending on line 186.

Unit Tests for added functionality located in JSONObjectTest.java starting on line 97 and ending on line 246 (4 total).

Clarifying comments located throughout the code. 

## Milestone 4 Functions:

 public Stream<JSONObject> toStream()

* Builds and returns a Stream<JSONObject>, with each part of the stream representing a leaf node.
* A leaf node is a JSONObject representing a collection of "innermost" non-JSON-valued key-value pairs.
* For example, for a JSONObject containing 5 keys, 3 of them being nested JSON, one part of the stream will be a JSONObject containing the two non-JSON keys and their values, and another part of the stream will be the eventual non-JSON keys and values within the nested JSONs.
* Uses buildJsonStream() to build the Stream<JSONObject>.


public void buildJsonStream(Object json, String containingKey)

* Builds the Stream<JSONObject> into the jsonStreamBuilder private class variable (a Stream.Builder<JSONObject> object).
* Works recursively, allowing for extraction of nested leaf nodes.


---

# SWE 262P Project Milestone 3

Changes made within XML.java starting on line 894 and ending on line 957.

Unit Tests for added functionality located in XMLTest.java starting on line 273 and ending on line 344 (3 total).

Clarifying comments located throughout the code. 

Project language changed from 1.7->1.8 (sourceCompatibility in build.gradle) in order to allow for use of Functionals necessary for Milestone 3.

## Milestone 3 Function:

static JSONObject toJSONObject(Reader reader, Function<String, String> keyTransformer)

* Reads an XML file into a JSON Object, transforming each key using the passed in keyTransformer, a Functional that takes in a String and returns a modified version
* Uses my own implementation of parse() that applies the keyTransformer functional (function that takes a String and returns a String) to every key as the XML is parsed
* If a null keyTransformer is passed in, simply return the original object
* A malformed xml throws a JSONException


Performance Implications: Writing this function within the library code itself allows for meaningful performance increases as it allows for keys to be transformed during parsing of each XML file, instead of requiring a JSON Object to first be created from the XML file and then going back through the object and constructing replacement objects with new names (as in Milestone 1's implementation). This not only saves CPU cycles, but reduces memory usage as well. 


---
# SWE 262P Project Milestone 2

Changes made within XML.java starting on line 665 and ending on line 891.

Unit Tests located in XMLTest.java starting on line 60 and ending on line 269 (10 total, 5 per function).

Clarifying comments located throughout the code.

## Milestone 2 Function for Part 1: 


static JSONObject toJSONObject(Reader reader, JSONPointer path)

* Reads an XML file into a JSON object, and extracts some smaller sub-object inside, given a certain path (using JSONPointer)
* Stores sub object into JSON object containing the object itself and its key
* Uses my own implementation of parse() in order to stop traversing file once path found
* If the path is found, stops running once the path's innermost keys are closed
* Returns a JSONObject holding the contents of the given key path, with a key representing the key path's innermost key
* Returns an empty JSONObject if the keypath is not found or XML malformed
* Returns a JSONObject containing the entire xml contents if the path is empty


## Milestone 2 Function for Part 2:


static JSONObject toJSONObject(Reader reader, JSONPointer path, JSONObject replacement)

* Reads an XML file into a JSON object, replaces a sub-object on a certain key path with the replacement JSON object passed in, then stores the result in a JSONObject
* Uses my own parse() implementation to find the desired path (similar to above), and read in the replacement object where the specified key path would be allowing for performance gains
* If an empty path is passed in, simply return the replacement object
* If the path does not exist, return a JSONObject representing the unchanged base file
* A malformed xml throws a JSONException
