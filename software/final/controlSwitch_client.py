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
tokenFileName = "/home/pi/Desktop/Senior-Design-Project/software/Token/token.txt"
keyFileName = "/home/pi/Desktop/Senior-Design-Project/software/Token/tokenFileKey.key"
key = ""

interval = 5  #default of 5 seconds

#used for firebase handler
firstHandlerEntryFromApp = 0
firstHandlerEntryPulse = 0
token = ""
deviceName = "ControlSwitch"
# bleName = "AT+NAMESWITCH_IoT"

#setup the bluetooth config.. this does not include timeout
# serialPort = serial.Serial("/dev/serial0", baudrate=9600)

appValues = {}

line = []
fullString = ""


grabToken = ""
token = ""



#Light Sequence GLOBALS
runReadSeq = False
modifyLocations = False
restartWIFI = False
allOff = False
allOn = False
bleInit = False
greenOn = False
redOn = False


#####
# Using cryptography, we are under the impression that if a tokenFileKey.key file
# has been made, that the contents are assumed to be encrypted within token.txt.
# This means that the program should not encrypt the data within token.txt
# if tokenFileKey.key has been created.

# If the user uses bluetooth to change the wifi ssid password, the token.txt 
# file needs to be encrypted after modification.


# Encrypt the entire file contents
def encryptFile(fileName):
    global key
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
def decryptFileContents(fileName):
    global key

    decrypted = ""

    # Decrypting the file
    while True:
        try:
            # Using the key
            fernet = Fernet(key)

            # Opening the encrypted file
            with open(fileName, 'rb') as enc_file:
                encrypted = enc_file.read()

            decrypted = fernet.decrypt(encrypted)
            break
        except:
            print("in except")
            bluetoothMAIN(True)

    return decrypted


def encryptInitialization():
    global tokenFileName, keyFileName, tokenFileKey, key

    if (os.path.exists(tokenFileName)):
        print("token file exists")
        # If the keyFileName exists it is assumed
        # that the token.txt file has been already
        # incrypted or will be encrypted later
        if os.path.exists(keyFileName):
            print("key file exists")
            # Opening the key
            # 'rb' means to open and read binary
            with open(keyFileName, 'rb') as tokenFileKey:
                key = tokenFileKey.read()

        else:
            print("key file does not exist")
            # Generates the key
            key = Fernet.generate_key()


            # String the key in a file
            # 'wb' means to open and write binary
            with open(keyFileName, 'wb') as tokenFileKey:
                tokenFileKey.write(key)

            encryptFile(tokenFileName)

    else:
        if os.path.exists(keyFileName):
            os.remove(keyFileName)
        
        # If the token.txt file does not exist, the 
        # user will need to use bluetooth to connect
        # to the pi and send the token aka. turn on
        # bluetooth and wait for user input
        print("token.txt does not exist")


# _thread.start_new_thread(lightSequence,(1,1)) #start thread for BLE init

#BLE Init
BLEReceived = True
stopBLEThread = False


# bleInit = True
# serialPort.write(("AT").encode())
# time.sleep(2)
# serialPort.write(("AT+IMME1").encode())
# time.sleep(2)
# serialPort.write((bleName).encode())
# time.sleep(2)
# serialPort.write(("AT+IMME0").encode())
# time.sleep(2)
# serialPort.write(("AT+RESET").encode())
# time.sleep(2)


print("BLE Initialization Complete")
bleInit = False

#END BLE Init    



def runReadSequence():
    global runReadSeq, modifyLocations, restartWIFI, allOff, allOn, bleInit, greenOn, redOn
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
    global runReadSeq, modifyLocations, restartWIFI, allOff, allOn, bleInit, greenOn, redOn
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

    global runReadSeq, modifyLocations, restartWIFI, allOff, allOn, bleInit, greenOn, redOn
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
    global runReadSeq, modifyLocations, restartWIFI, allOff, allOn, bleInit, greenOn, redOn, keyFileName
    runReadSeq = False
    modifyLocations = True
    restartWIFI = False
    allOff = False
    allOn = False
    bleInit = False
    greenOn = False
    redOn = False

    tokenConfig = open(tokenFileName, "r+")
    tokenConfig.seek(0)
    tokenConfig.truncate(0)
    tokenConfig.write(appValues.get("uid").strip())
    tokenConfig.close()

    if os.path.exists(keyFileName): #if key exists, remove it
        print("removed key file")
        os.remove(keyFileName)


def bluetoothMAIN(forToken):
    global runReadSeq, modifyLocations, restartWIFI, allOff, allOn, bleInit, greenOn, redOn
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
    status = "Not connected"

    if not forToken:
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

            encryptInitialization()

            status = "Connected" 
        except requests.ConnectionError:
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

        # ble.value = True  #BLE ON/OFF
        # runReadSequence()
        # modifyWPAFile()
        # modifyTOKENFile()
        # RestartWifi()
        encryptInitialization()
        print("in not connected loop")
        bluetoothMAIN(False)


bluetoothMAIN(False) 



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
        print("dataReceivedFromDatabase: " + str(dataReceivedFromDatabase))
        interval = int(dataReceivedFromDatabase)
        print(interval)
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
            database.child((decryptFileContents(tokenFileName)).decode("utf-8") + "/Status").update({str(deviceName) : str(1)}) #update Status to "1"
        else:
            print("Pulse = 0")
            


#function to send data to the server in a sequence
def sendingToDatabase(data):
    #send the data to the database
    database.child((decryptFileContents(tokenFileName)).decode("utf-8") + "/dataFromChild").update({str(deviceName) : str(data)})


while True:
    try:
        # Find the firebase token, and verify it is correct
        if(os.path.exists(tokenFileName)): #check if the token txt file exists
            print("Token file exists")
            users = database.child((decryptFileContents(tokenFileName)).decode("utf-8") + "/").get()
            if users.val() == None:
                print("Invalid token")
                raise
                # JUMP TO BLUETOOTH INIT HERE
            else:
                print("Token exists")   #token exists in db
                #update the database to display connected sensor
                database.child((decryptFileContents(tokenFileName)).decode("utf-8") + "/Connections").update({str(deviceName) : "1"})
        break
    except:
        bluetoothMAIN(True)

#Initialize the sending interval
database.child((decryptFileContents(tokenFileName)).decode("utf-8") + "/dataFromApp").update({str(deviceName) : str(interval)})

#initialize the firebase listener and pulse listener
myStream = database.child((decryptFileContents(tokenFileName)).decode("utf-8") + "/dataFromApp/" + deviceName).stream(firebaseStreamHandler, None)
myPulse = database.child((decryptFileContents(tokenFileName)).decode("utf-8") + "/Pulse/Pulse").stream(firebasePulseHandler, None)


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
    database.child((decryptFileContents(tokenFileName, key)).decode("utf-8") + "/Connections").update({str(deviceName) : "0"})
    print("connection closed")
#END CODE FOR OPERATIONS#
#delay
time.sleep(2)
