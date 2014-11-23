
var exec = require('cordova/exec');
var channel = require('cordova/channel');

module.exports = {

  _channels: {},
  fireNativeEvent: function (eventname, data, success, error) {
     exec(success, error, "broadcaster", "fireNativeEvent", [ eventname, data ]);
  },
  fireEvent: function (event) {
     if (event && (event.type in this._channels)) {
         this._channels[event.type].fire(event);
     }
  },
  //removeChannel: function( eventname ) {
  //  delete this._channels[eventname];
  //},
  addEventListener: function (eventname,f) {
     if (!(eventname in this._channels)) {
         this._channels[eventname] = channel.create(eventname);
     }
     this._channels[eventname].subscribe(f);
  },
  removeEventListener: function(eventname, f) {
     if (eventname in this._channels) {
         this._channels[eventname].unsubscribe(f);
     }
  }

};
