#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>
#include <unistd.h>
#include <inttypes.h>
#include <fcntl.h>
#include <netdb.h>
#include <netinet/in.h>
#include <errno.h>
#include <getopt.h>
#include <string.h>
#include <sys/socket.h>
#include <arpa/inet.h>

static char* discPort;
static char* port;
static char* server_ip;
#define BUFSIZE 1024

/*
 * Print a usage message
 */
static void
usage(const char* progname) {
	printf("Usage:  %s portnum\n", progname);
}

/*
 * Converts between two units.
 * from_conv - source unit
 * to_conv - destination unit
 * input_val - value to conv to.
 *
 * return - converted value.
 */
float convert(char from_conv, char to_conv, float input_val) {

	float conv_val = 0.0f;
	switch(from_conv) {
		case 'b' : //b for banana
			switch(to_conv) {
				case 'l' : //l for pound
					conv_val = input_val / 3.0f;
					break;
				default: //bananas to any other conv is invalid..
					return -1.0f;
			}
			break;
		case 'l' : //l for pound
			switch(to_conv) {
				case 'b': //b for banana
					conv_val = input_val * 3.0f;
					break;
				default: //pounds to any other conv is invalid..
					return -1.0f;
			}
			break;
		default : //only pounds and bananas are considered.. 
			return -1.0f;
	}
	return conv_val;
}

/*
 *If connection is established then send out welcome message
 */
void
process(int sock)
{
	int n;
	char buf[BUFSIZE] = { '\0' };
	char userInput[BUFSIZE] = { '\0' };
	char* msg = "b<->lbs conversion in C\n";

	/* Write a welcome message to the client */
	n = write(sock, msg, strlen(msg));
	if (n < 0){
		perror("ERROR writing to socket");
		close(sock);

		return; //return from this client..
	}

	/* read and print the client's request */
	bzero(buf, BUFSIZE);
	n = read(sock, buf, BUFSIZE);
	if (n < 0){
		perror("ERROR reading from socket");
		close(sock);

		return; //return from this client..
	}
	strcpy(userInput, buf);

	const char s[2] = " "; // space as delimiter
	char *token;
	int tok_count = 0; // number of tokens parsed..
	/* get the first token */
	token = strtok(userInput, s);
	char from_conv[3] = { '\0' }, to_conv[3] = { '\0' }; //capture input and output conversions..
	float input_val = 0.0f; //input value converted to float..

	/* walk through other tokens */
	while( token != NULL ) {
		if(tok_count == 0) { //first token..from conv..
			if(!(strcmp(token, "b") == 0 || strcmp(token, "lbs") == 0)) {
				printf("Invalid input[%s]\n", buf);
				close(sock);

				return; //return from this client..
			}
			strcpy(from_conv, token);
			tok_count ++;
		} else if(tok_count == 1) { //second token..to conv..
			if(!(strcmp(token, "lbs") == 0 || strcmp(token, "b") == 0)) {
				printf("Invalid input[%s]\n", buf);
				close(sock);

				return; //return from this client..
			}
			strcpy(to_conv, token);
			tok_count ++;
		} else if(tok_count == 2) { //third token..input value..
			//atof doesn't report errors for invalid input.. going with that limitation..
			input_val = atof(token);
			tok_count ++;
		} else { //only 3 tokens supported..
			printf("Invalid input[%s]\n", buf);
			close(sock);

			return; //return from this client..
		}
		token = strtok(NULL, s);
	}
	//if number of tokens aren't matching the protocol.
	if(tok_count != 3) {
		printf("Invalid input[%s]\n", buf);
		close(sock);
		
		return;
	}

	//convert value using input and output units of conversion..
	float conv_val = convert(from_conv[0], to_conv[0], input_val);
	if(conv_val < 0.0f) { //negative is not allowed.. used only for detecting failure..
		printf("Conversion failed..\n");
		close(sock);

		return; //return from this client..
	}
	printf("%f %s = %f %s\n", input_val, from_conv, conv_val, to_conv);

	bzero(buf, BUFSIZE);
	sprintf(buf, "%f\n", conv_val);

	/* Write the converted value to the client */
	n = write(sock, buf, strlen(buf));
	if (n < 0){
		perror("ERROR writing to socket");
		close(sock);

		return; //return from this client..
	}

	close(sock);
}
int removeFromDiscovery(char* arg,char* argv)
{
        char* server_port =discPort;
        char* server_ip = arg;
	int bytesRead=-1;
	int sockfd;  
	struct addrinfo hints, *servinfo, *p;
	int rv;
	char hn[1024]={'0'};
	char* rec=NULL;
	char msg[1024]="REMOVE ";
	strcat(msg,argv); 
	strcat(msg," ");
	strcat(msg,port);			//remove message created
	printf("%s \n",msg);					//remove message printed
	memset(&hints, 0, sizeof(hints));
	hints.ai_family = AF_INET; // ipv4
	hints.ai_socktype = SOCK_STREAM;
	if ((rv = getaddrinfo(server_ip, server_port, &hints, &servinfo)) != 0) 
	{
    		fprintf(stderr, "getaddrinfo: %s\n", gai_strerror(rv));
    		exit(1);
	}

// loop through all the results and connect to the first we can
	for(p = servinfo; p != NULL; p = p->ai_next) 
	{
		if ((sockfd = socket(p->ai_family, p->ai_socktype,p->ai_protocol)) == -1) 
		{
        		perror("socket");
        		continue;
    		}

    		if ((connect(sockfd, p->ai_addr, p->ai_addrlen)) == -1) 
		{
        		close(sockfd);
        		perror("connect");
        		continue;
    		}
		
		send(sockfd,msg,strlen(msg),0);//send message
		rec=malloc((100)*sizeof(char));		//for receiving reply from discovery 
		memset(rec,0,99*sizeof(char));
		while((bytesRead=recv(sockfd,rec,100,0))>0)//receive into rec and number of characters returned
		{
			rec[bytesRead]='\0';
			if((strcasecmp(rec,"success\n"))==0)	//if remove is success
			{
				close(sockfd);
				return sockfd;
			}
			else if(strcasestr(rec,"NOTFOUND\n"))	//if server does exist, do not reinitialize, and keep the server running
			{
				close(sockfd);
				return sockfd;
			}
			else		//if messages do not transmit
			{
				printf("Error transmitting messages to Discovery");
				close(sockfd);
				return -1;
			}				
		}
		break;
	}
}


