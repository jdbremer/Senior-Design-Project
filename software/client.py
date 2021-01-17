# Import socket module
import socket
import time

# Create a socket object
s = socket.socket()

# Define the port on which you want to connect
port = 12346

# connect to the server on local computer
s.connect(('192.168.1.28', port))

# receive data from the server
print (s.recv(1024) )

s.send('Thanks for connecting')
time.sleep(2)
s.send('1234')
time.sleep(2)
s.send('1235')
time.sleep(2)
s.send('1236')
time.sleep(2)
s.send('1237')
time.sleep(2)
s.send('1238')
time.sleep(2)
s.send('1239')

time.sleep(2)

# close the connection
s.close()
