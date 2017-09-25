var browser = require('cordova/platform');

module.exports = {

  fireNativeEvent = function(eventname, data, success, error) {
    console.log( "fileNativeEvent", eventname);
    success({});
  },
  addEventListener = function (eventname,f) {
    console.log( "addEventListener", eventname);
    success({});
  },
  removeEventListener = function(eventname, f) {
    console.log( "removeEventListener", eventname);
    success({});
  }

}

require("cordova/exec/proxy").add("Broadcaster", module.exports);
