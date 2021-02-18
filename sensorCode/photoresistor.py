import RPi.GPIO as GPIO

# Simple example of reading the MCP3008 analog input channels and printing
# them all out.
# Author: Tony DiCola
# License: Public Domain
import time

# Import SPI library (for hardware SPI) and MCP3008 library.
import Adafruit_GPIO.SPI as SPI
import Adafruit_MCP3008


# Software SPI configuration:
#CLK  = 18
#MISO = 23
#MOSI = 24
#CS   = 25
#mcp = Adafruit_MCP3008.MCP3008(clk=CLK, cs=CS, miso=MISO, mosi=MOSI)

# Hardware SPI configuration:
SPI_PORT   = 0
SPI_DEVICE = 0
mcp = Adafruit_MCP3008.MCP3008(spi=SPI.SpiDev(SPI_PORT, SPI_DEVICE))


GPIO.setmode(GPIO.BCM)
GPIO.setup(18, GPIO.OUT)



while(true)
    start = time.time()
    GPIO.output(18, GPIO.HIGH)

    while(true)
       if(mcp.read_adc(0) => 1000)
          end = time.time()
          GPIO.output(18, GPIO.LOW)
          break
       else 
          continue


    print(end-start)
