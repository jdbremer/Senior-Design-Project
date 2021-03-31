import serial
import time
serialPort = serial.Serial("/dev/serial0", baudrate=115200)
line=[]
while True:
	for c in serialPort.read().decode():
		line.append(c)
		if c == '\n':
			print("Line: " + ''.join(line))
			line = []
			break
