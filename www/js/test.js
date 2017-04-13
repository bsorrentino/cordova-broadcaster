;(function() {

console.log( "INIT TEST!!");

function onReady() {
  console.log( "register didShow listener" );

  var listener = function( e ) {
      console.log( "didShow event received! " + JSON.stringify(e) );
      window.broadcaster.removeEventListener( "didShow", listener );
  }
  window.broadcaster.addEventListener( "didShow", listener );

  var listener1 = function( e ) {
      console.log( "didShow event received after remove! " + JSON.stringify(e) );
      //window.broadcaster.removeEventListener( "didShow", listener1 );
  }
  window.broadcaster.addEventListener( "didShow", listener1);

  window.broadcaster.addEventListener( "powerStateDidChange", function( e ) {
    console.log( "powerStateDidChange", e );
  });

  cordova.plugins.broadcasterTest.startMonitoringPowerState(
    function(initState) {

      console.log( "START MONITOR POWER STATE!", JSON.stringify(initState) );
    },
    function(e) {
      console.log( "ERROR STARTING MONITOR POWER STATE ", e);

    }
  )
}

document.addEventListener('deviceready', onReady, false);
})();
