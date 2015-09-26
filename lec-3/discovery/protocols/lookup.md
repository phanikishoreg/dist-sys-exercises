# Lookup Protocol

To find a Converison server that could support a UNIT1->UNIT2 conversion, client/proxy server must send a message in the following format:
```
LOOKUP <UNIT1 UNIT2> [UNI/BI]
```
Where,
* LOOKUP is the Command to search a Conversion server
* UNIT1 and UNIT2 are the Conversion type specification used in HW2
* An optional [UNI/BI] parameter which can be used especially if in case client is looking for Bi-directional support.
  By default, it is assumed that the client requests for Uni-directional conversion, so it is optional for Uni-directional lookups.

**Note:** All the parameters are SPACE delimited and the whole request message should end with a NEWLINE.

##### Response
Following response will be sent by Discovery server if it was able to find requested conversion:
```
IP_ADDR PORT_NO
```
* IP_ADDR is the IP Address of the Conversion server which could be connected from any remote client.
* PORT_NO is the port number of the Conversion server that clients need to connect for requested conversion.

In case of failure to service the LOOKUP request, following responses could be sent:
* "null" - BAD DESIGN?? 
* NOT_FOUND: If the requested conversion-type cannot be discovered. 
* UNKNOWN_ERROR: If any unknown error in processing the request. 

**Note:** All the response messages will end with a NEWLINE.

### Pros & Cons - TODO

### Figures - TODO

*If you are feeling ambitious, have someone make a figure with a tool like this: http://bramp.github.io/js-sequence-diagrams/*
