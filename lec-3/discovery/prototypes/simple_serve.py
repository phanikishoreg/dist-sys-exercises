#!/usr/bin/env python

#******************************************************************************
#
#  CS 6421 - HW3 Prototype1: A server that calls different functions 
#		depending on whether a client sends a string 
#		where the first word is "set" or "get".
#
#  Execution:  python simple_serve.py portnum
#
#  Team: Phani, Teo, Harpreet, Ashwini, Mruganka, Changle
#
#  To Do: Exception handling! 
#
#******************************************************************************

#import required packages
import socket
import sys

## function 1: set function stub
def self_set():
    print "Doing a batch of fun stuff in the set function!"

## function 2: get function stub
def self_get():
    print "Doing a different batch of fun stuff in the get function!"

## function 3: unknown command stub
def funFinally():
    print "You're probably calling this wrong but we're doing a final batch of fun stuff in the last function anyway!"


## Function to process requests
def process(conn):

    # read userInput from client, userInput must contain a space after 'get/set'
    userInput = conn.recv(BUFFER_SIZE)
    if not userInput:
        print "Error reading message"
        sys.exit(1)

    print "Received message: ", userInput
    input = userInput.split(' ')

    # parse input for commands - supports: get/set
    if input[0] == 'get':
        self_get()
    elif input[0] == 'set':
        self_set()
    else:
        funFinally()

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
