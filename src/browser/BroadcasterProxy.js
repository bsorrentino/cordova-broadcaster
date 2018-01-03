/**
 * 
 * to simulate native event use:
 *  var event = new CustomEvent(<event>, { detail: <data> } );
 *  document.dispatchEvent( event )
 *  
 */

var browser = require('cordova/platform');

var _handler = function( ev ) {
  window.broadcaster.fireEvent( ev.type, ev.detail );  
}

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
    document.addEventListener( opts[0], _handler )
    success({});
  },
  /**
   * opts: [eventname]
   */
  removeEventListener:function(success, error, opts) {
    document.removeEventListener( opts[0], _handler )
    success({});
  }

}

require("cordova/exec/proxy").add("broadcaster", module.exports);
