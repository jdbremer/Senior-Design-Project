# Import socket module
import RPi.GPIO as GPIO #pulls in the GPIO pin numbers
import socket
import time
import _thread
import math
import os

import Adafruit_GPIO.SPI as SPI #ADC SPI library
import Adafruit_MCP3008

import nexmo

import pyrebase


#used for firebase handler
firstHandlerEntry = 0

token = ""

deviceName = "LightSensor"


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

#firebase listener "dataFromApp"
def firebaseStreamHandler(event):
    global token
    global firstHandlerEntry
    global interval

    #if this is the first time in here, the data will be initialization data, which we want to discard
    if(firstHandlerEntry == 0):
        firstHandlerEntry = 1

    else:
        eventPathString = event["path"]
        #pulls out the sensor data from the event data, this data is not delimited
        dataReceivedFromDatabase = eventPathString = event["data"]
        #CODE TO DO SOMETHING WITH RECEIVED DATA
        print("dataReceivedFromDatabase: " + str(dataReceivedFromDatabase))
        interval = int(dataReceivedFromDatabase)
        print(interval)
        #END CODE TO DO SOMETHING WITH RECEIVED DATA
            





interval = 5  #default of 5 seconds
average = 0
average_lux = 0


#function to send data to the server in a sequence
def sendingToDatabase(data):
    global token
    #send the data to the server
    #send the data to the database
    database.child(token + "/dataFromChild").update({str(deviceName) : str(data)})

    
    

        
def sendSampleThread(sendSocket,receive):
    global interval
    global average_lux
    while True:
        time.sleep(interval)
        print('average_lux: ' + str(average_lux))
        sendingToDatabase(average_lux)



#hardware SPI configuration
SPI_PORT   = 0
SPI_DEVICE = 0
#connects the SPI port and device to the variable
mcp = Adafruit_MCP3008.MCP3008(spi=SPI.SpiDev(SPI_PORT, SPI_DEVICE))

#set the GPIO to the board layout (used for pin numbers)
# GPIO.setmode(GPIO.BOARD)
#set the GPIO pin 18 to output
# GPIO.setup(18, GPIO.OUT)





#while loop for firebase token
while True:
    if(os.path.exists("token.txt")): #check if the token txt file exists
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
            #update the database to display connected sensor
            database.child(token + "/Connections").update({str(deviceName) : "1"})
            break                   #exit while true loop since the token exists

#initialize the firebase listener
myStream = database.child(token + "/dataFromApp/" + deviceName).stream(firebaseStreamHandler, None)



inc = 0
average = 0
average_lux
numberOfSamples = 500
sensorTotal = 0
adcValue = 0

#sensor code
try:
    while True:
        print("inside outter while loop")
        #grab the start time
        #start = time.time()
        #set the pin 18 to high
        # GPIO.output(18, GPIO.HIGH)

        
        sensorTotal = 0 #reset sensorTotal for next group of samples
        inc = 0
        
        sending = 0 #REMOVE
        receiving = 0 #REMOVE

        #start the thread to send the average lux on a user specified interval
        _thread.start_new_thread(sendSampleThread,(sending,receiving)) 
        while True:
            sensorTotal += mcp.read_adc(0) #read adc value of channel 0
            #take the average of the value
            #increment the incrementor
            inc = inc+1
            #if the incrementor is greater than the numberOfSamples, enough samples have been taken
            if(inc > numberOfSamples):
            
                #https://learn.adafruit.com/photocells/using-a-photocell
                
                #divide the sensor total by the total number of samples to get the average
                adcValue  = sensorTotal / numberOfSamples 
                #use the generated equation to determine the average lux
                average_lux = math.e**(((100*adcValue)-23529)/(11996))
                #round the average 2 decimal places
                average_lux = round(average_lux, 2)
                
                
                inc = 0
                sensorTotal = 0

           
except KeyboardInterrupt:
    print("keyboard interrupt")

finally:
    print("clean up")
    GPIO.cleanup()
    #update the database to display connected sensor
    database.child(token + "/Connections").update({str(deviceName) : "0"})


#delay before closing connections
time.sleep(2)
