## Discovery Server (HW4)

###### How to run:

Discovery server is implemented in Python. To run this server, 
$ python discovery.py <portno>

where,
<portno>: is the port number that discovery will listen on, for requests.


Once you have the discovery up and running, you need to then start Conversion servers and Proxy servers with the IP/Hostname and Port Number of this Discovery server.

** ADD/REMOVE/LOOKUP protocols are described in the protocols/ folder. This discovery does exactly that, not more, not less!**

#### Proxy Server

Our proxy server is an extended version of HW2 Proxy server.
To compile and run:
```
$ javac ProxyServer.java
```

```
$ java ProxyServer i:<my ip/hostname>:<my portno> d:<discovery1 ip/hostname>:<discovery1 portno>[,...]

where,
i:<ip>:<port> - are address and portnumber of this proxyserver. address should be an external ip address

d:<ip>:<port>,[...] - address and portnumber of discovery servers. This can be used to register to multiple discovery servers.

ex:
to register to 1 discovery server:
$ java ProxyServer i:localhost:9999 d:localhost:7777

to register to 2 discovery servers:
$ java ProxyServer i:localhost:9999 d:localhost:7777,localhost:6666
```

**Note:**
* Proxy server also supports reading discovery servers and conversion table from file. 
* Conversion server list is removed from the config file, instead Discovery server list is brought in.
* Tags: 
```
  start tag: "=DISCOVERY_SERVER_LIST" 
  server:	<ip or hostname>:<portno>
  end tag: "=END_LIST"
```
ex:
```
=DISCOVERY_SERVER_LIST
localhost:5555
=END_LIST
=CONVERSION_TABLE
ft->cm:ft->in,in->cm
cm->ft:cm->in,in->ft
m->in:m->cm,cm->in
in->m:in->cm,cm->m
=END_TABLE
```
* Each discovery server entry will contain IP address or hostname and port number with colon as a delimiter.
* Limitation: Only using maps to store discovery's ip,portno. So, if you have 2 discovery servers running on machine, only the last one is picked for discovery.
* At the start of the server, ADD requests are sent to all the discovery servers specified. 
* When the server terminates gracefully (which will never happen!), it sends p REMOVE request to each server.
* Whenever a conversion is requested by the client, Proxy server breaks the conversion into multi-steps based on it's predefined table, and sends out LOOKUP request to Discovery server in its list. It sends out request to each Discovery server until it receives a valid "<hostname/IP> <portno>". If it receives a "None" response or "FAILURE" response, it continues to LOOKUP with remaining Discovery servers.

#### Conversion servers

#### Python Conversion Server 1 - 

Our conversion server is an extended version of HW2 conversion server. It takes the IP address of the discovery server and the Conversion Server along with the Port number of both the servers. The Conversion server then registers its IP and port number to the discovery server. This conversion server converts from b to in or in to b and returns the client with the required output.
To compile and run:

Usage:
python <programName.py> <IP:Conversion Server> <Port: Conversion server> <IP:Discovery Server> <Port: Discovery server>

where,
programName.py - It is the name of the conversion server written in python.
IP: Conversion Server - The IP of the conversion is given as an input parameter.  
Port: Conversion Server - It is the port number of the Conversion server on which the conversion server will run.
IP: Discovery Server - It is the IP address of discovery server
Port: Discovery server - It is port number of discovery server.


Run:
```
python PythonConvServ2.py 10.0.0.3 5555 10.0.0.98 4444
```
Output:
```
registered
Response: SUCCESS

Started server on  4444
```
#### Python Conversion Server 2 - 

Our conversion server is an extended version of HW2 conversion server. It takes the IP address of the discovery server and the Conversion Server along with the Port number of both the servers. The Conversion server then registers its IP and port number to the discovery server. This conversion server converts from cm to m or m to cm and returns the client with the required output.
To compile and run:

Usage:
```
python <programName.py> <IP:Conversion Server> <Port: Conversion server> <IP:Discovery Server> <Port: Discovery server>
```
where,
programName.py - It is the name of the conversion server written in python.
IP: Conversion Server - The IP of the conversion is given as an input parameter.  
Port: Conversion Server - It is the port number of the Conversion server on which the conversion server will run.
IP: Discovery Server - It is the IP address of discovery server
Port: Discovery server - It is port number of discovery server.


Run:
```
python PythonConvServ2.py 10.0.0.3 5555 10.0.0.98 4445
```
Output:
```
registered
Response: SUCCESS

Started server on  4445
```

#### C Conversion Server

Compiling
gcc -o convserver.o convserver.c

Run
./convserver.o DiscoveryIP DiscoveryPort SelfServerIP SelfServerPort


####Java Conversion Server
Compiling
javac ConvServer.java

Run
java ConvServer DiscoveryIP DiscoveryPort SelfServerIP SelfServerPort


