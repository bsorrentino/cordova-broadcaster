package org.bsc.cordova;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class echoes a string called from JavaScript.
 */
public class CDVBroadcaster extends CordovaPlugin {

    public static final String EVENTNAME_ERROR = "event name null or empty.";

    java.util.Map<String,BroadcastReceiver> receiverMap =
                    new java.util.HashMap<String,BroadcastReceiver>(10);

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if( action.equals("fireNativeEvent")) {

            final String eventName = args.getString(0);
            if( eventName==null || eventName.isEmpty() ) {
                callbackContext.error(EVENTNAME_ERROR);

            }
            return true;
        }
        else if( action.equals("addEventListener")) {

            final String eventName = args.getString(0);
            if( eventName==null || eventName.isEmpty() ) {
                callbackContext.error(EVENTNAME_ERROR);
                return false;
            }
            if( !receiverMap.containsKey(eventName)) {

                final BroadcastReceiver r = new BroadcastReceiver() {

                    @Override
                    public void onReceive(Context context, Intent intent) {

                    }
                };

                LocalBroadcastManager.getInstance(super.webView.getContext())
                        .registerReceiver( r, new IntentFilter(eventName));

                receiverMap.put(eventName,r);
            }
            callbackContext.success();

            return true;
        }
        else if( action.equals("removeEventListener")) {

            final String eventName = args.getString(0);
            if( eventName==null || eventName.isEmpty() ) {
                callbackContext.error(EVENTNAME_ERROR);
                return false;
            }

            BroadcastReceiver r = receiverMap.remove(eventName);

            if( r!=null  ) {

                LocalBroadcastManager.getInstance(super.webView.getContext())
                        .unregisterReceiver( r );


            }
            callbackContext.success();
            return true;
        }
        return false;
    }

    private void fireEvent(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // deregister receiver
    }

}
