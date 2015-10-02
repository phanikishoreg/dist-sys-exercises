# Lookup Protocol

To find a Converison server that could support a UNIT1->UNIT2 conversion, client/proxy server must send a message in the following format:
```
LOOKUP <UNIT1 UNIT2>
```
Where,
* LOOKUP is the Command to search a Conversion server
* UNIT1 and UNIT2 are the Conversion type specification used in HW2

**Note:** All the parameters are SPACE delimited and the whole request message should end with a NEWLINE.

##### Response
Following response will be sent by Discovery server if it was able to find requested conversion:
```
IP_ADDR PORT_NO
```
* IP_ADDR is the IP Address of the Conversion server which could be connected from any remote client.
* PORT_NO is the port number of the Conversion server that clients need to connect for requested conversion.

In case of failure to service the LOOKUP request, following responses could be sent:
* NONE: If the requested conversion-type cannot be discovered. 
* FAILURE: For any other error. 

**Note:** All the response messages will end with a NEWLINE.
