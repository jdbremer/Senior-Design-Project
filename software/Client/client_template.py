# Import socket module
import RPi.GPIO as GPIO #pulls in the GPIO pin numbers
import socket
import time
import _thread

import Adafruit_GPIO.SPI as SPI #ADC SPI library
import Adafruit_MCP3008

#function to send data to the server in a sequence
def sendingSocket(sendingSocket, data):
       #send the data to the server
       sendingSocket.send(str(data).encode('ascii'))
       #received message from server to keep in sync
       msgFromServer = sendingSocket.recv(1024).decode('ascii')


#thread that initiates when the status socket gets initiated
def statusSocket(serverSocket,receiveSocket, sendingSocket):
	print (serverSocket.recv(1024).decode('ascii'))
	serverSocket.send('LightSensor'.encode('ascii'))
    
    

#thread to handle the data that is received from the base node
def receivingSocket(serverSocket,receiveSocket, sendingSocket):
    while True:
        #data that comes from the base node will end up in receivedDAta
        receivedData = receiveSocket.recv(1024).decode('ascii')
        print (receivedData)
        #need to send data back to keep sync
        receiveSocket.send('Received...'.encode('ascii'))

        #CODE TO DO SOMETHING WITH RECEIVED DATA


        #END CODE TO DO SOMETHING WITH RECEIVED DATA



#create a socket object for the receiving, sending, and status sockets
receiving = socket.socket()
sending = socket.socket()
status = socket.socket()

#defining of the ports for each of the sockets
sendPort = 12350
recvPort = 12351
statusPort = 12352

#connect the IP and the port # to the sockets
sending.connect(('192.168.1.28', sendPort))
receiving.connect(('192.168.1.28', recvPort))
status.connect(('192.168.1.28', statusPort))

#after connection, start the new status socket thread to handle transmissions
_thread.start_new_thread(statusSocket,(status, receiving, sending))
_thread.start_new_thread(receivingSocket,(status, receiving, sending))




#the sending intialization sequence
#receive, send, then receive again before sending data
print (sending.recv(1024).decode('ascii') )
msg = 'Connection Successful..'
sending.send(msg.encode('ascii'))
print (sending.recv(1024).decode('ascii') )
#end of the send initialization sequence


#CODE FOR OPERATIONS#



#END CODE FOR OPERATIONS#






#delay before closing connections
time.sleep(2)

# close the connections
sending.close()
receiving.close()
status.close()
