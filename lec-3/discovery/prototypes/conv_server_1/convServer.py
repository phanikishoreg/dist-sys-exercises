#!/usr/bin/env python

#******************************************************************************
#
#  CS 6421 - Simple Conversation for cm<->m
#  Execution:    python convServer.py portnum
#
#  Students: Phani, Changle
#
#******************************************************************************

import socket
import sys

discIp = ""
discPort = 0

# Convert function to convert between cm <-> m.
def convert(fromConv, toConv, inpVal):
	if fromConv == "cm" and toConv == "m":
		return str((float(inpVal) / 100.0))
	elif fromConv == "m" and toConv == "cm":
		return str((float(inpVal) * 100.0))
	else:
		return str() #error..return empty string.

## Function to process requests
def process(conn):
	#conn.send("Welcome, you are connected to a Python-based server\n")
	conn.send("cm<->m conversion in python\n")

	# read userInput from client
	userInput = conn.recv(BUFFER_SIZE)
	if not userInput:
		print "Error reading message"
		return

	countTokens = 0
	fromConv = ""
	toConv = ""
	inputVal = ""
	# TODO: add convertion function here, reply = func(userInput)
	reqTokens = userInput.split(" ")
	for item in reqTokens:
		if countTokens == 0: #first token, from conv.
			fromConv = item
			countTokens += 1
		elif countTokens == 1: #second token, to conv.
			toConv = item
			countTokens += 1
		elif countTokens == 2: #third token, input value.
			inputVal = item.strip()
			countTokens += 1
		else: #more tokens? invalid input.
			print "Invalid input", userInput
			conn.close()
			return

	if countTokens != 3: #very few tokens? invalid input.
		print "Invalid input", userInput
		conn.close()
		return

	#convert value..
	response = convert(fromConv, toConv, inputVal)
	if response == "":
		print "Failed to convert"
		conn.close() #close connection.. no standard defined to return response in failure.
		return

	print inputVal, fromConv, "=", response, toConv
	#send converted value to client and close client socket..
	conn.send(response + str("\n"))
	conn.close()

def discovery(request):
	#create socket..
	discsocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
	#connect to (host,portnum) tuple..
	discsocket.connect((discIp, discPort))

	print "Sending data: [" + request + "]"
	discsocket.send(request + str("\n")) #send data to socket -> server..error not caught!!
	discsocket.close() #cleanup..
	print("..done.")


### Main code run when program is started
BUFFER_SIZE = 1024
interface = ""

# if input arguments are wrong, print out usage
if len(sys.argv) <= 1 or len(sys.argv) > 4:
	print >> sys.stderr, "usage: python {0} portnum\n".format(sys.argv[0])
	sys.exit(1)

portnum = int(sys.argv[1])

try:
	# create socket
	s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
except socket.error as errmsg: #if failed..
        print "socket create error:", errmsg
        sys.exit(-1) #terminate..

try:
	s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1) #if terminated using Ctrl-C, it should not yell next time..
	s.bind((interface, portnum))
	s.listen(5)
except socket.error as errmsg: #if failed..
        print "socket error:", errmsg
        s.close() #cleanup..
        sys.exit(-1) #terminate..

try:
	discIp = sys.argv[2]
	discPort = int(sys.argv[3])
	discovery("hello there!")
except:
	print "something went wrong.."
	#just continue

print "started server on port ", str(portnum)
while True:
	try:
		# accept connection and print out info of client
		conn, addr = s.accept()
		print 'Accepted connection from client', addr
		try:
			process(conn)
		except Exception as errmsg:
			print "Caught exception:", errmsg
			conn.close()
			continue #don't need to terminate..if we failed to handle one client.
	except Exception as errmsg: #if failed..
		print "Caught exception:", errmsg
		s.close() #cleanup..
		sys.exit(-1) #terminate..

s.close()
