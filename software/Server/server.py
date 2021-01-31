# first of all import the socket library
import socket
import _thread
import select
import time
import threading
import pyrebase
import time


##DATABASE##
config = {
    "apiKey": "AIzaSyAcaqrqFZYmvcAb0qFCI9N4QiZ6L6OeuZ8",
    "authDomain": "seniordesign-ajr.firebaseapp.com",
    "databaseURL": "https://seniordesign-ajr-default-rtdb.firebaseio.com",
    "storageBucket": "seniordesign-ajr.appspot.com",
}

firebase = pyrebase.initialize_app(config)

storage = firebase.storage()
database = firebase.database()
##END DATABASE##


lock = threading.Lock() #thread lock initialization

connections = {} #global dictionary initialization

def on_new_client(clientSocket,addr,statusSocket):
    print ('Got connection from', addr )
    thankYouMsg = 'Thank you for connecting'
    clientSocket.send(thankYouMsg.encode('ascii'))
    print (clientSocket.recv(1024).decode('ascii'))
    checkMsg = 'I am here'
    clientSocket.send(checkMsg.encode('ascii'))
    while True:
        try:
            fromClient = clientSocket.recv(1024).decode('ascii')

            if(fromClient == ''):
                clientSocket.close()
                statusSocket.close()
                return
            print(fromClient)
            #print (clientSocket.recv(1024).decode('ascii'), addr)
            checkMsg = 'I am here'
            clientSocket.send(checkMsg.encode('ascii'))
        except socket.error:
#            print('Socket has disconnected! ', addr)
            clientSocket.close()
            statusSocket.close()
            return


def clientCloseCheck(clientSocket,addr, dataSocket):
    lock.acquire()
    clientSocket.send('connected....'.encode('ascii'))
    sensor = clientSocket.recv(1024).decode('ascii')
    print (sensor)
 #   clientSocket.send('send client ID...'.encode('ascii'))
 #   clientID = clientSocket.recv(1024.decode('ascii'))
 #   print (clientID)
#    print(clientID[1])
    print (addr[1])
    connections[addr[1]] = sensor



    lock.release()

    database.child("Connections").update({str(sensor) : "1"})
    #data = {str(sensor) : "1"}
    #database.set(data)


    while True:
        #time.sleep(1)
        try:
            checkMsg = 'Are you there?'
            clientSocket.send(checkMsg.encode('ascii'))
        except socket.error:
            lock.acquire()
            valueToPull = connections.get(addr[1])
            del connections[addr[1]]
            lock.release()
            database.child("Connections").update({str(valueToPull) : "0"})
            #data = {str(valueToPull) : "0"}
            #database.set(data)
            print('Socket has disconnected! ', addr)

            clientSocket.close()
            dataSocket.close()
            break

# next create a socket object
data = socket.socket()
status = socket.socket()
print ("Sockets successfully created")

# reserve a port on your computer in our
# case it is 12345 but it can be anything
dataPort = 12350
statusPort = 12351
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

    data_accept, data_addr = data.accept()
    status_accept, status_addr = status.accept()
    _thread.start_new_thread(clientCloseCheck, (status_accept,status_addr,data_accept))
    _thread.start_new_thread(on_new_client,(data_accept,data_addr,status_accept))
    # Close the connection with the client
    #c.close()
