# Import socket module
import socket
import time
import _thread

# Create a socket object
s = socket.socket()
#status = socket.socket()

# Define the port on which you want to connect
#port = 12346
statusPort = 12347

# connect to the server on local computer
s.connect(('192.168.1.28', port))
# connect to the server on local computer
#status.connect(('192.168.1.28', statusPort))
print(s.getsockname())
# receive data from the server
print (s.recv(1024).decode('ascii') )

msg = 'Thanks for connecting'
s.send(msg.encode('ascii'))
print (s.recv(1024).decode('ascii') )
time.sleep(.00001)
msg = '1234'
s.send(msg.encode('ascii'))
print (s.recv(1024).decode('ascii') )
time.sleep(.00001)
msg = '1235'
s.send(msg.encode('ascii'))
print (s.recv(1024).decode('ascii') )
time.sleep(.00001)
msg = '1236'
s.send(msg.encode('ascii'))
print (s.recv(1024).decode('ascii') )
time.sleep(.00001)
msg = '1237'
s.send(msg.encode('ascii'))
print (s.recv(1024).decode('ascii') )
time.sleep(2)
msg = '1238'
s.send(msg.encode('ascii'))
print (s.recv(1024).decode('ascii') )
time.sleep(2)
msg = '1239'
s.send(msg.encode('ascii'))
print (s.recv(1024).decode('ascii') )

time.sleep(2)

# close the connection
s.close()
