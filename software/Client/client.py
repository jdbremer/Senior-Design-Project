# Import socket module
import RPi.GPIO as GPIO
import socket
import time
import _thread

import Adafruit_GPIO.SPI as SPI
import Adafruit_MCP3008


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
sending.connect(('192.168.1.28', sendPort))
receiving.connect(('192.168.1.28', recvPort))
# connect to the server on local computer
status.connect(('192.168.1.28', statusPort))

_thread.start_new_thread(statusSocket,(status, receiving, sending))
#_thread.start_new_thread(receivingSocket,(status, receiving, sending))


# Hardware SPI configuration:
SPI_PORT   = 0
SPI_DEVICE = 0
mcp = Adafruit_MCP3008.MCP3008(spi=SPI.SpiDev(SPI_PORT, SPI_DEVICE))


GPIO.setmode(GPIO.BOARD)
GPIO.setup(18, GPIO.OUT)





# client rece
print (sending.recv(1024).decode('ascii') )
print("here")
msg = 'Connection Successful..'
sending.send(msg.encode('ascii'))
print (sending.recv(1024).decode('ascii') )

# when sending data make sure to send then do a receive before doing another send


inc = 0
average = 0

while True:
    start = time.time()
    GPIO.output(18, GPIO.HIGH)

    while True:
       if(mcp.read_adc(0) >= 1000):
          end = time.time()
          GPIO.output(18, GPIO.LOW)
          break
       else: 
          continue

    time.sleep(.5)
    if(average == 0):
       average = end-start
    #print(end-start)
    average = (average+(end-start))/2
    inc = inc+1
    if(inc > 50):
       print(average)
       inc = 0
       sending.send(average.encode('ascii'))
       print (sending.recv(1024).decode('ascii'))





time.sleep(2)

# close the connections
sending.close()
receiving.close()
status.close()
