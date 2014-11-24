package org.bsc.cordova;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class echoes a string called from JavaScript.
 */
public class CDVBroadcaster extends CordovaPlugin {

    java.util.Map<String,BroadcastReceiver> receiverMap =
                    new java.util.HashMap<String,BroadcastReceiver>(10);
                    
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("fireEvent")) {
            String message = args.getString(0);
            this.fireEvent(message, callbackContext);
            return true;
        }
        else if( action.equals("fireNativeEvent")) {

        }
        else if( action.equals("addEventListener")) {

        }
        else if( action.equals("removeEventListener")) {

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
}
