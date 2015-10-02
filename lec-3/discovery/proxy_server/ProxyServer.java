/******************************************************************************
 *
 *  CS 6421 - Proxy Server (uses Conversion server list and conversion table
 *		to do as many conversions.)
 *  Compilation:  javac ProxyServer.java
 *  Execution:    java ProxyServer <port> [config file]
 *		  % java ConvServer portno convdata.conf
 * 
 *  Students: Phani, Changle
 ******************************************************************************/

// import required packages..
import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.HashMap;

// server class for conversion server,host tuple
class Server {
	String host; //conversion server hostname/ip
	int portno; //conversion server port number

	Server(String pHost, int pPortNo) {
		host = pHost;
		portno = pPortNo;
	}

	String getHost() {
		return host;
	}

	int getPort() {
		return portno;
	}

	//override toString for use in System.out.print
	public String toString() {
		return (host + ":" + portno);
	}
}

public class ProxyServer {

	// Conversion table: Supported conversion types, conversion table (step by step conversions)
	static Map<String, String> convTable;

	//This is required for registration with the Discovery server.
	static String myExtHost;
	static String myPortNo;

	//Discovery server list.. Register/unregister with each of them..
	static Map<String, Integer> discTable;

	// Reads conversion data from a file..
	static int initConversionsFile(String confFile) {
		/* Sample:
		   =DISCOVERY_SERVER_LIST
		   localhost:9999
		   localhost:9999
		   localhost:9998
		   localhost:9998
		   =END_LIST
		   =CONVERSION_TABLE
		   ft->in:ft->in 
		   in->ft:in->ft
		   in->cm:in->cm
		   cm->in:cm->in
		   ft->cm:ft->in,in->cm 
		   cm->ft:cm->in,in->ft
		   ft->m:ft->in,in->cm,cm->m
		   m->ft:m->cm,cm->in,in->ft
		   =END_TABLE
		 */
		convTable = new HashMap<String, String>();
		discTable = new HashMap<String, Integer>();
		try 
		{
			//read from input file..
			BufferedReader br = new BufferedReader(new FileReader(confFile));

			boolean startServList = false;
			boolean endServList = false;
			boolean startConvTable = false;
			boolean endConvTable = false;
			String sCurrentLine = "";
			int countServers = 0;
			int countConversions = 0;
			int noOfLines = 0;

			//for each line in file
			while ((sCurrentLine = br.readLine()) != null) {
				//System.out.println("line no: " + (++ noOfLines));
				if(sCurrentLine.isEmpty() || sCurrentLine.charAt(0) == '#') {
					//some servers just die without notifying.. i cannot keep editing table each time..
					//for a empty line or a line with "#".. to support comments.
					continue;
				}
				//start of conversion servers found
				if(sCurrentLine.equals("=DISCOVERY_SERVER_LIST") && (startServList != true)) {
					startServList = true;
					continue;
				}
				//end of conversion servers..
				else if(sCurrentLine.equals("=END_LIST") && (startServList == true && endServList != true)) {
					endServList = true;
					continue;
				}
				//start of conversion table.. remember: conversion table only allowed after discovery servers end..
				else if(sCurrentLine.equals("=CONVERSION_TABLE") && (startServList == true && endServList == true && startConvTable == false)) {
					startConvTable = true;
					continue;
				}
				//end of conversion table..
				else if(sCurrentLine.equals("=END_TABLE") && startConvTable == true) {
					endConvTable = true;
					continue;
				}
				//after discovery server start tag..each line contains discovery server information..
				else if(startServList = true && endServList == false) {
					//read disc serv list
					//ex: localhost:9999
					String[] host = sCurrentLine.split(":");
					if(host.length != 2) {
						System.out.println("parse:ignore line - " + sCurrentLine);
						//ignore host..
						continue;
					}
					//host 0-> host name/ip
					//host 1-> port number
					int portno = 0;
					try {
						portno = Integer.parseInt(host[1]);
						//handling invalid port number.. catch string -> int conversion error and ignore line..
					} catch(NumberFormatException nfe) {
						System.out.println("parse: port no invalid " + host[1] + " e: " + nfe.getMessage());
						//ignore host and continue..
						continue;
					}
					//add to Map.
					discTable.put(host[0], portno);

					countServers ++;
				} else if (startConvTable == true && endConvTable == false) {
					//after conversion servers end and conversion table starts..
					//read conv table ex: cm->ft:cm->in,in->ft
					String[] conv = sCurrentLine.split(":");
					if(conv.length != 2) {
						System.out.println("parse:ignore line - " + sCurrentLine);
						//ignore conversion..
						continue;
					}
					//conv 0 -> conv type
					//conv 1 -> conv table to follow for conv 0.
					convTable.put(conv[0], conv[1]);
					countConversions ++;
				} else {
					System.out.println("parse:ignore line - " + sCurrentLine);
					//ignore line..
					continue;
				}
				//System.out.println(sCurrentLine);
			}
			//if no conversion servers or no conversion table.. exit.. what job do we have to do!!
			if(countConversions == 0 || countServers == 0) {
				System.out.println("Failed to create table!!\n");
				return -1;
			}

		} catch (IOException ioe) {
			ioe.printStackTrace();
			return -1;
		} catch (Exception e) {
			e.printStackTrace();
			return -1; //any exception.. we may not have created the table or something.. 
		}
		System.out.println(discTable);
		System.out.println(convTable);

		return 0;
	}

