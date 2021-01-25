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

##g
#def stream_handler(post):
#	print(post)

#my_stream = database.child("testObj").child("Key2").stream(stream_handler, None)

#time.sleep(10)
database.child("testObj").update({"Key1": "Lets"})
database.child("testObj").update({"Key2": "Goooooooo"})

###Create a key/keys as well as a child###
#database.child("testObj")
#data = {"Key1": "Value1", "Key2": "Value2"}
#database.set(data)

###Update an existing key from within a child node###
#database.child("testObj").update({"Key1": "TEST"})

###Grab the key from within a child node###
#grab = database.child("testObj")
#data = grab.child("Key1").get().val()
#print (data)

#updated = firebase.database.Reference()
#print(updated)
