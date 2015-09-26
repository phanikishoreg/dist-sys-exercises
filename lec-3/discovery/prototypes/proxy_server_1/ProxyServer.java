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

	// Conversion server list: Conversion type, Server tuple(host, ip)
	static Map<String, Server> convServers;
	// Conversion table: Supported conversion types, conversion table (step by step conversions)
	static Map<String, String> convTable;

	// Reads conversion data from a file..
	static int initConversionsFile(String confFile) {
		/* Sample:
		   =CONVERSION_SERVER_LIST
		   in->ft,localhost:9999
		   ft->in,localhost:9999
		   in->cm,localhost:9998
		   cm->in,localhost:9998
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
		convServers = new HashMap<String, Server>();
		convTable = new HashMap<String, String>();
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
				if(sCurrentLine.equals("=CONVERSION_SERVER_LIST") && (startServList != true)) {
					startServList = true;
					continue;
				}
				//end of conversion servers..
				else if(sCurrentLine.equals("=END_LIST") && (startServList == true && endServList != true)) {
					endServList = true;
					continue;
				}
				//start of conversion table.. remember: conversion table only allowed after conversion servers end..
				else if(sCurrentLine.equals("=CONVERSION_TABLE") && (startServList == true && endServList == true && startConvTable == false)) {
					startConvTable = true;
					continue;
				}
				//end of conversion table..
				else if(sCurrentLine.equals("=END_TABLE") && startConvTable == true) {
					endConvTable = true;
					continue;
				}
				//after conversion server start tag..each line contains conversion server information..
				else if(startServList = true && endServList == false) {
					//read conv serv list
					//ex: in->ft,localhost:9999
					String[] conv = sCurrentLine.split(",");
					if(conv.length != 2) {
						System.out.println("parse:ignore line - " + sCurrentLine);
						//ignore host..
						continue;
					}
					//conv 0-> conversion type
					//conv 1-> host:port
					String[] host = conv[1].split(":");
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
					convServers.put(conv[0], new Server(host[0], portno));
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
		System.out.println(convServers);
		System.out.println(convTable);

		return 0;
	}

	// Initialize conversion server list and conversion table.
	// TBD: 
	// 	1. Read conversion table and conversion server list from a config file
	//	2. Making undirected conversion table. (Makes splitConvert() do more computation..)
	static void initConversionsStatic() {
		convServers = new HashMap<String, Server>();
		convTable = new HashMap<String, String>();

		// Add list of conversion servers here. 
		// Avoid duplicates (same conversion by different servers).. Not supported yet!! 
		convServers.put("in->ft", new Server("localhost", 9999));
		convServers.put("ft->in", new Server("localhost", 9999));
		convServers.put("in->cm", new Server("localhost", 9998));
		convServers.put("cm->in", new Server("localhost", 9998));
		convServers.put("m->cm", new Server("localhost", 9997));
		convServers.put("cm->m", new Server("localhost", 9997));
		convServers.put("b->in", new Server("localhost", 9996));
		convServers.put("in->b", new Server("localhost", 9996));
		convServers.put("b->lbs", new Server("localhost", 9995));
		convServers.put("lbs->b", new Server("localhost", 9995));

		// Keeping directed routing makes it lot easier..!!
		// Support one level conversions using conversion server list.
		convTable.put("ft->in", "ft->in");
		convTable.put("in->ft", "in->ft");
		convTable.put("in->cm", "in->cm");
		convTable.put("cm->in", "cm->in");
		convTable.put("m->cm", "m->cm");
		convTable.put("cm->m", "cm->m");
		convTable.put("b->in", "b->in");
		convTable.put("in->b", "in->b");
		convTable.put("b->lbs", "b->lbs");
		convTable.put("lbs->b", "lbs->b");

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

		System.out.println(convServers);
		System.out.println(convTable);
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
			Server convSrv = (Server)convServers.get(s);
			String[] convParam = s.split("->");

			System.out.print("Step " + i + "/" + conversions.length + " ");
			//if no conversion server found..return..
			if(convSrv == null) {
				System.out.println("Not supported: " + convParam[0] + "->" + convParam[1]);
				return "";
			}
			System.out.print("using " + convSrv.getHost() + ":" + convSrv.getPort());

			//create request : <from> <to> <inputvalue>
			String request = convParam[0] + " " + convParam[1] + " " + convString;
			//get response: <outputvalue>
			String response = "";
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
		for(String s:tokens){
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

	//Just discovery. At this point, only just sends a message - hello or something..
	public static void processDiscovery (Socket clientSocket, String message) throws IOException {
		// open up IO streams
		PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
		System.out.println("Sending message [" + message + "] to " + clientSocket.getInetAddress());

		/* Write a welcome message to the discovery */
		out.println(message);
		// close only the IO stream
		System.out.println("done.\n");
		out.close();
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

		//check if argument length is invalid
		if(args.length > 4 || args.length == 0) {
			usage();
			System.exit(1);
		}
		if(args.length == 1 || args.length == 3) {
			initConversionsStatic();
		} else {
			if(initConversionsFile(args[1]) != 0) {
				System.err.println("Failed to initialize conversions..exiting..\n");
				System.exit(1);
			}
		}
		int port = 0;
		try {
			// create socket
			port = Integer.parseInt(args[0]);
		} catch (NumberFormatException nfe) {
			System.err.println("Invalid port number..\n");
			System.exit(1);
		}

		Socket discovSocket = null;
		if(args.length >= 3) {
			//send hello message: prototype 4 - to the print_serve 
			//since 2nd param(config file) is an optional parameter.. 
			//TODO: Change to use options and GetArgs.. -conf, -discIp -discPort etc..
			int indexIp = (args.length == 3) ? 1 : 2;
			int indexPort = (args.length == 3) ? 2 : 3;
			try {
				int discPort = Integer.parseInt(args[indexPort]);
				//connect to server..
				discovSocket = new Socket(args[indexIp], discPort);
				processDiscovery(discovSocket, "hello there!");	
			} catch (Exception e) {
				System.err.println("Exception caught. Failed to send hello to print_serve: " + e.getMessage());

				//don't return.. we can continue 
			}
			discovSocket.close();
			discovSocket = null;
		}

		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(port);
		} catch (Exception e) {
			System.err.println("Failed to create socket " + e.getMessage());
			System.exit(1);
		}
		System.out.println("Started server on port " + port);

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
			}

		} catch (IOException e) {
			System.err.println("Connection Error");
		} catch (Exception e) {
			System.err.println("Exception caught..");
		}
		System.exit(1); //should never get here..
	}
}