	// Initialize conversion server list and conversion table.
	// TBD: 
	// 	1. Read conversion table and conversion server list from a config file
	//	2. Making undirected conversion table. (Makes splitConvert() do more computation..)
	static void initConversionsStatic(String discoveryList) {
		//convServers = new HashMap<String, Server>();
		convTable = new HashMap<String, String>();
		discTable = new HashMap<String, Integer>();

		String [] discs = discoveryList.split(",");
		if(discs.length == 0) {
			System.out.println("No discovery servers registered..exiting");
			System.exit(1);
		}
		System.out.println("list size: " + discs.length);
		for(String s:discs) {
			String[] host = s.split(":");
			if(host.length != 2) {
				System.out.println("Failed to parse discovery servers..");
				System.exit(1);
			}
			int port = 0;
			try {
				port = Integer.parseInt(host[1]);

			} catch (NumberFormatException nfe) {
				System.out.println("NFE: " + host[1] + " msg: " + nfe.getMessage());
				//ignore this discovery!!
			}
			System.out.println("here");
			discTable.put(host[0], port);

		}

		// Add list of conversion servers here. 
		// Avoid duplicates (same conversion by different servers).. Not supported yet!! 

		//avoid conflicting with conversion server, so removing single level entries..

		// Support two level conversions using conversion server list.
		convTable.put("ft->cm", "ft->in,in->cm");
		convTable.put("cm->ft", "cm->in,in->ft");
		convTable.put("m->in", "m->cm,cm->in");
		convTable.put("in->m", "in->cm,cm->m");
		convTable.put("b->cm", "b->in,in->cm");
		convTable.put("cm->b", "cm->in,in->b");
		convTable.put("b->ft", "b->in,in->ft");
		convTable.put("ft->b", "ft->in,in->b");
		convTable.put("lbs->in", "lbs->b,b->in");
		convTable.put("in->lbs", "in->b,b->lbs");

		// Support three or more level conversions using conversion server list.
		// TBD: Enhance using conversion table with lower level conversions..
		// Ex: ft->m, use ft->cm 2 level conversion from same conversion table convTable and 
		// 		and cm->m 1 level conversion from same conversion table convTable.
		convTable.put("ft->m", "ft->in,in->cm,cm->m");
		convTable.put("m->ft", "m->cm,cm->in,in->ft");
		convTable.put("b->m", "b->in,in->cm,cm->m");
		convTable.put("m->b", "m->cm,cm->in,in->b");
		convTable.put("lbs->ft", "lbs->b,b->in,in->ft");
		convTable.put("ft->lbs", "ft->in,in->b,b->lbs");
		convTable.put("lbs->cm", "lbs->b,b->in,in->cm");
		convTable.put("cm->lbs", "cm->in,in->b,b->lbs");

		// 4 levels..
		convTable.put("lbs->m", "lbs->b,b->in,in->cm,cm->m");
		convTable.put("m->lbs", "m->cm,cm->in,in->b,b->lbs");

		//System.out.println(convServers);
		System.out.println(discTable);
		System.out.println(convTable);
	}

