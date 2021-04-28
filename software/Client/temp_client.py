import os
import glob
import time
import RPi.GPIO as GPIO #pulls in the GPIO pin numbers
import socket
import _thread
import random

interval = 5

os.system('modprobe w1-gpio')
os.system('modprobe w1-therm')

base_dir = '/sys/bus/w1/devices/'
device_folder = glob.glob(base_dir + '28*')[0]
device_file = device_folder + '/w1_slave'

#function to send data to the server in a sequence
def sendingSocket(sendingSocket, data):
       #send the data to the server
       sendingSocket.send(str(data).encode('ascii'))
       #received message from server to keep in sync
       msgFromServer = sendingSocket.recv(1024).decode('ascii')


#thread that initiates when the status socket gets initiated
def statusSocket(serverSocket,receiveSocket, sendingSocket):
    print (serverSocket.recv(1024).decode('ascii'))
    serverSocket.send('TempSensor'.encode('ascii'))
    
    
#thread to handle the data that is received from the base node
def receivingSocket(serverSocket,receiveSocket, sendingSocket):
    while True:
        #data that comes from the base node will end up in receivedDAta
        receivedData = receiveSocket.recv(1024).decode('ascii')
        print (receivedData)
        #need to send data back to keep sync
        receiveSocket.send('Received...'.encode('ascii'))
        global interval
        #CODE TO DO SOMETHING WITH RECEIVED DATA
        interval = int(receivedData)

        #END CODE TO DO SOMETHING WITH RECEIVED DATA

#read the temperature
def read_temp_raw():
    f = open(device_file, 'r')
    lines = f.readlines()
    f.close()
    return lines

def read_temp(sendSocket):
    lines = read_temp_raw()
    while lines[0].strip()[-3:] != 'YES':
        time.sleep(0.2)
        lines = read_temp_raw()
    equals_pos = lines[1].find('t=')
    if equals_pos != -1:
        temp_string = lines[1][equals_pos+2:]
        rand = random.randint(-10,10)
        rand = rand*.01

        temp_c = (float(temp_string) / 1000.000)+rand
        temp_f = round((temp_c * 9.000 / 5.000 + 32.000),2)  #temp in F
        temp_c = round(float(temp_string) / 1000.000,2)    #temp in C
        
#        rand = round(random.uniform(-.10,.10),2)
#        rand = rand*.01
 #       temp_c = round(rand + temp_c,2)
 #       temp_f = round(rand + temp_f,2)
	
        sendingSocket(sendSocket, (str(temp_c) + '~' + str(temp_f)))  #return the temp in the form: #degrees C~#degrees F
        return str(temp_c) + '~' + str(temp_f)  #return the temp in the form: #degrees C~#degrees F

#create a socket object for the receiving, sending, and status sockets
receiving = socket.socket()
sending = socket.socket()
status = socket.socket()

#defining of the ports for each of the sockets
sendPort = 12350
recvPort = 12351
statusPort = 12352

#connect the IP and the port # to the sockets
# sending.connect(('192.168.86.31', sendPort))
# receiving.connect(('192.168.86.31', recvPort))
# status.connect(('192.168.86.31', statusPort))
sending.connect(('172.20.10.11', sendPort))
receiving.connect(('172.20.10.11', recvPort))
status.connect(('172.20.10.11', statusPort))

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

while True:
	print(read_temp(sending)) #read the temperature
	time.sleep(interval)  #delay between temperature readings


#delay before closing connections
time.sleep(2)

# close the connections
sending.close()
receiving.close()
status.close()
