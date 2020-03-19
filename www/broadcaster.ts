
var exec    = require('cordova/exec');
var channel = require('cordova/channel');

type  Listener = (event:Event)=>void;
type AndroidData = {
  extras:object;
  flags:number;
  category:string;
}

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
   * @param type 
   * @param globalFlagOrData 
   * @param data 
   * @param success 
   * @param error 
   */
  fireNativeEvent(type: string, globalFlagOrData:boolean|object|AndroidData|null, data?:object|AndroidData|null, success?: () => void, error?: (message: string) => void):void
  {
    let isGlobal = false
    let oData:object|AndroidData|null = null

    if( typeof globalFlagOrData === 'boolean') {
      isGlobal = globalFlagOrData
      
      oData = data ?? null 

    } else if( typeof globalFlagOrData === 'object') {
      
      oData = globalFlagOrData
    
    }

    //function instanceOfAndroidData( object:any ): object is AndroidData {
    //  return ( 'extras' in object && 'flags' in object && 'category' in object )
    //}
    
    //if( oData!=null && this._instanceOfAndroidData(oData) ) {
    //  return exec(success, error, "broadcaster", "fireNativeEvent", [ type, oData.extras, isGlobal, oData.flags, oData.category ]);
    //}
    
    exec(success, error, "broadcaster", "fireNativeEvent", [ type, oData, isGlobal ]);
  }

  /**
   * fire local event
   * 
   * @param type 
   * @param data 
   */
  fireEvent(type:string, data: object | null ):void
  {
    if( !this._channelExists(type) ) return;

    const event = document.createEvent('Event');
    event.initEvent(type, false, false);
    if (data) {
        for (var i in data) {
            if (data.hasOwnProperty(i)) {
                (<any>event)[i] = (<any>data)[i];
            }
        }
    }
    this._channelFire( event );
  }

  /**
   * add a listener
   * 
   * @param eventname 
   * @param globalFlagOrListener 
   * @param listener 
   */
  addEventListener(eventname:string, globalFlagOrListener: boolean|Listener, listener?:Listener):void {
    let isGlobal = false
    let f:Listener = () => {}

    if( typeof globalFlagOrListener === 'boolean') {
      isGlobal = globalFlagOrListener
      
      if( !listener ) throw "listener must be defined!";
      
      f = listener

    } else if( typeof globalFlagOrListener === 'function') {
      
      f = globalFlagOrListener
    
    }

     if (!this._channelExists(eventname)) {
         this._channelCreate(eventname);
         exec( () => this._channelSubscribe(eventname,f),
              (err:any) => console.log( "ERROR addEventListener: ", err),
              "broadcaster", "addEventListener", [ eventname, isGlobal ]);
     }
     else {
       this._channelSubscribe(eventname,f);
     }
  }

  /**
   * remove a listener
   * 
   * @param eventname 
   * @param listener 
   */
  removeEventListener(eventname:string, listener:Listener):void {

     if (this._channelExists(eventname)) {
        if( this._channelUnsubscribe(eventname, listener) === 0 ) {
          exec( () => this._channelDelete(eventname),
                (err:any) => console.log( "ERROR removeEventListener: ", err),
                "broadcaster", "removeEventListener", [ eventname ]);

        }
     }
  }

    
}

module.exports = new Broadcaster();