	public static int addToDiscovery() {
		for(Map.Entry<String, Integer> entry : discTable.entrySet()) {
			System.out.printf("Key : %s and Value: %d\n", entry.getKey(), entry.getValue());
			System.out.println("Registering to it..\n");

			Server sv = new Server(entry.getKey(), entry.getValue());
			//connect using socket..
			//send ADD CONV1 CONV2 HOST PORT for each entry in convTable..
			for(Map.Entry<String, String> conv : convTable.entrySet()) {
				String[] conversion = conv.getKey().split("->");
				if(conversion.length != 2) {
					System.out.println("Ignore" + conv.getKey());
				}
				String request = "ADD " + conversion[0] + " " + conversion[1] + " " + myExtHost + " " + myPortNo;
				//Guess Discovery will send a FAILURE for 2nd request if I did register! :P 
				String response = processDiscovery(sv, request);
			}
		}
		return 0;
	}

	public static int removeFromDiscovery() {
		for(Map.Entry<String, Integer> entry : discTable.entrySet()) {
			System.out.printf("Key : %s and Value: %d\n", entry.getKey(), entry.getValue());
			System.out.println("Registering to it..\n");

			Server sv = new Server(entry.getKey(), entry.getValue());
			String request = "REMOVE " + myExtHost + " " + myPortNo;
			//Guess Discovery will send a FAILURE for 2nd request if I did register! :P 
			String response = processDiscovery(sv, request);
		}

		return 0;
	}

	public static Server lookupFromDiscovery(String from, String to) {
		for(Map.Entry<String, Integer> entry : discTable.entrySet()) {
			System.out.printf("Key : %s and Value: %d\n", entry.getKey(), entry.getValue());
			System.out.println("Registering to it..\n");

			Server sv = new Server(entry.getKey(), entry.getValue());
			String request = "LOOKUP " + from + " " + to;
			//Guess Discovery will send a FAILURE for 2nd request if I did register! :P 
			String response = processDiscovery(sv, request);
			if(response.isEmpty() || response.equalsIgnoreCase("none")) {
				continue;
			}
			String[] host = response.split(" ");
			if(host.length != 2) {
				continue;
			}
			int port = 0;
			try {
				port = Integer.parseInt(host[1]);
			} catch (NumberFormatException nfe) {
				System.out.println("NFE: " + host[1] + " msg: " + nfe.getMessage());
				//ignore this discovery..
				continue;
			}

			System.out.println("Found: " + host[0] + " " + port);
			return (new Server(host[0], port));
		}
 
		return null;
	}

	/* 
	 * Splits the conversion based on conversion table and carries out multi-level conversion 
	 * from - from conversion (initial)
	 * to - to conversion (final)
	 * inp - input value
	 *
	 * return - end result of multi-level conversion.
	 */
	public static String splitConvert(String from, String to, String inp) {

		String convString = inp;

		//splits the conversion into multiple conversions
		String convTemp = (String)convTable.get(from + "->" + to);
		//if you don't support that type, return..
		if(convTemp.isEmpty()) {
			System.out.println("Not supported: " + from + " -> " + to);
			return "";
		}

		String[] conversions = convTemp.split(",");
		int i = 1;
		//process each conversion and input to next level
		for(String s:conversions){
			//get conversion server information..
			//Server convSrv = (Server)convServers.get(s);
			String[] convParam = s.split("->");
			if(convParam.length != 2) {
				System.out.println("Failed to convert");
				return "";
			}
			Server convSrv = lookupFromDiscovery(convParam[0], convParam[1]);
			//if no conversion server found..return..
			if(convSrv == null) {
				System.out.println("Not supported: " + convParam[0] + "->" + convParam[1]);
				return "";
			}
			System.out.print("Step " + i + "/" + conversions.length + " ");
			System.out.print("using " + convSrv.getHost() + ":" + convSrv.getPort());

			String response = convString;
			//create request : <from> <to> <inputvalue>
			String request = convParam[0] + " " + convParam[1] + " " + convString;
			//get response: <outputvalue>
			try {
				//process this conversion..
				response = processConversion(convSrv, request);
			} catch(IOException ioe) {
				System.out.println("Caught IOException: " + ioe.getMessage());
				return "";
			}
			//if any server failed and returned no response..or empty response..
			if(response.isEmpty()) {
				System.out.println("Failed to convert " + convString + " from " + convParam[0] + " to " + convParam[1]);
				return response;
			}
			System.out.println(" => " + convString + " " + convParam[0] + " = " + response + " " +  convParam[1]);
			//feed value for next level..
			convString = response;
			i ++;
		}

		//in the end, return to the client..		
		return convString; 
	}

