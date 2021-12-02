#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

# Import socket module
import RPi.GPIO as GPIO #pulls in the GPIO pin numbers
import socket
import time
import _thread
import math
import os
import serial
import requests
from uuid import getnode as get_mac

# import Adafruit_GPIO.SPI as SPI #ADC SPI library
# import Adafruit_MCP3008

import busio
import digitalio
import board
import adafruit_mcp3xxx.mcp3008 as MCP
from adafruit_mcp3xxx.analog_in import AnalogIn

# create the spi bus
spi = busio.SPI(clock=board.SCK, MISO=board.MISO, MOSI=board.MOSI)

# create the cs (chip select)
cs = digitalio.DigitalInOut(board.D5)

# create the mcp object
mcp = MCP.MCP3008(spi, cs)

# create an analog input channel on pin 0
chan = AnalogIn(mcp, MCP.P0)

import nexmo

import pyrebase


# For cryptography operations
# Use 'pip install cryptography' if the library isn't found
from cryptography.fernet import Fernet


mac = get_mac()
print("MAC address: " + str(mac))

#MODIFY
#Modify the path to these two within the Token folder...
tokenFileName = "/home/pi/Desktop/seniorDesign/Senior-Design-Project/software/Token/token.txt"
keyFileName = "/home/pi/Desktop/seniorDesign/Senior-Design-Project/software/Token/tokenFileKey.key"
key = ""


interval = 5  #default of 5 seconds
average = 0
average_lux = 0



#used for firebase handler
firstHandlerEntry = 0
token = ""
deviceName = "LightSensor"




#setup the bluetooth config.. this does not include timeout
serialPort = serial.Serial("/dev/serial0", baudrate=9600)

appValues = {}

line = []
fullString = ""


grabToken = ""
token = ""

#GPIO Setup

greenLed = digitalio.DigitalInOut(board.D36)
redLed = digitalio.DigitalInOut(board.D38)
ble = digitalio.DigitalInOut(board.D40)

greenLed.direction = digitalio.direction.output
redLed.direction = digitalio.direction.output
ble.direction = digitalio.direction.output


# GPIO.setmode(GPIO.BOARD)
# GPIO.setup(36, GPIO.OUT) #GREEN LED
# GPIO.setup(38, GPIO.OUT) #RED LED
# GPIO.setup(40, GPIO.OUT) #BLE ON/OFF

ble.value = False  #BLE ON/OFF
time.sleep(2) # We want the bluetooth module to go off initially as a reset

#GPIO defaults
greenLed.value = True #GREEN LED
redLed.value = True #RED LED
ble.value = True  #BLE ON/OFF


#Light Sequence GLOBALS
runReadSeq = False
modifyLocations = False
restartWIFI = False
allOff = False
allOn = False
bleInit = False
greenOn = False
redOn = False

def lightSequence(n,a):
    global runReadSeq
    global modifyLocations
    global restartWIFI
    global allOff
    global allOn
    global bleInit
    global greenOn
    global redOn
    while True:
        if runReadSeq:
            ble.value = True  #BLE ON/OFF
            greenLed.value = False #GREEN LED
            redLed.value = True #RED LED
            time.sleep(.2)
            greenLed.value = True #GREEN LED
            redLed.value = False #RED LED
            time.sleep(.2)
        elif modifyLocations:
            ble.value = True  #BLE ON/OFF
            greenLed.value = True #GREEN LED
            redLed.value = True #RED LED
            time.sleep(.2)
            greenLed.value = False #GREEN LED
            redLed.value = False #RED LED
            time.sleep(.2)
        elif restartWIFI:
            ble.value = True  #BLE ON/OFF
            greenLed.value = False #GREEN LED
            redLed.value = True #RED LED
            time.sleep(.2)
            greenLed.value = False #GREEN LED
            redLed.value = False #RED LED
            time.sleep(.2)
            greenLed.value = False #GREEN LED
            redLed.value = True #RED LED
            time.sleep(.2)
            greenLed.value = False #GREEN LED
            redLed.value = False #RED LED
            time.sleep(.2)
            greenLed.value = True #GREEN LED
            redLed.value = False #RED LED
            time.sleep(.2)
            greenLed.value = False #GREEN LED
            redLed.value = False #RED LED
            time.sleep(.2)
            greenLed.value = True #GREEN LED
            redLed.value = False #RED LED
            time.sleep(.2)
            greenLed.value = False #GREEN LED
            redLed.value = False #RED LED
            time.sleep(.2)
        elif allOn:
            ble.value = True  #BLE ON/OFF
            greenLed.value = True #GREEN LED
            redLed.value = True #RED LED
            time.sleep(.2)
        elif allOff:
            greenLed.value = False #GREEN LED
            redLed.value = False #RED LED
            time.sleep(.2)
        elif bleInit:
            ble.value = True  #BLE ON/OFF
            greenLed.value = True #GREEN LED
            redLed.value = True #RED LED
            time.sleep(.2)  
        elif greenOn:
            greenLed.value = True #GREEN LED
            redLed.value = False #RED LED
        elif redOn:
            greenLed.value = False #GREEN LED
            redLed.value = True #RED LED


_thread.start_new_thread(lightSequence,(1,1)) #start thread for BLE init

#BLE Init
BLEReceived = True
stopBLEThread = False



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




######
#BLE Init

# BLEReceived = True
# stopBLEThread = False


