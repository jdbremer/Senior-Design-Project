
# first of all import the socket library
import socket
import _thread
import select
import time
import threading
#pyrebase is needed for the firebase connections
import pyrebase
import time
import os.path
from os import path
#for bluetooth com
import serial
#for internet check
import urllib
import requests
#for GPIO control
import RPi.GPIO as GPIO #pulls in the GPIO pin numbers





#setup the GPIO for the bluetooth module power
#set the GPIO to the board layout (used for pin numbers)
#GPIO.setmode(GPIO.BOARD)
#set the GPIO pin 18 to output
#GPIO.setup(18, GPIO.OUT)
#default the output to LOW
#GPIO.output(18, GPIO.LOW)

#setup the bluetooth config.. this does not include timeout
serialPort = serial.Serial("/dev/serial0", baudrate=115200)

appValues = {}

line = []
fullString = ""


grabToken = ""
token = ""


def runReadSequence():
    global line
    global fullString
    startMsg = "start"
    endMsg = "stop"
    ssid = "ssid"
    ssid_pswd = "password"
    uid = "uid"
    dataStart = False
    dataStop = False
    while True:
        for c in serialPort.read().decode():
            line.append(c)
            if c == '\n':
                print("newString")
                fullString = ''.join(line).replace("\n"," ")
                print(fullString)
                line = []

                if startMsg in fullString:
                    dataStart = True
                    dataStop = False
                    print("start")
                elif endMsg in fullString:
                    dataStart = False
                    dataStop = True
                    print("end")
                    return
                elif dataStart == True and dataStop == False:
                    if ssid in fullString or ssid_pswd in fullString or uid in fullString:
                        try:
                            valName = fullString.split(':')[0]
                            print(valName)
                            valData = fullString.split(':')[1]
                            print(valData)
                            appValues[valName] = valData
                        except:
                            print("invalid data")
                            return



#restarts the wifi services
def RestartWifi():
  os.system('sudo systemctl daemon-reload')
  time.sleep(1)
  os.system('sudo systemctl restart dhcpcd')
  time.sleep(10)


def modifyWPAFile():
    wifiConfig = open("/etc/wpa_supplicant/wpa_supplicant.conf", "r+")
    fileContents = wifiConfig.readlines()
    newFileContents = ""
    currentFileContents = ""
    for line in fileContents:
        newLine = line
        if "ssid" in line:
            newLine = "        ssid=" + '"'  + appValues.get("ssid").strip() + '"' + "\n"
            print(newLine)
        elif "psk" in line:
            newLine = "        psk=" + '"' + appValues.get("password").strip() + '"' + "\n"
            print(newLine)
        newFileContents += newLine
    wifiConfig.seek(0)
    wifiConfig.truncate(0)
    wifiConfig.write(newFileContents)
    wifiConfig.close()
    
    
    
def modifyTOKENFile():
    tokenConfig = open("/home/pi/Desktop/Senior-Design-Project/software/Server/token.txt", "r+")
    tokenConfig.seek(0)
    tokenConfig.truncate(0)
    tokenConfig.write(appValues.get("uid").strip())
    tokenConfig.close()

    
    
internet = True
#check if the pi is connected to the internet'
while internet:
    try:
        url = "https://www.google.com"
#        urllib.request.urlopen(url)
        response = requests.get(url)
        internet = False
#        GPIO.output(18, GPIO.LOW)
        print("Connected")
        break
    except requests.ConnectionError:
        print(response.status_code)
        status = "Not connected"
        
        
        
    print(status)
    if status == "Not connected":
        #turn on the bluetooth HAT
#        GPIO.output(18, GPIO.HIGH)
        #time.sleep(2)
        runReadSequence()
        modifyWPAFile()
        modifyTOKENFile()
        RestartWifi()


##DATABASE INIT##
#firebase database config
config = {
    "apiKey": "AIzaSyAcaqrqFZYmvcAb0qFCI9N4QiZ6L6OeuZ8",
    "authDomain": "seniordesign-ajr.firebaseapp.com",
    "databaseURL": "https://seniordesign-ajr-default-rtdb.firebaseio.com",
    "storageBucket": "seniordesign-ajr.appspot.com",
}

#initialize the pyrebase config instance 
firebase = pyrebase.initialize_app(config)

#instantiate both the storage and database firebase libraries
storage = firebase.storage()
database = firebase.database()
auth = firebase.auth()
##END DATABASE##

#thread lock initialization
lock = threading.Lock()

#global dictionary initialization ~ stores socket address and socket name
connections = {} 
#global dictionary initialization ~ stores socket name and socket object 
connectToSocketLib = {}

#used for firebase handler
firstHandlerEntry = 0



#the firebase handler will run this function to go through the sequence to send 
#the data from the app to the correct child node
def sendingClientFromFirebase(data, sensorName):
    if(sensorName in connectToSocketLib.keys()):
        sendSocket = connectToSocketLib.get(sensorName)
        sendSocket.send(data.encode('ascii'))
        receivedData = sendSocket.recv(1024).decode('ascii')
        print(receivedData)



#firebase listener "dataFromApp"
def firebaseStreamHandler(event):
    global token
    global firstHandlerEntry
    #if this is the first time in here, the data will be initialization data, which we want to discard
    if(firstHandlerEntry == 0):
        firstHandlerEntry = 1

    else:
        eventPathString = event["path"]
        if(len(eventPathString.split('/')) >= 2):
            #pulls out the sensor name from the event data which is delimited
            sensorName = eventPathString.split('/')[1]
            #pulls out the sensor data from the event data, this data is not delimited
            sensorData = eventPathString = event["data"]
            print("Received data from.. " + sensorName + " Sensor data.. ")
            print(sensorData)
            sendingClientFromFirebase(sensorData, sensorName)
            

       



