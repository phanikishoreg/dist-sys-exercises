#!/usr/bin/env python

#******************************************************************************
#
#  CS 6421 - HW3 Prototype2: A simple Print server that will display 
#			any messages that are sent to it. 
#
#  Execution:  python print_serve.py portnum
#
#  Team: Phani, Teo, Harpreet, Ashwini, Mruganka, Changle
#
#  To Do: Exception handling! 
#
#******************************************************************************

#import required packages
import socket
import sys

## Function to process requests
def process(conn):

    # read userInput from client, userInput must contain a space after 'get/set'
    userInput = conn.recv(BUFFER_SIZE)
    if not userInput:
        print "Error reading message"
        sys.exit(1)

    print "Received message: ", userInput

    conn.close()


### Main code run when program is started
BUFFER_SIZE = 1024
interface = ""

# if input arguments are wrong, print out usage
if len(sys.argv) != 2:
    print >> sys.stderr, "usage: python {0} portnum\n".format(sys.argv[0])
    sys.exit(1)

portnum = int(sys.argv[1])

# create socket
s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.bind((interface, portnum))
s.listen(5)

while True:
    # accept connection and print out info of client
    conn, addr = s.accept()
    print 'Accepted connection from client', addr
    process(conn)
s.close()
