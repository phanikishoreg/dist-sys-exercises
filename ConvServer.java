import java.net.*;
import java.io.*;

public class ConvServer 
{
	public static void process (Socket clientSocket) throws IOException 
	{
		ConvServer object=new ConvServer();	
	        // open up IO streams
	        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        	out.println("Welcome, you are connected to kg-g server.");
	        //Read the input from the client using readline()
	        // readLine() blocks until the server receives a new line from client
	        String userInput;
	        if ((userInput = in.readLine()) == null) 
	        {
	        	System.out.println("Error reading message");
	        	out.close();
	            	in.close();
	            	clientSocket.close();
	        }
		String msg=object.func(userInput, out);
		out.close();    //close bufferedreader object
	        in.close();     //close printwriter object
		clientSocket.close();  //close the socket object
	}
	public static void addToDiscovery()
	{//adding the conversion server to the discovery table
		BufferedReader input= null;
		int discoveryServerPort=50000;
	        PrintWriter output= null;
		Socket discoveryServer=null;
		try{
			System.out.println("Helo");
			discoveryServer = new Socket("127.0.0.1",discoveryServerPort);//connect to discovery
			input=new BufferedReader(new InputStreamReader(discoveryServer.getInputStream()));
			
	        	output=new PrintWriter(discoveryServer.getOutputStream(), true);
			output.print("ADD "+"kg "+"g "+"<IP_ADDR> "+"<PORT_NO>\n");//give the discovery the details
//			output.print("ADD "+"g "+"kg "+"<IP_ADDR> "+"<PORT_NO>\n");//if the discovery server takes unidirectional units
			input.close();
			output.close();
			discoveryServer.close();
			discoveryServer=output=input=null;
			}catch(IOException ioe)
			{
				System.out.println("Error: Disconnection to Discovery Server");
			}
			finally
			{
				System.out.println("Server Registered");
			}
	
	}		
	public String func(String userInput, PrintWriter out)
	{
			
		String Amt;
		double amt,output=0.0;
		String tokens[]=userInput.split(" ");    //split the user input into tokens separated by spaces using the split()
		for(String s:tokens)
		{
			System.out.println(s);
			System.out.println("\n");	
		}
		try
		{
	        //input is in "unit unit amount" format hence after using split()
	        //token[2] has the number(amount) which is to be converted
			amt=Double.valueOf(tokens[2]);
			if(tokens[0].equals("kg") && tokens[1].equals("g")) //checks if it is kg to g conversion
			{
				output=amt*1000;
				String Output=Double.toString(output); //the output obtained is converted to sting from double
				out.println(Output+'\n');	
				out.flush();	
				return Output;
			}
    			else if(tokens[0].equals("g") && tokens[1].equals("kg")) //checks if it is g to kg conversion
		    	{
			    	output=amt/1000;
				String Output=Double.toString(output); //the output obtained is converted to sting from double
				out.println(Output+'\n');
				out.flush();
				return Output;
		    	}
			else  //if the input is not in "unit unit amount" format print error indicating invalid inputs
			{
				out.println("Invalid Units"+tokens[0]+" and "+ tokens[1]);
				return null;
			}
    		}catch(NumberFormatException e)
		{
		        //if the given input does not contain a valid numeral this exception is thrown
			out.println("Please enter a valid numeral");
			System.out.println("Invalid Number Input. Disconnecting");
			return null;
		}
	}
    	public static void main(String[] args) throws Exception {

        //check if argument length is invalid
        if(args.length != 1) 
        {
            System.err.println("Usage: java ConvServer port");
        }
        // create socket
        int port = Integer.parseInt(args[0]);
        ServerSocket serverSocket = new ServerSocket(port);
	addToDiscovery();
        System.err.println("Started server on port " + port);
        // wait for connections, and process
        try {
            while(true) {
                // a "blocking" call which waits until a connection is requested
                Socket clientSocket = serverSocket.accept();
                System.err.println("\nAccepted connection from client");
                process(clientSocket);
            }

        }catch (IOException e) 
        {
            //if the given input is invalid IOException is thrown
            System.err.println("Connection Error");
        }
        System.exit(0);
    }
}
