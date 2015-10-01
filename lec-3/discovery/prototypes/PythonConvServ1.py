import socket
import sys

class Register(object):
    def __init__(self):
        # Discov Server
        self.discov_ip = sock.gethostbyname()
        self.discov_portnum = inputVal
        self.unit1 = fromConv
        self.unit2 = toConv

    #******************************************************************************
    #   Register Request at Discov Server
    #******************************************************************************
    def register(self):
        # send discov msg when turned on
        try:
            sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            sock.connect((self.discov_ip, self.discov_portnum))
            self.msg = self.unit1 + ' ' + self.unit2 + ' ' + sock.getsockname()[0] + ' ' + sys.argv[1]
            sock.send(self.msg)
            print "registed"
            sock.close()
        except KeyboardInterrupt:
            sock.close()
            sys.exit(1)
        except IOError:
            # Close the client connection socket
            sock.close()
            sys.exit(1)
    
    #******************************************************************************
    #   Unregister Request at Discov Server
    #******************************************************************************
    def unregister(self):
        # send discov msg when turned off
        try:
            sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            sock.connect((self.discov_ip, self.discov_portnum))
            sock.send(self.msg)
            self.msg = self.unit1 + ' ' + self.unit2 + ' ' + sock.getsockname()[0] + ' ' + sys.argv[1]
            print "unregisted"
            sock.close()
        except KeyboardInterrupt:
            sock.close()
            sys.exit(1)
        except IOError:
            # Close the client connection socket
            sock.close()
            sys.exit(1)

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

### Main code run when program is started
BUFFER_SIZE = 1024
interface = ""

# if input arguments are wrong, print out usage
if len(sys.argv) != 2:
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

print "started server on port ", str(portnum)
while True:
    try:
        # accept connection and print out info of client
        conn, addr = s.accept()
        print 'Accepted connection from client', addr
        register(self)
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
unregister(self)
