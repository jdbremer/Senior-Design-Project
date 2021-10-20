# Import socket module
import RPi.GPIO as GPIO #pulls in the GPIO pin numbers
import socket
import time
import _thread
import math
import os
import glob
from uuid import getnode as get_mac
import pyrebase

#import Adafruit_GPIO.SPI as SPI #ADC SPI library
#import Adafruit_MCP3008

#import nexmo

#import pyrebase


# For cryptography operations
# Use 'pip install cryptography' if the library isn't found
from cryptography.fernet import Fernet


mac = get_mac()
print("MAC address: " + str(mac))


tokenFileName = "~/Desktop/Senior-Design-Project/software/Token/token.txt"
keyFileName = "~/Desktop/Senior-Design-Project/software/Token/tokenFileKey.key"
key = ""


interval = 5  #default of 5 seconds
average = 0
os.system('modprobe w1-gpio')
os.system('modprobe w1-therm')

base_dir = '/sys/bus/w1/devices/'
device_folder = glob.glob(base_dir + '28*')[0]
device_file = device_folder + '/w1_slave'



#used for firebase handler
firstHandlerEntry = 0
token = ""
deviceName = "TempSensor"




#setup the bluetooth config.. this does not include timeout
#serialPort = serial.Serial("/dev/serial0", baudrate=9600)

appValues = {}

line = []
fullString = ""


grabToken = ""
token = ""




#BLE Init
BLEReceived = True
stopBLEThread = False



GPIO.setmode(GPIO.BOARD)
GPIO.setup(36, GPIO.OUT) #GREEN LED
GPIO.setup(38, GPIO.OUT) #RED LED
GPIO.setup(40, GPIO.OUT) #BLE ON/OFF

#GPIO defaults
GPIO.output(36, GPIO.HIGH) #GREEN LED
GPIO.output(38, GPIO.HIGH) #RED LED
GPIO.output(40, GPIO.HIGH)  #BLE ON/OFF



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

BLEReceived = True
stopBLEThread = False


def BLEModuleInit(fun,fun1):
    line = []
    BLEInit = 0
    global BLEReceived
    while stopBLEThread == False:
        for c in serialPort.read().decode():
            line.append(c)
            linev2 = ''.join(line).replace("\n", " ").strip()
            if linev2 == "OK+Set:1":
                print(linev2)
                line = []
                BLEReceived = True
            elif linev2 == "OK+RESET":
                print(linev2)
                line = []
                BLEReceived = True
            elif linev2 == "OK+Set:0":
                print(linev2)
                line = []
                BLEReceived = True
            elif linev2 == "OK+RESET":
                print(linev2)
                line = []
                BLEReceived = True
            elif linev2 == "OK+Set:SERVER_IoT":
                print(linev2)
                line = []
                BLEReceived = True
            elif linev2 == "OK" and BLEInit == 0:
                BLEInit = 1
                print(linev2)
                line = []
                BLEReceived = True

# _thread.start_new_thread(BLEModuleInit,(1,1)) #start thread for BLE init

# BLEReceived = False
# serialPort.write(("AT").encode())
# while BLEReceived == False: continue
# BLEReceived = False
# serialPort.write(("AT+IMME1").encode())
# while BLEReceived == False: continue
# BLEReceived = False
# serialPort.write(("AT+NAMESERVER_IoT").encode())
# while BLEReceived == False: continue
# BLEReceived = False
# serialPort.write(("AT+IMME0").encode())
# while BLEReceived == False: continue
# BLEReceived = False
# serialPort.write(("AT+RESET").encode())
# while BLEReceived == False: continue


# stopBLEThread = True
# print("BLE Initialization Complete")

#END BLE Init





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
  os.system('sudo systemctl daemon-reload')
  time.sleep(5)
  os.system('sudo systemctl stop dhcpcd.service')
  time.sleep(5)
  os.system('sudo systemctl start dhcpcd.service')
  time.sleep(20)


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
    tokenConfig = open("/home/pi/Desktop/Senior-Design-Project/software/Token/token.txt", "r+")
    tokenConfig.seek(0)
    tokenConfig.truncate(0)
    tokenConfig.write(appValues.get("uid").strip())
    tokenConfig.close()

######




def bluetoothMAIN():
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
        #GPIO.output(18, GPIO.LOW)
        GPIO.output(36, GPIO.HIGH) #GREEN LED
        GPIO.output(38, GPIO.LOW) #RED LED
        GPIO.output(40, GPIO.LOW)  #BLE ON/OFF
        status = "Connected"
    except requests.ConnectionError:
        #print(response.status_code)
        status = "Not connected"
            
            
            
    print(status)
    if status == "Not connected":
        #turn on the bluetooth HAT
        #GPIO.output(18, GPIO.HIGH)
        #time.sleep(2)
        GPIO.output(36, GPIO.LOW) #GREEN LED
        GPIO.output(38, GPIO.HIGH) #RED LED
        GPIO.output(40, GPIO.HIGH)  #BLE ON/OFF
        runReadSequence()
        modifyWPAFile()
        modifyTOKENFile()
        RestartWifi()
        bluetoothMAIN()




# Used to decrypt the file, we only want to decrypt the contents

# Opening the file in write mode and
# Writing the decrypted data
# with open('token.txt', 'wb') as dec_file:
#   dec_file.write(decrypted)



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

    
        
# def sendSampleThread(sendSocket,receive):
#     global interval
#     global average_lux
#     while True:
#         time.sleep(interval)
#         print('average_lux: ' + str(average_lux))
#         sendingToDatabase(average_lux)


def read_temp_raw():
    f = open(device_file, 'r')
    lines = f.readlines()
    f.close()
    return lines

def read_temp():
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
    
        # sendingSocket(sendSocket, (str(temp_c) + '~' + str(temp_f)))  #return the temp in the form: #degrees C~#degrees F
        return str(temp_c) + ' C ~ ' + str(temp_f) + ' F'  #return the temp in the form: #degrees C~#degrees F


#hardware SPI configuration
SPI_PORT   = 0
SPI_DEVICE = 0
#connects the SPI port and device to the variable
#mcp = Adafruit_MCP3008.MCP3008(spi=SPI.SpiDev(SPI_PORT, SPI_DEVICE))

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

#read the temperature



inc = 0
average = 0
numberOfSamples = 500
sensorTotal = 0
adcValue = 0

#sensor code
try:
    while True:
        # print(read_temp(sendingToDatabase)) #read the temperature
        tempVal = read_temp()
        print(tempVal)
        sendingToDatabase(tempVal)
        time.sleep(interval)  #delay between temperature readings

           
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
