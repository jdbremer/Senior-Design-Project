const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();
// OnUpdates
// function name convention:
// childsensor/control
// everything is in camelCase

exports.alexaControlSwitch = functions.database
    .ref("{user}/alexa/control switch/data")
    .onWrite((change, context) => {
      // uses the ESLint styling, you can look it up
      // the max length of a line is 80
      // comments need a space after "//"
      // there can be no spaces in-between lines
      // there can not be spaces after any line
      // needs to be all tab or space
      const original = change.after.val();
      const user = context.params.user;
      const reftwo = change.after.ref.root
          .child(user + "/dataFromApp/ControlSwitch");
      if (original == "1") {
        return reftwo.set("1~1");
      } else if (original == "2") {
        return NULL;  
      } else {
        return reftwo.set("0~0");
      }
    });

// onUpdate setting default values

exports.defaultValues = functions.database
    .ref("{user}/Connections/{connection}")
    .onUpdate((change, context) => {
      const data = change.after.val();
      const sensorControl = context.params.connection;
      const user = context.params.user;
      const refDataFromApp = change.after.ref.root
          .child(user + "/dataFromApp/" + sensorControl);
      const refDataFromChild = change.after.ref.root
          .child(user + "/dataFromChild/" + sensorControl);
      const refInternalAppData = change.after.ref.root
          .child(user + "/internalAppData/thresholds/" + sensorControl);
      if (data == "0") {
        if (sensorControl == "ControlSwitch") {
          refDataFromApp.set("0~0");
        } else if (sensorControl == "TempSensor") {
          refDataFromChild.set("0.0~0.0");
        } else {
          refDataFromApp.set("0");
          refDataFromChild.set("0");
        }
        return refInternalAppData.set("0");
      }
    });

// onCreate adds the new connection to all parts of database

exports.addConnectionToDB = functions.database
    .ref("{user}/Connections/{connection}")
    .onCreate((snapshot, context) => {
      const sensorControl = context.params.connection;
      const user = context.params.user;
      const refDataFromApp = snapshot.ref.root
          .child(user + "/dataFromApp/" + sensorControl);
      const refDataFromChild = snapshot.ref.root
          .child(user + "/dataFromChild/" + sensorControl);
      const refInternalAppData = snapshot.ref.root
          .child(user + "/internalAppData/thresholds/" + sensorControl);
      if (sensorControl == "ControlSwitch") {
        refDataFromApp.set("0~0");
      } else if (sensorControl == "TempSensor") {
        refDataFromChild.set("0.0~0.0");
      } else {
        refDataFromApp.set("0");
      }
      refDataFromChild.set("0");
      return refInternalAppData.set("0");
    });

// onDelete deletes all of the data associated with that connection

exports.deleteConnectionToDB = functions.database
    .ref("{user}/Connections/{connection}")
    .onDelete((snapshot, context) => {
      const sensorControl = context.params.connection;
      const user = context.params.user;
      const refDataFromApp = snapshot.ref.root
          .child(user + "/dataFromApp/" + sensorControl);
      const refDataFromChild = snapshot.ref.root
          .child(user + "/dataFromChild/" + sensorControl);
      const refInternalAppData = snapshot.ref.root
          .child(user + "/internalAppData/thresholds/" + sensorControl);
      refDataFromApp.remove();
      refDataFromChild.remove();
      return refInternalAppData.remove();
    });

// New User Created

exports.newUserSetup = functions.auth.user()
    .onCreate((user) => {
      const userId = user.uid;
      const internal = "internalAppData";
      const body = {
        Connections: "",
        alexa: "",
        dataFromApp: "",
        dataFromChild: "",
      };
      admin.database().ref("/" + userId + "/").set(body);
      return admin.database()
          .ref("/" + userId + "/" + internal + "/thresholds/").set("");
    });

// ControlSwitch/Alexa

exports.defaultValues = functions.database
    .ref("{user}/dataFromChild/{connection}")
    .onUpdate((change, context) => {
      const data = change.after.val();
      const sensorControl = context.params.connection;
      const user = context.params.user;
      const refAlexa = change.after.ref.root
          .child(user + "/alexa/control switch/data");
      if (sensorControl == "ControlSwitch") {
        if (data == "1~1") {
          return refAlexa.set("1");
        } else if (data == "0~1" || data == "1~0") {
          return refAlexa.set("2");
        } else {
          return refAlexa.set("0");
        }
      }
    });
