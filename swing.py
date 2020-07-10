import dht
import time
import network
from umqtt import simple as mqtt
from machine import Pin
d=dht.DHT11(Pin(23))
wlan=network.WLAN(network.STA_IF)  
wlan.active(True)
wlan.connect('abc','abc123456780')
client=mqtt.MQTTClient('1740707','101.200.164.203',port='1883',user='zzx',password='981216')  #连接mqtt服务器
client.connect()
while True:
  time.sleep(3)
  d.measure()
  print(d.humidity())
  print(d.temperature())
  h=d.humidity()
  t=d.temperature()
  msg=str(h)
  msg1=str(t)
  client.publish('w',msg)