	public static String processDiscovery(Server convServ, String request) {

		String response = "";
		try {
			//connect to server..
			Socket serverSocket = new Socket(convServ.getHost(), convServ.getPort());
			// open up IO streams
			BufferedReader in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
			PrintWriter out = new PrintWriter(serverSocket.getOutputStream(), true);

			out.println(request); //write request to server socket..

			/* read and print the conversion response */
			// readLine() blocks until the server receives a new line from client
			if ((response = in.readLine()) == null) {
				System.out.println("Error reading message");
				out.close();
				in.close();
				serverSocket.close();

				return ""; //exit from this client..
			}

			out.close();
			in.close();
			serverSocket.close();
		} catch(Exception e) {
			System.out.println("Caugth exception: " + e.getMessage());
			return "";
		}
		return response;
	}


	public static String processConversion(Server convServ, String request) throws IOException {

		//connect to server..
		Socket serverSocket = new Socket(convServ.getHost(), convServ.getPort());
		// open up IO streams
		BufferedReader in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
		PrintWriter out = new PrintWriter(serverSocket.getOutputStream(), true);

		/* Read a welcome message from the conversion server */
		// thats the protocol.. 
		String welcomeMsg = in.readLine();

		out.println(request); //write request to server socket..

		/* read and print the conversion response */
		// readLine() blocks until the server receives a new line from client
		String response;
		if ((response = in.readLine()) == null) {
			System.out.println("Error reading message");
			out.close();
			in.close();
			serverSocket.close();

			return ""; //exit from this client..
		}
		//check if the response was a Float value..
		try {
			float f = Float.parseFloat(response);
		} catch (NumberFormatException nfe) {
			System.out.println("Caught NFE on response: " + response + " msg: " + nfe.getMessage());
			return "";
		}

		out.close();
		in.close();
		serverSocket.close();
		return response;
	}


	public static void processClient (Socket clientSocket) throws IOException {
		// open up IO streams
		BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

		/* Write a welcome message to the client */
		//out.println("Welcome! (Commands=> quit: to exit, list: to list servers, table: to list conversion table)");
		out.println("Welcome to Java-based Proxy server..Conversions supported:" + convTable);

		/* read and print the client's request */
		// readLine() blocks until the server receives a new line from client
		String userInput;
		if ((userInput = in.readLine()) == null) {
			System.out.println("Error reading message");
			out.close();
			in.close();
			clientSocket.close();

			return; //exit from this client..
		}
		//removed support for commands.. 

		String[] tokens = userInput.split(" ");
		String fromConv = "", toConv = "";
		String inputValue = "";
		int noOfTokens = 0;
		for(String s:tokens) {
			if(noOfTokens == 0) { //input conv token..
				fromConv = s;
				noOfTokens ++;
			} else if(noOfTokens == 1) { //output conv token..
				toConv = s;
				noOfTokens ++;
			} else if(noOfTokens == 2) { //input value token..
				inputValue = s;
				noOfTokens ++;
			} else { //invalid number of tokens..
				System.out.println("Invalid input here: " + userInput);
				out.close();
				in.close();
				clientSocket.close();

				return; //exit from this client..
			}
		}		
		//too few tokens..
		if(noOfTokens != 3) {
			System.out.println("Invalid input here: " + userInput);
			out.close();
			in.close();
			clientSocket.close();

			return; //exit from this client..
		}

		System.out.println("\n\nRequest: " + inputValue + " " + fromConv + " = ? " + toConv);

		//convert any unit to any other unit..
		String outputValue = splitConvert(fromConv, toConv, inputValue);
		if(outputValue.isEmpty()) {
			//don't know what to return in failure.. so just closing socket..
			//out.println("conversion failed");
			System.out.println("Conversion failed/not supported..");
			out.close();
			in.close();
			clientSocket.close();

			return; //exit from this client..
		}

		System.out.println("Response: " + inputValue + " " + fromConv + " = " + outputValue + " " + toConv + "\n\n");
		out.println(outputValue); //write result to socket..

		// close IO streams, then socket
		System.out.println("done.\n");
		out.close();
		in.close();
		clientSocket.close();
	}

