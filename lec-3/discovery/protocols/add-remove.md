# Add and Remove Protocol

### Addition Protocol

To add a Converison server to the Discovery, conversion server must send a message in the following format when it's ready:
```
ADD <UNIT1 UNIT2 IP_ADDR PORT_NO> [UNI/BI]
```
Where
* ADD is the Command to register a Conversion server
* UNIT1 and UNIT2 are the Conversion type specification used in HW2
* IP_ADDR is the IP Address of the Conversion server which could be connected from any remote client.
* PORT_NO is the port number of the Conversion server that clients need to connect to for UNIT1<->UNIT2 conversion.

* An optional [UNI/BI] parameter which can be used especially in case of a Server having only Uni-directional support.
  By default, it is assumed that the Conversion server supports Bi-directional, so it is optional to Bi-directional servers.

**Note:** All the parameters are SPACE delimited and the whole request message should end with a NEWLINE.

##### Response

If the Discovery server could register the server, a "OK" response is sent back to the Conversion server (in this case client).

And if the Discovery server failed for some reason to register, following failed messages could be returned.
* ALREADY_EXISTS - If the conversion server is already in the Discovery table.
* UNI_EXISTS - If the conversion server requested for BI directional and there is already a Uni-directional registration.
* BI_EXISTS - If the conversion server requested for UNI directional and there is already a Bi-directional registration.
* INVALID_HOST - If the Discovery server could determine that it is invalid IP or host name at the time of registration. - Reserved
* INVALID_PORT - If the Discovery server could not parse the Port no as an Integer (unsigned of course). - Reserved
* UNKNOWN_ERROR - If there was some failure in processing the Request.

**Note:** All the response messages will end with a NEWLINE.

### Removal Protocol

To Remove a Converison server from the Discovery, conversion server must send a message in the following format when it wants to stop being discovered:
```
REMOVE <IP_ADDR PORT_NO>
```
Where
* REMOVE is the Command to deregister a Conversion server
* IP_ADDR is the IP Address of the Conversion server this request corresponds to.
* PORT_NO is the port number of the Conversion server.

**Note:** All the parameters are SPACE delimited and the whole request message should end with a NEWLINE.

##### Response

If the Discovery server could deregister the server, a "OK" response is sent back to the Conversion server (in this case client).

And if the Discovery server failed for some reason to deregister, following failed messages could be returned.
* NOT_FOUND - If the conversion server could not be found in the Discovery table.
* UNKNOWN_ERROR - If there was some failure in processing the Request.

**Note:** All the response messages will end with a NEWLINE.

### Pros & Cons - TODO

### Figures - TODO

*If you are feeling ambitious, have someone make a figure with a tool like this: http://bramp.github.io/js-sequence-diagrams/*
