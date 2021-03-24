const functions = require("firebase-functions");

// OnUpdates
// function name convention:
// childsensor/control
// everything is in camelCase

exports.alexaControlSwitch = functions.database
    .ref("/alexa/control switch/data")
    .onWrite((change, context) => {
      // uses the ESLint styling, you can look it up
      // the max length of a line is 80
      // comments need a space after "//"
      // there can be no spaces in-between lines
      // there can not be spaces after any line
      // needs to be all tab or space
      const original = change.after.val();
      const reftwo = change.after.ref.root
          .child("/dataFromApp/ControlSwitch");
      if (original == "1") {
        return reftwo.set("1~1");
      } else {
        return reftwo.set("0~0");
      }
    });

// onUpdate setting default values

exports.defaultValues = functions.database.ref("/Connections/{connection}")
    .onUpdate((change, context) => {
      const data = change.after.val();
      const sensorControl = context.params.connection;
      const refDataFromApp = change.after.ref.root
          .child("/dataFromApp/" + sensorControl);
      const refDataFromChild = change.after.ref.root
          .child("/dataFromChild/" + sensorControl);
      const refInternalAppData = change.after.ref.root
          .child("/internalAppData/thresholds/" + sensorControl);
      if (data == "1") {
        if (sensorControl == "ControlSwitch") {
          refDataFromApp.set("0~0");
        } else {
          refDataFromApp.set("0");
        }
        refDataFromChild.set("0");
        return refInternalAppData.set("0");
      } else {
        if (sensorControl == "ControlSwitch") {
          refDataFromApp.set("0~0");
        } else {
          refDataFromApp.set("0");
        }
        refDataFromChild.set("0");
        return refInternalAppData.set("0");
      }
    });

// onCreate adds the new connection to all parts of database

exports.addConnectionToDB = functions.database
    .ref("/Connections/{connection}")
    .onCreate((snapshot, context) => {
      const sensorControl = context.params.connection;
      const refDataFromApp = snapshot.ref.root
          .child("/dataFromApp/" + sensorControl);
      const refDataFromChild = snapshot.ref.root
          .child("/dataFromChild/" + sensorControl);
      const refInternalAppData = snapshot.ref.root
          .child("/internalAppData/thresholds/" + sensorControl);
      if (sensorControl == "ControlSwitch") {
        refDataFromApp.set("0~0");
      } else {
        refDataFromApp.set("0");
      }
      refDataFromChild.set("0");
      return refInternalAppData.set("0");
    });

// onDelete deletes all of the data associated with that connection

exports.addConnectionToDB = functions.database
    .ref("/Connections/{connection}")
    .onDelete((snapshot, context) => {
      const sensorControl = context.params.connection;
      const refDataFromApp = snapshot.ref.root
          .child("/dataFromApp/" + sensorControl);
      const refDataFromChild = snapshot.ref.root
          .child("/dataFromChild/" + sensorControl);
      const refInternalAppData = snapshot.ref.root
          .child("/internalAppData/thresholds/" + sensorControl);
      refDataFromApp.remove();
      refDataFromChild.remove();
      return refInternalAppData.remove();
    });
