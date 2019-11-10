
var exec    = require('cordova/exec');
var channel = require('cordova/channel');

type  Listener = (event:Event)=>void;

interface Channel {
  subscribe( handler:Listener ):void;
  unsubscribe( handler:Listener ):void;
  fire( event:Event ):void;
  numHandlers:number;
}

type Channels = {
    [ key:string ]:Channel;
};


class Broadcaster {

  private _debug = false;
  private _channels:Channels = {};

  private _channelCreate = ( c:string ) => {
    if( this._debug ) console.log( "CHANNEL " + c + " CREATED! ");
    this._channels[c] = channel.create(c);
  }

  private _channelDelete = ( c:string ) => {
    delete this._channels[c];
    if( this._debug ) console.log( "CHANNEL " + c + " DELETED! ");
  }

  private _channelSubscribe = ( c:string, f:Listener ) => {
    var channel = this._channels[c];
    channel.subscribe(f);
    if( this._debug ) console.log( "CHANNEL " + c + " SUBSCRIBED! " + channel.numHandlers);
    return channel.numHandlers;
  }

  private _channelUnsubscribe = ( c:string, f:Listener ) => {
    var channel = this._channels[c];
    channel.unsubscribe(f);
    if( this._debug ) console.log( "CHANNEL " + c + " UNSUBSCRIBED! " + channel.numHandlers);
    return channel.numHandlers;
  }

  private _channelFire = ( event:Event ) => {
    if( this._debug ) console.log( "CHANNEL " + event.type + " FIRED! ");
    this._channels[event.type].fire(event);
  }

  private _channelExists = ( c:string ) => {
     return this._channels.hasOwnProperty(c);
  }

  /**
   * fire native evet
   *
   */
  fireNativeEvent(type: string, data: object | null, success?: () => void, error?: (message: string) => void):void
  {
       exec(success, error, "broadcaster", "fireNativeEvent", [ type, data ]);
  }

  /**
   * fire local evet
   *
   */
  fireEvent(type:string, data: object | null ):void
  {
    if( !this._channelExists(type) ) return;

    var event = document.createEvent('Event');
    event.initEvent(type, false, false);
    if (data) {
        for (var i in data) {
            if (data.hasOwnProperty(i)) {
                event[i] = data[i];
            }
        }
    }
    this._channelFire( event );
  }

  /**
   * add a listener
   *
   */
  addEventListener(eventname:string,f:Listener):void {

     if (!this._channelExists(eventname)) {
         this._channelCreate(eventname);
         exec( () => this._channelSubscribe(eventname,f),
              (err:any) => console.log( "ERROR addEventListener: ", err),
              "broadcaster", "addEventListener", [ eventname ]);
     }
     else {
       this._channelSubscribe(eventname,f);
     }
  }

  /**
   * remove a listener
   *
   */
  removeEventListener(eventname:string, f:Listener):void {

     if (this._channelExists(eventname)) {
        if( this._channelUnsubscribe(eventname, f) === 0 ) {
          exec( () => this._channelDelete(eventname),
                (err:any) => console.log( "ERROR removeEventListener: ", err),
                "broadcaster", "removeEventListener", [ eventname ]);

        }
     }
  }

}


module.exports = new Broadcaster();
