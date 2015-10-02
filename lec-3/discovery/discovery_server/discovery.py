#!/usr/bin/env python

#******************************************************************************
#
#  CS 6421 - Discovery Server [HW4]
#  Execution:    python discovery.py portnum
#
#  Initial: Wenhui (Thank you!)
#
#******************************************************************************
#
# Students: Phani, Teo, Harpreet, Ashwini, Mruganka, Changle
#

import socket
import sys
import string

#******************************************************************************
#   Discovery Server Class
#******************************************************************************
class DiscoveryServer(object):
    def __init__(self):
        self.BUFFER_SIZE = 1024
        self.server_dict = dict()

    
    #******************************************************************************
    #   ADD function
    #   
    #   adds the conversion server to dict
    #******************************************************************************
    def add(self, userInputs):
        print('add...')
        if len(userInputs) != 4:
            print "ADD UNIT1 UNIT2 IP PORT\n"
            return "FAILURE"

        print userInputs[0], userInputs[1], userInputs[2], userInputs[3]

        keys = [userInputs[0].strip(), userInputs[1].strip()]
        # Sort keys to avoid adding 2 entries for unit1-unit2/unit2-unit1
        keys.sort()
        # Key is conversion type seperated by '-'
        key = keys[0].strip() + "-" + keys[1].strip()
        # Value is <ip/hostname>:<portno> seperated by ':'
        value = userInputs[2].strip() + ":" + userInputs[3].strip()

        # if key exists
        if key in self.server_dict:
            # get the list of servers..
            listvalue = self.server_dict[key]
            for val in listvalue:
                # if the server already exists.. return FAILURE
                if val == value:
                    return "FAILURE EXISTS"
            # add new server to list
            listvalue.append(value)
            # update dictionary with this new list
            self.server_dict[key] = listvalue
        else:
            # if key doesn't exist
            strlist = [value]
            # add key, list(value) to dict
            self.server_dict[key] = strlist

        print self.server_dict
        res = "SUCCESS"

        return res
    
    #******************************************************************************
    #   REMOVE function
    #
    #   removes the conversion server from dict
    #******************************************************************************
    def remove(self, userInputs):
        print('remove...')
        if len(userInputs) != 2:
            print "REMOVE IP PORT\n"
            return "FAILURE"

        print userInputs[0], userInputs[1]
        value = userInputs[0].strip() + ":" + userInputs[1].strip()
        # to see if the server was found!
        count_find = 0 

        # for each conversion type (key)
        for k in self.server_dict.keys():
            listvalue = self.server_dict[k]
            # get list of servers..
            for val in listvalue:
                if val == value:
                    #delete if the server exists in the list
                    listvalue.remove(value)
                    count_find += 1
            # if after removing servers, list is empty
            if len(listvalue) == 0:
                # delete key
                del self.server_dict[k]
            else:
                # just update the listvalue 
                self.server_dict[k] = listvalue 

        # server to remove wasn't found? just return failure
        if count_find == 0:
            return "FAILURE NOTFOUND"

        print self.server_dict
        res = 'SUCCESS'
        
        return res
    
    #******************************************************************************
    #   LOOKUP function
    #
    #   searches for conversion server for a conversion type
    #******************************************************************************
    def lookup(self, userInputs):
        print('lookup...')
        if len(userInputs) != 2:
            print "GET UNIT1 UNIT2\n"
            return "FAILURE"

        print userInputs[0], userInputs[1]
        # remember? conversion type is sorted for uniqueness..
        userInputs.sort()
        key = userInputs[0].strip() + "-" + userInputs[1].strip()

        # if this conversion type is found in dict (key)
        if key in self.server_dict:
            # get the list
            listvalue = self.server_dict[key]
            # get first server in the list.. and return that in protocol format..
            resp = listvalue[0].split(':')
            res = resp[0] + " " + resp[1] 
        else:
            return "None"

        return res
    
    #******************************************************************************
    #   process function
    #
    #   Function to process requests
    #******************************************************************************
    def process(self, conn):
        # conn.send(self.welcome)
        # read userInput from client
        userInput = conn.recv(self.BUFFER_SIZE)
        print "Request: " + userInput.strip('\n')

        if not userInput:
                print "Error reading message\n" 
                # error case! return FAILURE to user..
                res = "FAILURE\n"
                conn.send(res)
                conn.close()
                return

        # split request by SPACE
        userInputs = userInput[:-1].split(' ')

        # call required function based on the request..
        if userInputs[0].upper() == 'ADD':
            res = self.add(userInputs[1:])
        elif userInputs[0].upper() == 'REMOVE':
            res = self.remove(userInputs[1:])
        elif userInputs[0].upper() == 'LOOKUP':
            res = self.lookup(userInputs[1:])
        else:
            print 'just ADD/REMOVE/LOOKUP actions allowed\n'
            res = "FAILURE\n"
            conn.send(res);
            conn.close()
            return

        print "Response: " + res + '\n' 
        conn.send(res + '\n')
        conn.close()




#******************************************************************************
#   Main code run when program is started
#******************************************************************************
if __name__ == "__main__":
    
    interface = ""
   
    try:
        # discovery server only needs its portno to start listening on.
        if len(sys.argv) != 2:
            print >> sys.stderr, "usage: python {0} portnum\n".format(sys.argv[0])
            sys.exit(1)
        else:
            portnum = int(sys.argv[1])

        # create socket
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

        # this helps in that weird Ctrl-C address in use error
        s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)

        s.bind((interface, portnum))
        s.listen(5)
        print('Server is running. Port:' + str(portnum))
        action = DiscoveryServer()

        # Server should be up and running and listening to the incoming connections
        while True:
            # Set up a new connection from the client
            conn, addr = s.accept()
            # If an exception occurs during the execution of try clause
            # the rest of the clause is skipped
            # If the exception type matches the word after except
            # the except clause is executed
            try:
                # Receives the request message from the client
                print 'Accepted connection from client', addr
                # Process the connection
                action.process(conn)
            except IOError:
                # Close the client connection socket
                conn.close()
            except:
                # caught some exception
                conn.close()

        # Close the Server connection socket
        s.close()    
        # Exit
        sys.exit(0)
    except:
        print "Oh no!! You will not be happy to see this!"
        sys.exit(1)

