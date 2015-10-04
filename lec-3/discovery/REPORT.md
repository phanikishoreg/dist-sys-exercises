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
    * Host the Discovery server. It always runs on port 5555.
    * And then host Proxy and conversion servers with Discovery server information. 

For more details, please refer to README.md

#### To Test

* Once the above environment is setup, connect to Discovery and see if the required conversion is supported. (Use telnet, if you'd like to do it.)
**OR**
* Know the Proxy server's Address and port number, connect to proxy server. Proxy server displays a list of conversions it supports, in the welcome message. 
* Select one of the conversions, provide <inconv> <outconv> <inpvalue> from telnet to Proxy.
* Proxy internally does multi-step conversion using LOOKUP feature of Discovery server. 

* If you observe the logs of Proxy, Discovery and Conversion servers, you'll see the distributed behavior.

*in to m
Proxy server ->
```
Found: 10.0.0.98 9999
Step 1/2 using 10.0.0.98:9999 => 34 in = 86.36 cm
Key : 10.0.0.98 and Value: 5555
Registering to it..

Found: Ashwinis-MacBook-Pro.local 5557
Step 2/2 using Ashwinis-MacBook-Pro.local:5557 => 86.36 cm = 0.8636 m
Response: 34 in = 0.8636 m
```
Client side ->
```
Welcome to Java-based Proxy server..Conversions supported:{ft->lbs=ft->in,in->b,b->lbs, in->lbs=in->b,b->lbs, m->lbs=m->cm,cm->in,in->b,b->lbs, b->ft=b->in,in->ft, b->m=b->in,in->cm,cm->m, lbs->m=lbs->b,b->in,in->cm,cm->m, lbs->in=lbs->b,b->in, cm->ft=cm->in,in->ft, lbs->cm=lbs->b,b->in,in->cm, lbs->ft=lbs->b,b->in,in->ft, m->ft=m->cm,cm->in,in->ft, m->in=m->cm,cm->in, ft->m=ft->in,in->cm,cm->m, ft->cm=ft->in,in->cm, cm->b=cm->in,in->b, ft->b=ft->in,in->b, b->cm=b->in,in->cm, m->b=m->cm,cm->in,in->b, cm->lbs=cm->in,in->b,b->lbs, in->m=in->cm,cm->m}
in m 34
0.8636
```
###Conversion server 1 ->
```
Started server on port 9999

Accepted connection from client - /10.0.0.98
34.0 in = 86.36 cm

Conversion server 2->
Accepted connection from client ('10.0.0.98', 64927)
('Received message: ', 'cm m 86.36\n')
In func
86.36 cm = 0.8636 m
```

###C Conversion Server
Working Example
Discovery Server (python):
Accepted connection from client ('10.0.0.3', 40445)

Request: LOOKUP lbs b
lookup...
lbs b

Response: 10.0.0.3 9000
Accepted connection from client ('10.0.0.3', 40445)
Request: LOOKUP lbs b
lookup...
lbs b
Response: 10.0.0.3 9000

Accepted connection from client ('10.0.0.3', 40447)
Request: LOOKUP b in
lookup...
b in
Response: 10.0.0.3 11111

Proxy Server (java):
Accepted connection from client: /10.0.0.3


Request: 76 lbs = ? in
Key : 10.0.0.3 and Value: 5555
Registering to it..

C Conversion Server connected(lbs<->b)
Found: 10.0.0.3 9000
Step 1/2 using 10.0.0.3:9000 => 76 lbs = 228.000000 b
Key : 10.0.0.3 and Value: 5555
Registering to it..

PythonConvServ2 connected(b<->in)
Found: 10.0.0.3 11111
Step 2/2 using 10.0.0.3:11111 => 228.000000 b = 1140.0 in
Response: 76 lbs = 1140.0 in


done.

C Conversion Server (lbs<->b):
./convserver.o 10.0.0.3 5555 10.0.0.3 9000
Started server on port 9000
ADD b lbs 10.0.0.3 9000
 
Accepted connection from client[10.0.0.3]
76.000000 lbs = 228.000000 b

Client(lbs<->in):
telnet 10.0.0.3 7777
Trying 10.0.0.3...
Connected to 10.0.0.3.
Escape character is '^]'.
Welcome to Java-based Proxy server..Conversions supported:{ft->lbs=ft->in,in->b,b->lbs, in->lbs=in->b,b->lbs, m->lbs=m->cm,cm->in,in->b,b->lbs, b->ft=b->in,in->ft, b->m=b->in,in->cm,cm->m, lbs->m=lbs->b,b->in,in->cm,cm->m, lbs->in=lbs->b,b->in, cm->ft=cm->in,in->ft, lbs->cm=lbs->b,b->in,in->cm, lbs->ft=lbs->b,b->in,in->ft, m->ft=m->cm,cm->in,in->ft, m->in=m->cm,cm->in, ft->m=ft->in,in->cm,cm->m, ft->cm=ft->in,in->cm, cm->b=cm->in,in->b, ft->b=ft->in,in->b, b->cm=b->in,in->cm, m->b=m->cm,cm->in,in->b, cm->lbs=cm->in,in->b,b->lbs, in->m=in->cm,cm->m}
lbs in 76
1140.0
Connection closed by foreign host.


###Java Conversion Server:

Working Example:

Discovery Output:
Accepted connection from client ('10.0.0.3', 40449)
Request: ADD in cm 10.0.0.3 8000
add...
in cm 10.0.0.3 8000
{'lbs-m': ['10.0.0.3:7777'], 'b-ft': ['10.0.0.3:7777'], 'ft-lbs': ['10.0.0.3:7777'], 'b-m': ['10.0.0.3:7777'], 'in-lbs': ['10.0.0.3:7777'], 'b-in': ['10.0.0.3:11111'], 'ft-m': ['10.0.0.3:7777'], 'cm-in': ['10.0.0.3:8000'], 'cm-lbs': ['10.0.0.3:7777'], 'b-lbs': ['10.0.0.3:9000'], 'b-cm': ['10.0.0.3:7777'], 'cm-ft': ['10.0.0.3:7777'], 'in-m': ['10.0.0.3:7777']}
Response: SUCCESS
Note: The other IP addresses and port numbers will be displayed as the Proxy itself has registered, and is running on port 7777.
Accepted connection from client ('10.0.0.3', 40451)
Request: LOOKUP lbs b
lookup...
lbs b
Response: 10.0.0.3 9000

Accepted connection from client ('10.0.0.3', 40453)
Request: LOOKUP b in
lookup...
b in
Response: 10.0.0.3 11111

Accepted connection from client ('10.0.0.3', 40455)
Request: LOOKUP in cm
lookup...
in cm
Response: 10.0.0.3 8000

Proxy Server (java):
Accepted connection from client: /10.0.0.3


Request: 89 lbs = ? cm
Key : 10.0.0.3 and Value: 5555
Registering to it..

C Conversion Server connected(lbs<->b)
Found: 10.0.0.3 9000
Step 1/3 using 10.0.0.3:9000 => 89 lbs = 267.000000 b
Key : 10.0.0.3 and Value: 5555
Registering to it..

PythonConvServ2 connected(b<->in)
Found: 10.0.0.3 11111
Step 2/3 using 10.0.0.3:11111 => 267.000000 b = 1335.0 in
Key : 10.0.0.3 and Value: 5555
Registering to it..

Java Conversion Server connected(in<->cm)
Found: 10.0.0.3 8000
Step 3/3 using 10.0.0.3:8000 => 1335.0 in = 3390.9 cm
Response: 89 lbs = 3390.9 cm


done.

Java Conversion Server(in<->cm):
java ConvServer 10.0.0.3 5555 10.0.0.3 8000
Started server on port 8000

Accepted connection from client - /10.0.0.3
1335.0 in = 3390.9 cm

Client(lbs<->cm):
telnet 10.0.0.3 7777
Trying 10.0.0.3...
Connected to 10.0.0.3.
Escape character is '^]'.
Welcome to Java-based Proxy server..Conversions supported:{ft->lbs=ft->in,in->b,b->lbs, in->lbs=in->b,b->lbs, m->lbs=m->cm,cm->in,in->b,b->lbs, b->ft=b->in,in->ft, b->m=b->in,in->cm,cm->m, lbs->m=lbs->b,b->in,in->cm,cm->m, lbs->in=lbs->b,b->in, cm->ft=cm->in,in->ft, lbs->cm=lbs->b,b->in,in->cm, lbs->ft=lbs->b,b->in,in->ft, m->ft=m->cm,cm->in,in->ft, m->in=m->cm,cm->in, ft->m=ft->in,in->cm,cm->m, ft->cm=ft->in,in->cm, cm->b=cm->in,in->b, ft->b=ft->in,in->b, b->cm=b->in,in->cm, m->b=m->cm,cm->in,in->b, cm->lbs=cm->in,in->b,b->lbs, in->m=in->cm,cm->m}
lbs cm 89
3390.9
Connection closed by foreign host.

##WORKING OF PYTHON COVERSION SERVERS
#### Python Conversion Server 1 - 

Our conversion server is an extended version of HW2 conversion server. It takes the IP address of the discovery server and the Conversion Server along with the Port number of both the servers. The Conversion server then registers its IP and port number to the discovery server. This conversion server converts from b to in or in to b and returns the client with the required output.
To compile and run:
```
Usage:
python <programName.py> <IP:Conversion Server> <Port: Conversion server> <IP:Discovery Server> <Port: Discovery server>

where,
programName.py – It is the name of the conversion server written in python.
IP: Conversion Server – The IP of the conversion is given as an input parameter.  
Port: Conversion Server – It is the port number of the Conversion server on which the conversion server will run.
IP: Discovery Server – It is the IP address of discovery server
Port: Discovery server – It is port number of discovery server.


Run:
 python PythonConvServ2.py 10.0.0.3 5555 10.0.0.98 4444

Output:
registered
Response:  SUCCESS

Started server on  4444

#### Python Conversion Server 2 - 

Our conversion server is an extended version of HW2 conversion server. It takes the IP address of the discovery server and the Conversion Server along with the Port number of both the servers. The Conversion server then registers its IP and port number to the discovery server. This conversion server converts from cm to m or m to cm and returns the client with the required output.
To compile and run:
```
Usage:
python <programName.py> <IP:Conversion Server> <Port: Conversion server> <IP:Discovery Server> <Port: Discovery server>

where,
programName.py – It is the name of the conversion server written in python.
IP: Conversion Server – The IP of the conversion is given as an input parameter.  
Port: Conversion Server – It is the port number of the Conversion server on which the conversion server will run.
IP: Discovery Server – It is the IP address of discovery server
Port: Discovery server – It is port number of discovery server.


Run:
 python PythonConvServ2.py 10.0.0.3 5555 10.0.0.98 4445

Output:
registered
Response:  SUCCESS

Started server on  4445

