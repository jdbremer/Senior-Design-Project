import serial
#import OS
import time
import _thread

serialPort = serial.Serial("/dev/serial0", baudrate=9600)


def checkUart(here,here2):
    while True:
        for c in serialPort.read().decode():
            print(c)



_thread.start_new_thread(checkUart ,(1,1)) 

while True:
    user = input("type stuff:")
    user = user.replace("\n", " ")
    print(user)
    serialPort.write((user).encode())
    # for c in serialPort.read().decode():
        # line.append(c)
        # print(line)
#        if c == '\n':
#            print("Line: " + ''.join(line))
#            line = []

#    serialPort.write(('AT+CMGS="9495353464"'+'\r\n').encode())
    #time.sleep(1)