#thread for the data received from the clients
def receiveClient(recvDataSocket, status_addr ,addr, statusSocket, sendDataSocket):
    global token
    #print the address of the new connecitons
    print ('Got connection from recv client.. ', addr )

    #client receiving initialization
    thankYouMsg = 'Server :: Thank you for connecting.. '
    recvDataSocket.send(thankYouMsg.encode('ascii'))
    print (recvDataSocket.recv(1024).decode('ascii'))
    checkMsg = 'Connection Successful..'
    recvDataSocket.send(checkMsg.encode('ascii'))
    #end of the client receiving initialization

    while True:
        try:
            #wait to receive client data
            fromClient = recvDataSocket.recv(1024).decode('ascii')
            
            #if there is no data, the connection has disconnected
            if(fromClient == ''):
                recvDataSocket.close()
                sendDataSocket.close()
                statusSocket.close()
                #break out of the while loop
                return

            #print the data that came from the client
            print(fromClient)
            
            #send the data to the database
            database.child(token + "/dataFromChild").update({str(connections.get(status_addr[1])) : str(fromClient)})

            #send a messsage to the client to keep sync
            checkMsg = 'I am here'
            recvDataSocket.send(checkMsg.encode('ascii'))

        #if the socket has an error, or is disconnected, come into this exception
        except socket.error:
            recvDataSocket.close()
            sendDataSocket.close()
            statusSocket.close()
            return
            


#status socket thread
def clientCloseCheck(statusSocket, addr, recvDataSocket, sendDataSocket):
    global token
    #lock the thread
    lock.acquire()
    #client initialization 
    statusSocket.send('connected....'.encode('ascii'))
    sensor = statusSocket.recv(1024).decode('ascii')
    
    print (sensor)
    
    #if a sensor already exists, increment it by 1
    if sensor in connectToSocketLib:
        sensor = sensor + "_1"
        i = 1
        while sensor in connectToSocketLib:
            i += 1
            sensor = sensor[:-1]
            sensor = sensor + i
        print("New Sensor Name: " + sensor)
    
    #print the address of the server/client status socket connection
    print (addr[1])
    #add the sensor name and address to the connections dictionary
    connections[addr[1]] = sensor
    connectToSocketLib[sensor] = sendDataSocket
    #release the thread lock
    lock.release()
    #update the database to display connected sensor
    database.child(token + "/Connections").update({str(sensor) : "1"})


    #keep trying to send data to the client (the client will never accept on purpose)
    while True:
        try:
            checkMsg = 'Are you there?'
            statusSocket.send(checkMsg.encode('ascii'))

        #when it trys to send the data, if it recognizes an error or disconnect, come into this exception
        except socket.error:
            #lock the thread
            lock.acquire()
            #find the sensor name using the socket address via the dictionary
            valueToPull = connections.get(addr[1])
            #delete it from the dictionary, showing that it is disconnected
            del connections[addr[1]]
            del connectToSocketLib[valueToPull]
            #release the thread
            lock.release()
            #update the connections database with the disconnected device
            database.child(token + "/Connections").update({str(valueToPull) : "0"})
            #update the data coming from child within the database to 0
            database.child(token + "/dataFromChild").update({str(valueToPull) : "0"})
            #display the disconnected sockets address
            print('Socket has disconnected! ', addr)
            #close all sockets (for saftey measures)
            recvDataSocket.close()
            sendDataSocket.close()
            statusSocket.close()
            break


# next create a socket object for receiving, sending and status
recvData = socket.socket()
sendData = socket.socket()
status = socket.socket()
print ("Sockets successfully created")

#ports that are reserved 
recvPort = 12350
sendPort = 12351
statusPort = 12352


#bind all of the port numbers to the sockets
recvData.bind(('', recvPort))
print ("data socket binded to %s" %(recvPort))

sendData.bind(('', sendPort))
print ("data socket binded to %s" %(sendPort))

status.bind(('',statusPort))
print ("status socket binded to %s" %(statusPort))

# put the sockets into listening mode
recvData.listen(5)
print ("recv data socket is listening")
sendData.listen(5)
print ("send data socket is listening")
status.listen(5)
print ("status socket is listening")

#while loop for firebase token
while True:
    if(path.exists("token.txt")): #check if the token txt file exists
        print("Token file exists")
        grabToken = open("token.txt")   #open token text file
        token = grabToken.read().replace("\n", " ") #assign token the string from text file without \n
        grabToken.close     #done grabbing the data from token text file
        users = database.child(token + "/").get()
        if users.val() == None:
            print("Invalid token")
            time.sleep(3)   #token not found in db, repeat loop after 3 seconds
        else:
            print("Token exists")   #token exists in db
            break                   #exit while true loop since the token exists
            
#initialize the firebase listener
myStream = database.child(token + "/dataFromApp").stream(firebaseStreamHandler, None)

#a forever loop until we interrupt it or an error occurs
while True:
    recv_data_accept, recv_data_addr = recvData.accept()
    send_data_accept, send_data_addr = sendData.accept()
    status_accept, status_addr = status.accept()
    _thread.start_new_thread(clientCloseCheck, (status_accept,status_addr,recv_data_accept, send_data_accept))
    _thread.start_new_thread(receiveClient,(recv_data_accept,status_addr, recv_data_addr,status_accept, send_data_accept))

