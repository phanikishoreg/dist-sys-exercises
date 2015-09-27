
import java.io.*;
import java.net.*;
public class ProxyServer 
{
	
	public static void main(String[] args) throws IOException 
	{
	        ServerSocket serverSocket= null;
	        Socket clientSocket= null;
	        int port= 0;
		
	        try
	        {
			
			//check if argument length is invalid
	      		if(args.length != 1)
	            	{
				System.err.println("Usage: java ProxyServer portnum");
			}
			connectDisServer();	
      // create socket
			port = Integer.parseInt(args[0]);
			serverSocket = new ServerSocket(port);
      			System.out.println("Started server on port " + port);
      // wait for connections, and process
            		while(true)
			{
              // a "blocking" call which waits until a connection is requested
                		clientSocket = serverSocket.accept();
                		System.err.println("\nAccepted connection from client");
               			process(clientSocket);
	            	}
        }
        catch (IOException ioe)
        {
            System.err.println("Connection Error");
        }
        catch (Exception e)
        {
            System.err.println("Connection Error");
        }
        System.exit(0);
	}
	public static void connectDisServer()
	{
		int discoveryServerPort=50000;
		BufferedReader input = null;
	        PrintWriter output = null;
		Socket discoveryServer=null;
		try{
			System.out.println("Helo");
			discoveryServer = new Socket("127.0.0.1",discoveryServerPort);
			input = new BufferedReader(new InputStreamReader(discoveryServer.getInputStream()));
			output = new PrintWriter(discoveryServer.getOutputStream(), true);
			output.print("Proxy-Server <IP_ADDR> "+" <PORT_NO>\n");
			input.close();
				output.close();
				discoveryServer.close();
			}catch(IOException ioe)
			{
				System.out.println("Disconnection to Discovery Server");
			}
			finally
			{
				System.out.println("Proxy Registered");
				
			}
	}
	public static void process(Socket clientSocket)throws IOException
	{
        BufferedReader in = null;
        PrintWriter out = null;
        String userInput = null;
        ProxyServer object =new ProxyServer();
        String tokens[] = null;
        try
        {
            // open up IO streams
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            /* Write a welcome message to the client */
            out.println("Welcome to the conversion server!\n");
            out.println("Enter in this format <input unit> <output unit> <input amount>\n");
            /* read and print the client's request */
            // readLine() blocks until the server receives a new line from client
            if ((userInput = in.readLine()) == null)
            {
          		System.out.println("Error reading message");
          		out.close();
          		in.close();
          		clientSocket.close();
      		}
      		 tokens = userInput.split(" ");
            if(tokens.length < 3)
            {
                System.out.println("Usage: <input unit> <output unit> <input amount>");
                out.println("Usage: <input unit> <output unit> <input amount>");
            }
            try
            {
                Double.parseDouble(tokens[2]);
		object.recursor(tokens, userInput, in, out, clientSocket);     
		 out.close();
                in.close();
                clientSocket.close();       
		}
            catch(NumberFormatException e)
            {
                out.println("Enter Correct Number...Closing Connection");
               
            }
            
		}
		catch(Exception e)
		{
			out.println("Error in connection...Closing Connection");
		}
        finally
        {
            out.close();
            in.close();
            clientSocket.close();
		System.out.println("Client Disconnected");
        }
	}
	public void recursor(String tokens[], String buff, BufferedReader in, PrintWriter out, Socket clientSocket)
	{//check for complex conversions
        String temp = null, Temp = null;
        double ip = 0.0;
        try
        {
            if((tokens[0].equals("kg") || tokens[0].equals("oz")) && (tokens[1].equals("oz") || tokens[1].equals("kg")) && !tokens[0].equals(tokens[1]))						//for kg<->g<->oz
            {
			
                temp=tokens[1];
                tokens[1]="g";
                Temp=tokens[0]+" "+tokens[1]+" "+tokens[2];//string in its receivable format for conversion server
                ip=calculationRepl(tokens,Temp, out);//for first conversion
                tokens[2]=Double.toString(ip);//output of first conversion used as input for the second
                tokens[0]=tokens[1];
                tokens[1]=temp;
                Temp=tokens[0]+" "+tokens[1]+" "+tokens[2];
                ip=calculationRepl(tokens,Temp, out);//for second conversion
                out.println(Double.toString(ip));
                out.close();
                in.close();
                clientSocket.close();}
            else if((tokens[0].equals("g") || tokens[0].equals("lbs")) && (tokens[1].equals("lbs") || tokens[1].equals("g")) && !tokens[0].equals(tokens[1]))					//for g<->kg<->lbs
            {
			
                temp=tokens[1];
                tokens[1]="kg";
                Temp=tokens[0]+" "+tokens[1]+" "+tokens[2];//string in its receivable format for conversion server
                ip=calculationRepl(tokens,Temp, out);//for first conversion
                tokens[2]=Double.toString(ip);//output of first conversion used as input for the second
                tokens[0]=tokens[1];
                tokens[1]=temp;
                Temp=tokens[0]+" "+tokens[1]+" "+tokens[2];
                ip=calculationRepl(tokens,Temp, out);//for second conversion
                out.println(Double.toString(ip));
                out.close();
                in.close();
                clientSocket.close();
            }
            else			//for direct conversions
            {
                ip=calculationRepl(tokens,buff, out);
                out.println(Double.toString(ip));
                out.close();
                in.close();
                clientSocket.close();
            }
        }
        catch(IOException ioe)
        {
            System.err.println("recursor Connection Error");
            out.println("Connection Error");
        }
        catch(Exception e)
        {
            System.err.println("recursor Connection Error");
            out.println("Connection Error");
        }
	}
	public double calculationRepl(String tokens[], String userInput, PrintWriter out)
	{	//checks direct conversions and calls the function to run the respective conversion servers
        Socket sock = null;
        String buff = null;
        double ip = 0.0;
        String kg_g_Host="128.164.94.148";//for kg<->g conversion Host
		int kg_g_Port=6000;
		String g_oz_Host="128.164.94.148";//for g<->oz conversion Host
		int g_oz_Port=7000;
		String kg_lbs_Host="127.0.0.1";//for kg<->lbs conversion Host
		int kg_lbs_Port=22222;
		String lbs_oz_Host="54.165.253.233";//for lbs<->oz conversion Host
		int lbs_oz_Port=11111;
        try
        {
      		if((tokens[0].equals("kg") || tokens[0].equals("g")) && (tokens[1].equals("g") || tokens[1].equals("kg")) && !tokens[0].equals(tokens[1]))
      		{
                sock = new Socket(kg_g_Host,kg_g_Port);
                buff=userInput;
                ip=convServer(sock, buff);
		sock.close();
		System.out.println("Connection to "+tokens[0]+"<->"+tokens[1]+"  Server closed");
                return ip;
            }
			
            else if(((tokens[0].equals("g") || tokens[0].equals("oz")) && (tokens[1].equals("oz") || tokens[1].equals("g")) && !tokens[0].equals(tokens[1])))
            {
                sock = new Socket(g_oz_Host,g_oz_Port);
                buff=userInput;
                ip=convServer(sock, buff);
                sock.close();
		System.out.println("Connection to "+tokens[0]+"<->"+tokens[1]+"  Server closed");
                return ip;
            }
            else if(((tokens[0].equals("lbs") || tokens[0].equals("kg")) && (tokens[1].equals("lbs") || tokens[1].equals("kg")) && !tokens[0].equals(tokens[1])))
            {
                sock = new Socket(kg_lbs_Host,kg_lbs_Port);
                buff=userInput;
                ip=convServer(sock, buff);
                sock.close();
		System.out.println("Connection to "+tokens[0]+"<->"+tokens[1]+"  Server closed");
                return ip;
            }
            else if(((tokens[0].equals("lbs") || tokens[0].equals("oz")) && (tokens[1].equals("lbs") || tokens[1].equals("oz")) && !tokens[0].equals(tokens[1])))
            {
                sock = new Socket(lbs_oz_Host,lbs_oz_Port);
                buff=userInput;
                ip=convServer(sock, buff);
                sock.close();
		System.out.println("Connection to "+tokens[0]+"<->"+tokens[1]+"  Server closed");
                return ip;
            }
            else if(tokens[0].equals(tokens[1]))
            {
                ip=Double.parseDouble(tokens[2]);
                return ip;
            }
            else
                out.println("Enter Correct units");
        }
        catch(IOException ioe)
        {
            System.err.println("calculationRepl IO Error");
        }
        catch(Exception e)
        {
            System.err.println("calculationRepl Connection Error");
        }
  		return 0.0;
	}
	public double convServer(Socket sock, String buff)
	{//calls the converter server needed
        BufferedReader input = null;
        PrintWriter output = null;
        String temp = null;
        double ip = 0.0;
		try 
		{
			 input = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			 output = new PrintWriter(sock.getOutputStream(), true);
            while(true)
			{
                System.out.println(input.readLine());
				output.println(buff);
				output.flush();
                temp=input.readLine();
                ip=Double.valueOf(temp);
				output.close();
				input.close();
				return ip;
			}
        }
        catch (IOException ioe)
		{
            System.err.println("Convserver IO Error");
            output.println("Connection Error");
        }
        catch (Exception e)
        {
            System.err.println("ConvServer Connection Error");
            output.println("Connection Error");
        }
        System.exit(0);
		return 0.0;
	}
}
