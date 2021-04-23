# Import socket module
import RPi.GPIO as GPIO #pulls in the GPIO pin numbers
import socket
import time
import _thread

import Adafruit_GPIO.SPI as SPI #ADC SPI library
import Adafruit_MCP3008



GPIO.setmode(GPIO.BOARD)
GPIO.setup(36, GPIO.OUT) #GREEN LED
GPIO.setup(38, GPIO.OUT) #RED LED
GPIO.setup(40, GPIO.OUT) #BLE ON/OFF

#GPIO defaults
GPIO.output(36, GPIO.HIGH) #GREEN LED
GPIO.output(38, GPIO.HIGH) #RED LED
GPIO.output(40, GPIO.HIGH)  #BLE ON/OFF

#setup the bluetooth config.. this does not include timeout
serialPort = serial.Serial("/dev/serial0", baudrate=9600)

appValues = {}

line = []
fullString = ""


grabToken = ""
token = ""




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

_thread.start_new_thread(BLEModuleInit,(1,1)) #start thread for BLE init

BLEReceived = False
serialPort.write(("AT").encode())
while BLEReceived == False: continue
BLEReceived = False
serialPort.write(("AT+IMME1").encode())
while BLEReceived == False: continue
BLEReceived = False
serialPort.write(("AT+NAMESERVER_IoT").encode())
while BLEReceived == False: continue
BLEReceived = False
serialPort.write(("AT+IMME0").encode())
while BLEReceived == False: continue
BLEReceived = False
serialPort.write(("AT+RESET").encode())
while BLEReceived == False: continue


stopBLEThread = True
print("BLE Initialization Complete")

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
        GPIO.output(36, GPIO.HIGH) #GREEN LED
        GPIO.output(38, GPIO.LOW) #RED LED
        GPIO.output(40, GPIO.LOW)  #BLE ON/OFF
        break
    except requests.ConnectionError:
#        print(response.status_code)
        status = "Not connected"
        
        
        
    print(status)
    if status == "Not connected":
        #turn on the bluetooth HAT
#        GPIO.output(18, GPIO.HIGH)
        #time.sleep(2)
        GPIO.output(36, GPIO.LOW) #GREEN LED
        GPIO.output(38, GPIO.HIGH) #RED LED
        GPIO.output(40, GPIO.HIGH)  #BLE ON/OFF


        runReadSequence()
        modifyWPAFile()
        modifyTOKENFile()
        RestartWifi()






#function to send data to the server in a sequence
def sendsSocket(sendingSocket, data):
       #send the data to the server
       sendingSocket.send(str(data).encode('ascii'))
       #received message from server to keep in sync
       msgFromServer = sendingSocket.recv(1024).decode('ascii')
       print(msgFromServer)

#thread that initiates when the status socket gets initiated
def statusSocket(serverSocket,receiveSocket, sendingSocket):
	print(serverSocket.recv(1024).decode('ascii'))
	serverSocket.send('BLE'.encode('ascii'))
    
    

def receivingSocket(serverSocket,receiveSocket, sendingSocket):
    while True:
        #data that comes from the base node will end up in receivedDAta
        receivedData = receiveSocket.recv(1024).decode('ascii')
        print(receivedData)
        #need to send data back to keep sync
        receiveSocket.send('Received...'.encode('ascii'))

        #CODE TO DO SOMETHING WITH RECEIVED DATA



        #END CODE TO DO SOMETHING WITH RECEIVED DATA
    






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




#the sending intialization sequence
#receive, send, then receive again before sending data
print (sending.recv(1024).decode('ascii') )
msg = 'Connection Successful..'
sending.send(msg.encode('ascii'))
print (sending.recv(1024).decode('ascii') )
#end of the send initialization sequence


#CODE FOR OPERATIONS#

try:

    while True:
        continue
    
except KeyboardInterrupt:
    print("keyboard interrupt")

finally:
    print("clean up")
    GPIO.cleanup()




#END CODE FOR OPERATIONS#






#delay before closing connections
time.sleep(2)

# close the connections
sending.close()
receiving.close()
status.close()
