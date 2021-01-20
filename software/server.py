# first of all import the socket library
import socket
import _thread
import select
import time

def on_new_client(clientSocket,addr):
    print ('Got connection from', addr )
    thankYouMsg = 'Thank you for connecting'
    clientSocket.send(thankYouMsg.encode('ascii'))
    print (clientSocket.recv(1024).decode('ascii'))
    while True:
        try:
            print (clientSocket.recv(1024).decode('ascii'), addr)
        except socket.error:
#            print('Socket has disconnected! ', addr)
#            clientSocket.close()
            return


def clientCloseCheck(clientSocket,addr):
    while True:
        time.sleep(1)
        try:
            checkMsg = 'Are you there?'
            clientSocket.send(checkMsg.encode('ascii'))
        except socket.error:
            print('Socket has disconnected! ', addr)
            clientSocket.close()
            break

# next create a socket object
s = socket.socket()
print ("Socket successfully created")

# reserve a port on your computer in our
# case it is 12345 but it can be anything
port = 12346

# Next bind to the port
# we have not typed any ip in the ip field
# instead we have inputted an empty string
# this makes the server listen to requests
# coming from other computers on the network
s.bind(('', port))
print ("socket binded to %s" %(port))

# put the socket into listening mode
s.listen(5)
print ("socket is listening")

# a forever loop until we interrupt it or
# an error occurs
while True:

    # Establish connection with client.
    c, addr = s.accept()

    _thread.start_new_thread(on_new_client,(c,addr))
    _thread.start_new_thread(clientCloseCheck,(c,addr))
    # send a thank you message to the client.


    # Close the connection with the client
    #c.close()
