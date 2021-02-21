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

def receiveClient(recvDataSocket, addr, statusSocket, sendDataSocket):
    print ('Got connection from recv client.. ', addr )
    thankYouMsg = 'Server :: Thank you for connecting.. '
    recvDataSocket.send(thankYouMsg.encode('ascii'))
    print (recvDataSocket.recv(1024).decode('ascii'))
    checkMsg = 'Connection Successful..'
    recvDataSocket.send(checkMsg.encode('ascii'))
    while True:
        try:
            fromClient = recvDataSocket.recv(1024).decode('ascii')

            if(fromClient == ''):
                recvDataSocket.close()
                statusSocket.close()
                return
            print(fromClient)
            #print (recvDataSocket.recv(1024).decode('ascii'), addr)
            checkMsg = 'I am here'
            recvDataSocket.send(checkMsg.encode('ascii'))
        except socket.error:
#            print('Socket has disconnected! ', addr)
            recvDataSocket.close()
            sendDataSocket.close()
            statusSocket.close()
            return
            



def clientCloseCheck(statusSocket, addr, recvDataSocket, sendDataSocket):
    lock.acquire()
    statusSocket.send('connected....'.encode('ascii'))
    sensor = statusSocket.recv(1024).decode('ascii')
    print (sensor)
 #   statusSocket.send('send client ID...'.encode('ascii'))
 #   clientID = statusSocket.recv(1024.decode('ascii'))
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
            statusSocket.send(checkMsg.encode('ascii'))
        except socket.error:
            lock.acquire()
            valueToPull = connections.get(addr[1])
            del connections[addr[1]]
            lock.release()
            database.child("Connections").update({str(valueToPull) : "0"})
            #data = {str(valueToPull) : "0"}
            #database.set(data)
            print('Socket has disconnected! ', addr)

            receiveClientSocket.close()
            sendDataSocket.close()
            dataSocket.close()
            break

# next create a socket object
recvData = socket.socket()
sendData = socket.socket()
status = socket.socket()
print ("Sockets successfully created")

# reserve a port on your computer in our
# case it is 12345 but it can be anything
recvPort = 12350
sendPort = 12351
statusPort = 12352

# Next bind to the port
# we have not typed any ip in the ip field
# instead we have inputted an empty string
# this makes the server listen to requests
# coming from other computers on the network
recvData.bind(('', recvPort))
print ("data socket binded to %s" %(recvPort))

sendData.bind(('', sendPort))
print ("data socket binded to %s" %(sendPort))

status.bind(('',statusPort))
print ("status socket binded to %s" %(statusPort))

# put the socket into listening mode
recvData.listen(5)
print ("recv data socket is listening")
sendData.listen(5)
print ("send data socket is listening")
status.listen(5)
print ("status socket is listening")
# a forever loop until we interrupt it or
# an error occurs
while True:
    recv_data_accept, recv_data_addr = recvData.accept()
    send_data_accept, send_data_addr = sendData.accept()
    status_accept, status_addr = status.accept()
    _thread.start_new_thread(clientCloseCheck, (status_accept,status_addr,recv_data_accept, send_data_accept))
    _thread.start_new_thread(receiveClient,(recv_data_accept,recv_data_addr,status_accept, send_data_accept))
    # Close the connection with the client
    #c.close()
