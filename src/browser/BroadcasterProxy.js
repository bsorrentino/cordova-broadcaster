var browser = require('cordova/platform');

  /**
   * 
   * to simulate native event use 'broadcaster.fireEvent( event, data );'
   */
  module.exports = {

  /**
   * opts: [eventname:string, data:any]
   */
   fireNativeEvent:function(success, error, opts) {
    var event = new CustomEvent(opts[0], { detail: opts[1] } );
    if( document.dispatchEvent( event ) ) success({});
  },
  /**
   * opts: [eventname:string]
   * 
   */
  addEventListener:function (success, error, opts ) {
    success({});
  },
  /**
   * opts: [eventname]
   */
  removeEventListener:function(success, error, opts) {
    success({});
  }

}

require("cordova/exec/proxy").add("broadcaster", module.exports);
