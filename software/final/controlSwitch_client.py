#AAAHHOpFl9ms1OltNglAAARWMQoPZbUGXEFE3PCYuwAAA

# Import socket module
import RPi.GPIO as GPIO #pulls in the GPIO pin numbers
import socket
import time
import _thread
import math
import os
import glob
import requests
from uuid import getnode as get_mac

#import nexmo

import pyrebase


# For cryptography operations
# Use 'pip install cryptography' if the library isn't found
from cryptography.fernet import Fernet

# import busio
# import digitalio
# import board
# import adafruit_mcp3xxx.mcp3008 as MCP
# from adafruit_mcp3xxx.analog_in import AnalogIn

# create the spi bus
# spi = busio.SPI(clock=board.SCK, MISO=board.MISO, MOSI=board.MOSI)

# create the cs (chip select)
# cs = digitalio.DigitalInOut(board.D5)

# create the mcp object
# mcp = MCP.MCP3008(spi, cs)

# create an analog input channel on pin 0
# chan = AnalogIn(mcp, MCP.P0)


mac = get_mac()
print("MAC address: " + str(mac))

#MODIFY
#Modify the path to these two within the Token folder...
# tokenFileName = "/home/pi/Desktop/Senior-Design-Project/software/Token/token.txt"
# keyFileName = "/home/pi/Desktop/Senior-Design-Project/software/Token/tokenFileKey.key"
key = "X4KVbWNlW8XhHE6b1NfcxCMUkGc2"

interval = 5  #default of 5 seconds

#used for firebase handler
firstHandlerEntryFromApp = 0
firstHandlerEntryPulse = 0
token = ""
deviceName = "ControlSwitch"
# bleName = "AT+NAMESWITCH_IoT"


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
print("reached end database")
##END DATABASE##

#firebase listener "dataFromApp"
def firebaseStreamHandler(event):
    global firstHandlerEntryFromApp
    global interval

    #if this is the first time in here, the data will be initialization data, which we want to discard
    if(firstHandlerEntryFromApp == 0):
        firstHandlerEntryFromApp = 1

    else:
        eventPathString = event["path"]
        #pulls out the sensor data from the event data, this data is not delimited
        dataReceivedFromDatabase = eventPathString = event["data"]
        #CODE TO DO SOMETHING WITH RECEIVED DATA
        try:
            print("Received data.. " + str(dataReceivedFromDatabase))
            relay1 = str(dataReceivedFromDatabase).split('~')[0]
            relay2 = str(dataReceivedFromDatabase).split('~')[1]
            if(int(relay1) == 0):
                GPIO.output(18, GPIO.LOW)
            elif(int(relay1) == 1):
                GPIO.output(18, GPIO.HIGH)
                
                
            if(int(relay2) == 0):
                GPIO.output(16, GPIO.LOW)
            elif(int(relay2) == 1):
                GPIO.output(16, GPIO.HIGH)
                
            sendingToDatabase(str(dataReceivedFromDatabase))
            # sendsSocket(sendingSocket, receivedData)
        
        except KeyboardInterrupt:
            print("keyboard interrupt")
        # print("dataReceivedFromDatabase: " + str(dataReceivedFromDatabase))
        # interval = int(dataReceivedFromDatabase)
        # print(interval)
        #END CODE TO DO SOMETHING WITH RECEIVED DATA

#firebase listener "Pulse" -> "Pulse"
def firebasePulseHandler(event):
    global firstHandlerEntryPulse
    #if this is the first time in here, the data will be initialization data, which we want to discard
    if(firstHandlerEntryPulse == 0):
        firstHandlerEntryPulse = 1
        print()

    else:
        eventPathString = event["path"]
        #pulls out the pulse value
        dataReceivedFromDatabase = eventPathString = event["data"]
        print(dataReceivedFromDatabase)
        print(type(dataReceivedFromDatabase))
        #CODE TO DO SOMETHING WITH RECEIVED DATA
        if int(dataReceivedFromDatabase) == 1:
            print("Pulse = 1")
            database.child(key + "/Status").update({str(deviceName) : str(1)}) #update Status to "1"
        else:
            print("Pulse = 0")
            


#function to send data to the server in a sequence
def sendingToDatabase(data):
    #send the data to the database
    database.child(key + "/dataFromChild").update({str(deviceName) : str(data)})

# database.child(key + "/Connections").update({str(deviceName) : "1"})

#Initialize the sending interval
database.child(key + "/dataFromApp").update({str(deviceName) : str(interval)})
print("updated connections and dataFromApp")
#initialize the firebase listener and pulse listener
myStream = database.child(key + "/dataFromApp/" + deviceName).stream(firebaseStreamHandler, None)
myPulse = database.child(key + "/Pulse/Pulse").stream(firebasePulseHandler, None)
print("initialized firebase listener and pulse listener")

#sensor code
#CODE FOR OPERATIONS#

try:
    #set the GPIO to the board layout (used for pin numbers)
    GPIO.setmode(GPIO.BOARD)
    #set the GPIO pin 18 to output
    GPIO.setup(18, GPIO.OUT)
    #default the output to LOW
    GPIO.output(18, GPIO.LOW)
    
    #set the GPIO pin 16 to output
    GPIO.setup(16, GPIO.OUT)
    #default the output to LOW
    GPIO.output(16, GPIO.LOW)
   
    #for testing...
    while True:
        continue
    
except KeyboardInterrupt:
    print("keyboard interrupt")

finally:
    print("clean up")
    GPIO.cleanup()
    #update the database to display connected sensor
    # database.child(key + "/Connections").update({str(deviceName) : "0"})
    print("connection closed")
#END CODE FOR OPERATIONS#
#delay
time.sleep(2)