# def BLEModuleInit(fun,fun1):
#     line = []
#     BLEInit = 0
#     global BLEReceived
#     while stopBLEThread == False:
#         for c in serialPort.read().decode():
#             line.append(c)
#             linev2 = ''.join(line).replace("\n", " ").strip()
#             if linev2 == "OK+Set:1":
#                 print(linev2)
#                 line = []
#                 BLEReceived = True
#             elif linev2 == "OK+RESET":
#                 print(linev2)
#                 line = []
#                 BLEReceived = True
#             elif linev2 == "OK+Set:0":
#                 print(linev2)
#                 line = []
#                 BLEReceived = True
#             elif linev2 == "OK+RESET":
#                 print(linev2)
#                 line = []
#                 BLEReceived = True
#             elif linev2 == "OK+Set:SERVER_IoT":
#                 print(linev2)
#                 line = []
#                 BLEReceived = True
#             elif linev2 == "OK" and BLEInit == 0:
#                 BLEInit = 1
#                 print(linev2)
#                 line = []
#                 BLEReceived = True

# _thread.start_new_thread(BLEModuleInit,(1,1)) #start thread for BLE init


bleInit = True
# BLEReceived = False
serialPort.write(("AT").encode())
time.sleep(2)
# while BLEReceived == False: continue
# BLEReceived = False
serialPort.write(("AT+IMME1").encode())
time.sleep(2)
# while BLEReceived == False: continue
# BLEReceived = False
serialPort.write(("AT+NAMESERVER_IoT").encode())
time.sleep(2)
# while BLEReceived == False: continue
# BLEReceived = False
serialPort.write(("AT+IMME0").encode())
time.sleep(2)
# while BLEReceived == False: continue
# BLEReceived = False
serialPort.write(("AT+RESET").encode())
time.sleep(2)
# while BLEReceived == False: continue


# stopBLEThread = True
print("BLE Initialization Complete")
bleInit = False

#END BLE Init    



def runReadSequence():
    global runReadSeq
    global modifyLocations
    global restartWIFI
    global allOff
    global allOn
    global bleInit
    global greenOn
    global redOn
    runReadSeq = True
    modifyLocations = False
    restartWIFI = False
    allOff = False
    allOn = False
    bleInit = False
    greenOn = False
    redOn = False

    
    global line
    global fullString
    startMsg = "start"
    endMsg = "stop"
    ssid = "ssid"
    ssid_pswd = "password"
    uid = "uid"
    dataStart = False
    dataStop = False
    line = []
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
    global runReadSeq
    global modifyLocations
    global restartWIFI
    global allOff
    global allOn
    global bleInit
    global greenOn
    global redOn
    runReadSeq = False
    modifyLocations = False
    restartWIFI = True
    allOff = False
    allOn = False
    bleInit = False
    greenOn = False
    redOn = False

    os.system('sudo systemctl daemon-reload')
    time.sleep(5)
    os.system('sudo systemctl stop dhcpcd.service')
    time.sleep(5)
    os.system('sudo systemctl start dhcpcd.service')
    time.sleep(20)


def modifyWPAFile():
    global runReadSeq
    global modifyLocations
    global restartWIFI
    global allOff
    global allOn
    global bleInit
    global greenOn
    global redOn
    runReadSeq = False
    modifyLocations = True
    restartWIFI = False
    allOff = False
    allOn = False
    bleInit = False
    greenOn = False
    redOn = False

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
    global runReadSeq
    global modifyLocations
    global restartWIFI
    global allOff
    global allOn
    global bleInit
    global greenOn
    global redOn
    runReadSeq = False
    modifyLocations = True
    restartWIFI = False
    allOff = False
    allOn = False
    bleInit = False
    greenOn = False
    redOn = False

    tokenConfig = open("/home/pi/Desktop/Senior-Design-Project/software/Token/token.txt", "r+")
    tokenConfig.seek(0)
    tokenConfig.truncate(0)
    tokenConfig.write(appValues.get("uid").strip())
    tokenConfig.close()

######




def bluetoothMAIN():
    global runReadSeq
    global modifyLocations
    global restartWIFI
    global allOff
    global allOn
    global bleInit
    global greenOn
    global redOn
    runReadSeq = False
    modifyLocations = False
    restartWIFI = False
    allOff = False
    allOn = False
    bleInit = False
    greenOn = False
    redOn = False


    print("Bluetooth MAIN")
    internet = False
    status = ""
    #check if the pi is connected to the internet'
    #while internet == False:
    try:
        url = "https://www.google.com"
        #urllib.request.urlopen(url)
        response = requests.get(url)
        internet = True
        runReadSeq = False
        modifyLocations = False
        restartWIFI = False
        allOff = False
        allOn = False
        bleInit = False
        greenOn = True
        redOn = False
        ble.value = False  #BLE ON/OFF
        status = "Connected"
    except requests.ConnectionError:
        #print(response.status_code)
        status = "Not connected"
            
            
            
    print(status)
    if status == "Not connected":
        runReadSeq = False
        modifyLocations = False
        restartWIFI = False
        allOff = False
        allOn = False
        bleInit = False
        greenOn = False
        redOn = True
        ble.value = True  #BLE ON/OFF
        runReadSequence()
        modifyWPAFile()
        modifyTOKENFile()
        RestartWifi()
        bluetoothMAIN()


bluetoothMAIN()

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
            sensorTotal += chan.value #read adc value of channel 0
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
    print("connection closed")


#delay
time.sleep(2)