int addToDiscovery(char* argv,char* arg)
{
        char* server_port = discPort;
        char* server_ip = argv;
	int bytesRead=-1;
	int sockfd;  
	struct addrinfo hints, *servinfo, *p;
	int rv;
	char hn[1024]={'\0'};
	char* rec=NULL;
	char msg[1024]="ADD b lbs ";
	strcat(msg,arg); 
	strcat(msg," ");
	strcat(msg,port);		//add message created
	strcat(msg,"\n\0");
	printf("%s \n",msg);					//add message printed
	memset(&hints, 0, sizeof(hints));
	hints.ai_family = AF_INET; // ipv4
	hints.ai_socktype = SOCK_STREAM;
	if ((rv = getaddrinfo(server_ip, server_port, &hints, &servinfo)) != 0) 
	{
    		fprintf(stderr, "getaddrinfo: %s\n", gai_strerror(rv));
    		exit(1);
	}

// loop through all the results and connect to the first we can
	for(p = servinfo; p != NULL; p = p->ai_next) 
	{
		if ((sockfd = socket(p->ai_family, p->ai_socktype,p->ai_protocol)) == -1) 
		{
        		perror("socket");
        		continue;
    		}

    		if ((connect(sockfd, p->ai_addr, p->ai_addrlen)) == -1) 
		{
        		close(sockfd);
        		perror("connect");
        		continue;
    		}
		
		rec=malloc((100)*sizeof(char));//for receiving reply from discovery 
		memset(rec,0,100*sizeof(char));
		send(sockfd,msg,strlen(msg),0);//send message
		
		bytesRead=recv(sockfd,rec,99,0);
		if(bytesRead>0)//receive into rec and number of characters returned
		{
			rec[bytesRead]='\0';
			if((strcasecmp(rec,"success\n"))==0)//if add is success
			{
				close(sockfd);
				return sockfd;
			}
			else if(strcasestr(rec,"EXISTS\n"))//if server already exists, do not reinitialize, and keep the server running
			{
				close(sockfd);
				return sockfd;
			}
			else//if messages do not transmit
			{
				printf("Error transmitting messages to Discovery");
				close(sockfd);
				return -1;
			}				
		} else {
			perror("recv");
			return -1;
		}
		
	}
}

/*
 * Server
 */
int
server( char* argv, char* arg )
{
	int optval = 1;
	int sockfd, newsockfd;
	socklen_t clilen;
	struct sockaddr_in serv_addr, cli_addr;
	int  n,conn;
	/* First call to socket() function */
	sockfd = socket(AF_INET, SOCK_STREAM, 0);
	setsockopt(sockfd, SOL_SOCKET, SO_REUSEADDR, &optval, sizeof optval);

	if (sockfd < 0){
		perror("ERROR opening socket");
		exit(1);
	}
	/* Initialize socket structure */
	bzero((char *) &serv_addr, sizeof(serv_addr));
	serv_addr.sin_family = AF_INET;
	serv_addr.sin_addr.s_addr = INADDR_ANY;
	serv_addr.sin_port = htons(atoi(port));
	/* Now b	ind the host address using bind() call.*/
	if (bind(sockfd, (struct sockaddr *) &serv_addr, sizeof(serv_addr)) < 0){
		perror("ERROR on binding");
		close(sockfd); //cleanup
		exit(1);
	}
	printf("Started server on port %s\n",port);
	conn=addToDiscovery(argv,arg);		//add the server to discovery after it has started
	/* Listening for the client */
	listen(sockfd,5);
	clilen = sizeof(cli_addr);
	
	/* Loop forever receiving client connections */
	if(conn!=-1)
	{
	while(1) {
		/* Accept connection from the client */
		newsockfd = accept(sockfd, (struct sockaddr *)&cli_addr, &clilen);
		if (newsockfd < 0){
			perror("ERROR on accept");
			close(sockfd); //cleanup.
			exit(1);
		}
		printf("Accepted connection from client[%s]\n", inet_ntoa(cli_addr.sin_addr));
		/* Process a client request on new socket */
		process(newsockfd);
//		conn=removeFromDiscovery(argv,arg);		//to be be done when we have exit condition for server
//		break;
	}
	close(sockfd);
	}
	else
	{
		close(sockfd);
		return 0;
	/*clean up*/
	
	}
	
	return conn;//will return non negative value if everything was a success
}

int main(int argc, char ** argv){

	const char* progname = argv[0];
	//program name, DiscoveryIP discoveryPort YourIP port number.
	if (argc != 5){
		usage(progname);
		exit(1);
	}
	port = argv[4];
	discPort=argv[2];
	//start server..
	if (server(argv[1], argv[3]) < 0)//negative value means the server failed somewhere
	{
		printf("server in trouble\n");
		exit(1);
	}
	return 0;
}


