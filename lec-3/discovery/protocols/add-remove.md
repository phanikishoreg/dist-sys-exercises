# Add and Remove Protocol

### Addition Protocol

To add a Converison server to the Discovery, conversion server must send a message in the following format when it's ready:
```
ADD <UNIT1 UNIT2 IP_ADDR PORT_NO>
```
Where
* ADD is the Command to register a Conversion server
* UNIT1 and UNIT2 are the Conversion type specification used in HW2
* IP_ADDR is the IP Address of the Conversion server which could be connected from any remote client.
* PORT_NO is the port number of the Conversion server that clients need to connect to for UNIT1<->UNIT2 conversion.

**Note:** All the parameters are SPACE delimited and the whole request message should end with a NEWLINE.

##### Response

If the Discovery server could register the server, a "SUCCESS" response is sent back to the Conversion server (in this case client).

And if the Discovery server failed for some reason to register, following failed messages could be returned.
* FAILURE EXISTS - If the conversion server is already in the Discovery table.
* FAILURE - For any other error.. 

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

If the Discovery server could unregister the server, a "SUCCESS" response is sent back to the Conversion server (in this case client).

And if the Discovery server failed for some reason to unregister, following failed messages could be returned.
* FAILURE NOTFOUND - If the conversion server could not be found in the Discovery table.
* FAILURE - For any other error.

**Note:** All the response messages will end with a NEWLINE.

