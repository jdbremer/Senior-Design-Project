# first of all import the socket library
import socket
import _thread
import select
import time
import threading

lock = threading.Lock()

connections = {}

def on_new_client(clientSocket,addr):
    print ('Got connection from', addr )
    thankYouMsg = 'Thank you for connecting'
    clientSocket.send(thankYouMsg.encode('ascii'))
    print (clientSocket.recv(1024).decode('ascii'))
    while True:
        try:
            fromClient = clientSocket.recv(1024).decode('ascii')
            if(fromClient == ' '):
               clientSocket.close()
	       return
            print (clientSocket.recv(1024).decode('ascii'), addr)
            checkMsg = 'I am here'
            clientSocket.send(checkMsg.encode('ascii'))
        except socket.error:
#            print('Socket has disconnected! ', addr)
            clientSocket.close()
            return


def clientCloseCheck(clientSocket,addr):
    lock.acquire()
    clientSocket.send('connected....'.encode('ascii'))
    sensor = clientSocket.recv(1024).decode('ascii')
    print (sensor)    
 #   clientSocket.send('send client ID...'.encode('ascii'))
 #   clientID = clientSocket.recv(1024.decode('ascii'))
 #   print (clientID)
#    print(clientID[1])
    clientID = clientSocket.getsockname()
    print (clientID[1])
    connections[clientID[1]].append(sensor)

    lock.release()

    while True:
        #time.sleep(1)
        try:
            checkMsg = 'Are you there?'
            clientSocket.send(checkMsg.encode('ascii'))
        except socket.error:
            print('Socket has disconnected! ', addr)
            clientSocket.close()
            break

# next create a socket object
data = socket.socket()
status = socket.socket()
print ("Sockets successfully created")

# reserve a port on your computer in our
# case it is 12345 but it can be anything
dataPort = 12346
statusPort = 12347
# Next bind to the port
# we have not typed any ip in the ip field
# instead we have inputted an empty string
# this makes the server listen to requests
# coming from other computers on the network
data.bind(('', dataPort))
print ("data socket binded to %s" %(dataPort))

status.bind(('',statusPort))
print ("status socket binded to %s" %(statusPort))

# put the socket into listening mode
data.listen(5)
print ("data socket is listening")
status.listen(5)
print ("status socket is listening")

# a forever loop until we interrupt it or
# an error occurs
while True:
 #   data.listen(5)
#    print ("data socket is listening")
    # Establish connection with client.
    c, addr = data.accept()

    _thread.start_new_thread(on_new_client,(c,addr))
    #_thread.start_new_thread(clientCloseCheck,(c,addr))
    # send a thank you message to the client.
  #  status.listen(5)
  #  print ("status socket is listening")
    
    c, addr = status.accept()
    _thread.start_new_thread(clientCloseCheck, (c,addr))

    # Close the connection with the client
    #c.close()
