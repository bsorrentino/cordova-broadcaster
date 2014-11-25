package org.bsc.cordova;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.webkit.ValueCallback;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class echoes a string called from JavaScript.
 */
public class CDVBroadcaster extends CordovaPlugin {

    private static String TAG =  CDVBroadcaster.class.getSimpleName();

    public static final String EVENTNAME_ERROR = "event name null or empty.";

    java.util.Map<String,BroadcastReceiver> receiverMap =
                    new java.util.HashMap<String,BroadcastReceiver>(10);

    private void fireEvent() {

    }
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if( action.equals("fireNativeEvent")) {

            final String eventName = args.getString(0);
            if( eventName==null || eventName.isEmpty() ) {
                callbackContext.error(EVENTNAME_ERROR);

            }
            return true;
        }
        else {
            if (action.equals("addEventListener")) {

                final String eventName = args.getString(0);
                if (eventName == null || eventName.isEmpty()) {
                    callbackContext.error(EVENTNAME_ERROR);
                    return false;
                }
                if (!receiverMap.containsKey(eventName)) {

                    final BroadcastReceiver r = new BroadcastReceiver() {

                        @Override
                        public void onReceive(Context context, final Intent intent) {

                            final Bundle b = intent.getExtras();

                            // parse the JSON passed as a string.
                            try {

                                final String userData = b.getString("userdata", "{}");

                                final JSONObject json = new JSONObject(userData); // CHECK IF VALID

                                String method = String.format( "window.broadcaster.fireEvent( '%s', %s );", eventName, userData);

                                CDVBroadcaster.this.webView.evaluateJavascript(method, new ValueCallback<String>() {
                                    @Override
                                    public void onReceiveValue(String value) {
                                        Log.d(TAG, "fireEvent executed!");
                                    }
                                });

                            } catch (JSONException e) {
                                Log.e(TAG, "'userdata' is not a valid json object!");
                            }

                        }
                    };

                    LocalBroadcastManager.getInstance(super.webView.getContext())
                            .registerReceiver(r, new IntentFilter(eventName));

                    receiverMap.put(eventName, r);
                }
                callbackContext.success();

                return true;
            } else if (action.equals("removeEventListener")) {

                final String eventName = args.getString(0);
                if (eventName == null || eventName.isEmpty()) {
                    callbackContext.error(EVENTNAME_ERROR);
                    return false;
                }

                BroadcastReceiver r = receiverMap.remove(eventName);

                if (r != null) {

                    LocalBroadcastManager.getInstance(super.webView.getContext())
                            .unregisterReceiver(r);


                }
                callbackContext.success();
                return true;
            }
        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // deregister receiver
        for( BroadcastReceiver r : receiverMap.values() ) {
            LocalBroadcastManager.getInstance(super.webView.getContext())
                    .unregisterReceiver(r);

        }

        receiverMap.clear();

    }

}
