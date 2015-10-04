## Discovery Server (HW4)

###### How to run:

Discovery server is implemented in Python. It runs on port number 5555. To run this server, 
```
$ python discovery.py <portno>

where,
<portno>: is the port number that discovery will listen on, for requests.
```

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
* When the server terminates gracefully (which will never happen!), it sends REMOVE request to each server.
* Whenever a conversion is requested by the client, Proxy server breaks the conversion into multi-steps based on it's predefined table, and sends out LOOKUP request to Discovery server in its list. It sends out request to each Discovery server until it receives a valid "<hostname/IP> <portno>". If it receives a "None" response or "FAILURE" response, it continues to LOOKUP with remaining Discovery servers.

#### Conversion servers

## @Harpreet, @Mruganka: Describe working and things for each server here..

#### Conversion Servers - python

Our conversion server is an extended version of HW2 conversion server.
To compile and run:
```
$ python <programName.py> <Port: Conversion server> <IP:Discovery Server> <Port: Discovery server>
```
where,
Port: Conversion server - is the port number of conversion server
IP:Discovery Server - is the ip address of discovery server
IP: Conv Server - is ip address of conversion server
Port: Discovery server - is port number of discovery server
ex:
to register to 1 discovery server:
$ python PythonConvServ2.py 5757
```
registered
```
SUCCESS
```
Started server on  5757
```

To cover:
* how to compile (if java or c programs)
* how to run, what command line inputs.
* how it works

