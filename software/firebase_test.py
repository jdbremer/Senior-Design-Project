import pyrebase
import time

config = {
    "apiKey": "AIzaSyAcaqrqFZYmvcAb0qFCI9N4QiZ6L6OeuZ8",
    "authDomain": "seniordesign-ajr.firebaseapp.com",
    "databaseURL": "https://seniordesign-ajr-default-rtdb.firebaseio.com",
    "storageBucket": "seniordesign-ajr.appspot.com",
}

firebase = pyrebase.initialize_app(config)

storage = firebase.storage()
database = firebase.database()

##
def stream_handler_key1(post1):
	print(post1)

my_stream = database.child("testObj").child("Key1").stream(stream_handler, None)


def stream_handler_key2(post2):
	print(post2)

my_stream = database.child("testObj").child("Key2").stream(stream_handler, None)
