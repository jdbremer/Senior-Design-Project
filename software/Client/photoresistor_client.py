# Import socket module
import RPi.GPIO as GPIO #pulls in the GPIO pin numbers
import socket
import time
import _thread
import math

import Adafruit_GPIO.SPI as SPI #ADC SPI library
import Adafruit_MCP3008

import nexmo

# client = nexmo.Client(key='77d3ed4c', secret='SjSjkdsIgYw1AHce')

# client.send_message({
#     'from': '18553182827',
#     'to': '19522327269',
#     'text': 'Hello from Vonage SMS API',
# })


interval = 5  #default of 5 seconds
average = 0
average_lux = 0


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
        interval = int(receivedData)

        #END CODE TO DO SOMETHING WITH RECEIVED DATA
        
def sampleThread(sendSocket,receive):
    while True:
        time.sleep(interval)
        print('average: ' + average_lux)
        sendingSocket(sendSocket, average_lux)


#create a socket object for the receiving, sending, and status sockets
receiving = socket.socket()
sending = socket.socket()
status = socket.socket()

#defining of the ports for each of the sockets
sendPort = 12350
recvPort = 12351
statusPort = 12352

#connect the IP and the port # to the sockets
sending.connect(('192.168.86.31', sendPort))
receiving.connect(('192.168.86.31', recvPort))
status.connect(('192.168.86.31', statusPort))

#after connection, start the new status socket thread to handle transmissions
_thread.start_new_thread(statusSocket,(status, receiving, sending))
_thread.start_new_thread(receivingSocket,(status, receiving, sending))


#hardware SPI configuration
SPI_PORT   = 0
SPI_DEVICE = 0
#connects the SPI port and device to the variable
mcp = Adafruit_MCP3008.MCP3008(spi=SPI.SpiDev(SPI_PORT, SPI_DEVICE))

#set the GPIO to the board layout (used for pin numbers)
# GPIO.setmode(GPIO.BOARD)
#set the GPIO pin 18 to output
# GPIO.setup(18, GPIO.OUT)





#the sending intialization sequence
#receive, send, then receive again before sending data
print (sending.recv(1024).decode('ascii') )
msg = 'Connection Successful..'
sending.send(msg.encode('ascii'))
print (sending.recv(1024).decode('ascii') )
#end of the send initialization sequence


inc = 0
average = 0
average_lux
numberOfSamples = 500
sensorTotal = 0
temp = 0
Vcc = 5
R = 10000

#sensor code
while True:
    print("inside outter while loop")
    #grab the start time
    #start = time.time()
    #set the pin 18 to high
    # GPIO.output(18, GPIO.HIGH)

    
    sensorTotal = 0 #reset sensorTotal for next group of samples
    inc = 0
    
    _thread.start_new_thread(sampleThread,(sending,receiving))
    while True:
        sensorTotal += mcp.read_adc(0) #read adc value of channel 0
        #take the average of the value
        #increment the incrementor
        inc = inc+1
        #if the incrementor is greater than 50, enough samples have been taken
        if(inc > numberOfSamples):
            #print the average to serial
            # print("inside if statement")
            temp = sensorTotal / numberOfSamples
            average = (( Vcc * R )/( temp )) - R #https://learn.adafruit.com/photocells/using-a-photocell
            #average_lux = (math.sqrt((76.278**1000)/(average**1000)))**(1/827) #the table data was put into excel, it was plotted then a linear fit line and  
                                                                          #equation were created
                                                                          
            average_lux = (math.sqrt(((76.278)/(average))**1000))**(1/827)
            # print('average: ' + average)
            inc = 0
            sensorTotal = 0

           
# except KeyboardInterrupt:
#   print("keyboard interrupt")

# finally:
#   print("clean up")
#   GPIO.cleanup()



#delay before closing connections
time.sleep(2)

# close the connections
sending.close()
receiving.close()
status.close()
