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


# For cryptography operations
# Use 'pip install cryptography' if the library isn't found
from cryptography.fernet import Fernet





tokenFileName = "token.txt"
keyFileName = "tokenFileKey.key"
key = ""


interval = 5  #default of 5 seconds
average = 0
average_lux = 0



#used for firebase handler
firstHandlerEntry = 0
token = ""
deviceName = "LightSensor"




#####
# Using cryptography, we are under the impression that if a tokenFileKey.key file
# has been made, that the contents are assumed to be encrypted within token.txt.
# This means that the program should not encrypt the data within token.txt
# if tokenFileKey.key has been created.

# If the user uses bluetooth to change the wifi ssid password, the token.txt 
# file needs to be encrypted after modification.


# Encrypt the entire file contents
def encryptFile(fileName, key):
    # Using the generated key
    fernet = Fernet(key)

    # Opening the original file to encrypt
    with open(fileName, 'rb') as file:
        original = file.read()
        
    # Encrypting the file
    encrypted = fernet.encrypt(original)

    # Epening the file in write mode and
    # Writing the encrypted data
    with open(fileName, 'wb') as encrypted_file:
        encrypted_file.write(encrypted)

# For decrypting file contents, not the file itself
def decryptFileContents(fileName, key):
    # Using the key
    fernet = Fernet(key)

    # Opening the encrypted file
    with open(fileName, 'rb') as enc_file:
        encrypted = enc_file.read()

    # Decrypting the file
    decrypted = fernet.decrypt(encrypted)

    return decrypted
    # For debug
    # print(decrypted)



if (os.path.exists(tokenFileName)):
    # If the keyFileName exists it is assumed
    # that the token.txt file has been already
    # incrypted or will be encrypted later
    if os.path.exists(keyFileName):
        # Opening the key
        # 'rb' means to open and read binary
        with open(keyFileName, 'rb') as tokenFileKey:
            key = tokenFileKey.read()

    else:
        # Generates the key
        key = Fernet.generate_key()

        # String the key in a file
        # 'wb' means to open and write binary
        with open(keyFileName, 'wb') as tokenFileKey:
            tokenFileKey.write(key)

        encryptFile(tokenFileName, key)

else:
    if os.path.exists(keyFileName):
        os.remove(keyFileName)
    
    # If the token.txt file does not exist, the 
    # user will need to use bluetooth to connect
    # to the pi and send the token aka. turn on
    # bluetooth and wait for user input
    print("token.txt does not exist")

    # JUMP TO BLUETOOTH INIT HERE


# Used to decrypt the file, we only want to decrypt the contents

# Opening the file in write mode and
# Writing the decrypted data
# with open('token.txt', 'wb') as dec_file:
# 	dec_file.write(decrypted)



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
            


#function to send data to the server in a sequence
def sendingToDatabase(data):
    #send the data to the database
    database.child((decryptFileContents(tokenFileName, key)).decode("utf-8") + "/dataFromChild").update({str(deviceName) : str(data)})

    
        
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





# Find the firebase token, and verify it is correct
if(os.path.exists(tokenFileName)): #check if the token txt file exists
    print("Token file exists")
    # grabToken = open("token.txt")   #open token text file
    # token = grabToken.read().replace("\n", " ") #assign token the string from text file without \n
    # grabToken.close     #done grabbing the data from token text file
    users = database.child((decryptFileContents(tokenFileName, key)).decode("utf-8") + "/").get()
    if users.val() == None:
        print("Invalid token")
        # JUMP TO BLUETOOTH INIT HERE
    else:
        print("Token exists")   #token exists in db
        #update the database to display connected sensor
        database.child((decryptFileContents(tokenFileName, key)).decode("utf-8") + "/Connections").update({str(deviceName) : "1"})

# else:
    # JUMP TO BLUETOOTH INIT HERE


#initialize the firebase listener
myStream = database.child((decryptFileContents(tokenFileName, key)).decode("utf-8") + "/dataFromApp/" + deviceName).stream(firebaseStreamHandler, None)



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
    database.child((decryptFileContents(tokenFileName, key)).decode("utf-8") + "/Connections").update({str(deviceName) : "0"})


#delay before closing connections
time.sleep(2)
