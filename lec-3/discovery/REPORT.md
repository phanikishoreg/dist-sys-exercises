## Report

## Students
* Ashwini (Programmer, Tester)
* Changle (Programmer, Tester)
* Harpreet (Protocol designer, Programmer)
* Mruganka (Programmer, Tester)
* Phani (Protocol designer, Programmer)
* Teo (Programmer, Tester)

## Capabilities

1. Discovery server supports the ADD/REMOVE/LOOKUP protocols.
2. Proxy server registers to any number of discovery servers and can convert from any to any unit. (discovery server list and conversion steps for any to any must be predefined.)
3. Conversion servers only register to 1 discovery server.

## Protocol Specification

### ADD

```
ADD <unit1> <unit2> <host> <port>
```
Adds conversion/proxy server in the above request to the Discovery table. It is assumed that the conversion is bidirectional, and for any client requesting for unit1<->unit2 or unit2<->unit1, this server could be used.

If it was successfully registered, response would be:
```
SUCCESS
```

If it failed to register because it's a duplicate request:
```
FAILURE EXISTS
```

If it failed to register for any other reason:
```
FAILURE
```

**Note:** <unit1> and <unit2> are sorted before adding them to the discovery table to ensure that the key which is a combination of <unit1>"-"<unit2> is always unique and there will not be a key with <unit2>"-"<unit1> in the table. 

### REMOVE

```
REMOVE <host> <port>
```
Removes the conversion/proxy server in the above request from the discovery table. REMOVE removes all the ADDed entries, so if a proxy server registered for multiple conversions, all of them will be removed from the table. 

If it was successful, response would be:
```
SUCCESS
```

If it failed to unregister because there was no entry:
```
FAILURE NOTFOUND
```

If it failed for any other reason:
```
FAILURE
```

### LOOKUP

```
LOOKUP <unit1> <unit2>
```
Searches the discovery table to find if any conversion/proxy server supports this connversion. And, if this conversion is indeed supported by multiple conversion/proxy servers, only the first entry in the list is returned to the client.

If it was successful in LOOKUP, response includes the conversion/proxy server details:
```
<IP/HOSTNAME> <PORTNO>
```
Client can then use this information to connect to the above Server to get their job done!

If it failed because there was no server supporting this conversion type, response would be:
```
NONE
```

And if it failed for any other reason:
```
FAILURE
```

## Test Plan

#### To Compile

* Discovery server as such is a Python program, so there is separate step to compile.
* Proxy server and some conversion servers are C/Java programs, to know the details for compiling them, please refer to README.md.

#### To Run

* Each server takes different set of command line arguments. 
* Sequence to run:
    * Host the Discovery server.
    * And then host Proxy and conversion servers with Discovery server information. 

For more details, please refer to README.md

#### To Test

* Once the above environment is setup, connect to Discovery and see if the required conversion is supported. (Use telnet, if you'd like to do it.)
**OR**
* Know the Proxy server's Address and port number, connect to proxy server. Proxy server displays a list of conversions it supports, in the welcome message. 
* Select one of the conversions, provide <inconv> <outconv> <inpvalue> from telnet to Proxy.
* Proxy internally does multi-step conversion using LOOKUP feature of Discovery server. 

* If you observe the logs of Proxy, Discovery and Conversion servers, you'll see the distributed behavior.

## Changle, Ashwini: Can you add the exact steps you followed to show the working? Commands used to run each! Steps and sample of one multi-step conversion!