	public static void usage() {
		System.err.println("Usage: java ProxyServer <port> [config file]");
		System.err.println("	<port> - Proxy server port");
		System.err.println("	[config file] - config file containing conversion server and conversion table info (optional)");
		System.err.println("");
		System.err.println("ex1: java ProxyServer 7777");
		System.err.println("ex2: java ProxyServer 7777 convserv.cfg");
	}

	public static void main(String[] args) throws Exception {

		String filename = "", whoami = "", discoveryListArg = "";
		for(String s:args) {
			if(s.length() <= 2 || s.charAt(1) != ':') {
				System.out.println("Invalid arguments..");
				usage();
				System.exit(1);
			}
			switch(s.charAt(0)) {
				case 'f':
					filename = s.substring(2);
					System.out.println("Filename: " + filename);
					if(!discoveryListArg.isEmpty()) {
						System.out.println("Failed..");
						usage();
						System.exit(1);
					}
					break;
					//
				case 'i':
					whoami = s.substring(2);
					System.out.println("I'm: " + whoami);
					break;
					//
				case 'd':
					discoveryListArg = s.substring(2);
					System.out.println("Discovery servers: " + discoveryListArg);
					if(!filename.isEmpty()) {
						System.out.println("Failed..");
						usage();
						System.exit(1);
					}
					break;
				default:
					System.out.println("Invalid arguments..");
					usage();
					System.exit(1);
					break;
			}
		}
		if(whoami.isEmpty()) {
			//error..
		}
		if(filename.isEmpty() && discoveryListArg.isEmpty()) {
			//error
		}

		if(filename.isEmpty()) {
			initConversionsStatic(discoveryListArg);
		} else {
			//if there is a file.. then discovery list is going to be in the file.. not outside..
			if(initConversionsFile(filename) != 0) {
				System.err.println("Failed to initialize conversions..exiting..\n");
				System.exit(1);
			}
		}

		//split my ip and the port number..
                String[] host = whoami.split(":");
                String myHost = "", myPort = "";
		if(host.length != 2) {
			System.err.println("Invalid input");
			System.exit(1);
		}
		myHost = host[0];
		myPort = host[1];
		myExtHost = myHost;

		int port = 0;
		try {
			// create socket
			port = Integer.parseInt(myPort);
			myPortNo = Integer.toString(port);
		} catch (NumberFormatException nfe) {
			System.err.println("Invalid port number..\n");
			System.exit(1);
		}

		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(port);
		} catch (Exception e) {
			System.err.println("Failed to create socket " + e.getMessage());
			System.exit(1);
		}
		System.out.println("Started server on port " + port);

		//Connect to discovery servers and register yourself! 
		if(addToDiscovery() < 0) {
			System.out.println("Couldn't add to discovery.. exiting..");
		}

		// wait for connections, and process
		try {
			while(true) {

				// a "blocking" call which waits until a connection is requested
				Socket clientSocket = serverSocket.accept();
				System.out.println("\nAccepted connection from client: " + clientSocket.getInetAddress() );
				try {
					//read client's request, process it..
					processClient(clientSocket);
				} catch (Exception e) {
					System.err.println("Failed to process client..");
					continue; // failure with one client doesn't mean, we exit..
				}
				clientSocket.close();
			}

		} catch (IOException e) {
			System.err.println("Connection Error");
		} catch (Exception e) {
			System.err.println("Exception caught..");
		}

		//Connect to discovery servers and register yourself! 
		if(removeFromDiscovery() < 0) {
			System.out.println("Couldn't remove from discovery..");

			System.exit(1);
		}

		serverSocket.close();
		System.exit(1); //should never get here..
	}
}
