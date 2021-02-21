# Import socket module
import socket
import time
import _thread

def statusSocket(serverSocket,receiveSocket, sendingSocket):
	print (serverSocket.recv(1024).decode('ascii'))
	serverSocket.send('LightSensor'.encode('ascii'))
    
    

def receivingSocket(serverSocket,receiveSocket, sendingSocket):
	print (receiveSocket.recv(1024).decode('ascii'))



# Create a socket object
receiving = socket.socket()
sending = socket.socket()
status = socket.socket()

# Define the port on which you want to connect
sendPort = 12350
recvPort = 12351
statusPort = 12352

# connect to the server on local computer
receiving.connect(('192.168.1.28', sendPort))
sending.connect(('192.168.1.28', recvPort))
# connect to the server on local computer
status.connect(('192.168.1.28', statusPort))

_thread.start_new_thread(statusSocket,(status, receiving, sending))
#_thread.start_new_thread(receivingSocket,(status, receiving, sending))

print(sending.getsockname())

# receive data from the server
print (receiving.recv(1024).decode('ascii') )
print("here")
msg = 'Connection Successful..'
receiving.send(msg.encode('ascii'))
print (receiving.recv(1024).decode('ascii') )


# receive data from the server
# print (sending.recv(1024).decode('ascii') )

# msg = 'Thanks for connecting'
# sending.send(msg.encode('ascii'))
# print (sending.recv(1024).decode('ascii') )
# time.sleep(.00001)
# msg = '1234'
# sending.send(msg.encode('ascii'))
# print (sending.recv(1024).decode('ascii') )
# time.sleep(.00001)
# msg = '1235'
# sending.send(msg.encode('ascii'))
# print (sending.recv(1024).decode('ascii') )
# time.sleep(.00001)
# msg = '1236'
# sending.send(msg.encode('ascii'))
# print (sending.recv(1024).decode('ascii') )
# time.sleep(.00001)
# msg = '1237'
# sending.send(msg.encode('ascii'))
# print (sending.recv(1024).decode('ascii') )
# time.sleep(2)
# msg = '1238'
# sending.send(msg.encode('ascii'))
# print (sending.recv(1024).decode('ascii') )
# time.sleep(2)
# msg = '1239'
# sending.send(msg.encode('ascii'))
# print (sending.recv(1024).decode('ascii') )

time.sleep(2)

# close the connection
sending.close()
receiving.close()
status.close()
