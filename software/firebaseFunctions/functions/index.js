const functions = require("firebase-functions");

exports.makeUppercase = functions.database.ref("/alexa/control switch/data")
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
