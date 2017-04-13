
var exec = require('cordova/exec');
var channel = require('cordova/channel');

function Broadcaster() {
  var _debug = false;
  //console.log( "NEW BROADCASTER");
  this._channels = {};

  this.channelExists = function( c ) {
    //return (c in this._channels);
    return this._channels.hasOwnProperty(c);
  }

  this.channelCreate = function( c ) {
    if( _debug ) console.log( "CHANNEL " + c + " CREATED! ");
    this._channels[c] = channel.create(c);
  }
  this.channelSubscribe = function( c, f ) {
    var channel = this._channels[c];
    channel.subscribe(f);
    if( _debug ) console.log( "CHANNEL " + c + " SUBSCRIBED! " + channel.numHandlers);
    return channel.numHandlers;
  }
  this.channelUnsubscribe = function( c, f ) {
    var channel = this._channels[c];
    channel.unsubscribe(f);
    if( _debug ) console.log( "CHANNEL " + c + " UNSUBSCRIBED! " + channel.numHandlers);
    return channel.numHandlers;
  }
  this.channelFire = function( event ) {
    if( _debug ) console.log( "CHANNEL " + event.type + " FIRED! ");
    this._channels[event.type].fire(event);
  }
  this.channelDelete = function( c ) {
    delete this._channels[c];
    if( _debug ) console.log( "CHANNEL " + c + " DELETED! ");
  }

}

Broadcaster.prototype.fireNativeEvent = function(eventname, data, success, error) {
     exec(success, error, "broadcaster", "fireNativeEvent", [ eventname, data ]);
}

Broadcaster.prototype.fireEvent = function(type, data) {
  if( !this.channelExists(type) ) return;

  var event = document.createEvent('Event');
  event.initEvent(type, false, false);
  if (data) {
      for (var i in data) {
          if (data.hasOwnProperty(i)) {
              event[i] = data[i];
          }
      }
  }
  this.channelFire( event );
}

function _debug( msg, o ) {
  console.log( msg );
  for( var m in o ) {
    console.log( "==> " + m);
  }
}

Broadcaster.prototype.addEventListener = function (eventname,f) {

   if (!this.channelExists(eventname)) {
       this.channelCreate(eventname);
       var me = this;
       exec( function() {
         me.channelSubscribe(eventname,f);
       }, function(err)  {
         console.log( "ERROR addEventListener: ", err)
       }, "broadcaster", "addEventListener", [ eventname ]);
   }
   else {
     this.channelSubscribe(eventname,f);
   }
}

Broadcaster.prototype.removeEventListener = function(eventname, f) {

   if (this.channelExists(eventname)) {
      if( this.channelUnsubscribe(eventname, f) === 0 ) {
        var me = this;
        exec( function() {
          me.channelDelete(eventname);
        }, function(err)  {
          console.log( "ERROR removeEventListener: ", err)
        }, "broadcaster", "removeEventListener", [ eventname ]);

      }
   }
}

module.exports = new Broadcaster();
