package org.bsc.cordova;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * This class echoes a string called from JavaScript.
 */
public class CDVBroadcaster extends CordovaPlugin {

    public static final String USERDATA = "userdata";
    private static String TAG =  CDVBroadcaster.class.getSimpleName();

    public static final String EVENTNAME_ERROR = "event name null or empty.";

    java.util.Map<String,BroadcastReceiver> receiverMap =
                    new java.util.HashMap<String,BroadcastReceiver>(10);

    /**
     *
     * @param eventName
     * @param jsonUserData
     * @throws JSONException
     */
    protected void fireEvent( final String eventName, final Object jsonUserData) throws JSONException {

        String method = null;
        if( jsonUserData != null ) {
            final String data = String.valueOf(jsonUserData);
            if (!(jsonUserData instanceof JSONObject)) {
                final JSONObject json = new JSONObject(data); // CHECK IF VALID
            }
            method = String.format("window.broadcaster.fireEvent( '%s', %s );", eventName, data);
        }
        else {
            method = String.format("window.broadcaster.fireEvent( '%s', {} );", eventName);
        }
        sendJavascript(method);
    }

    protected void registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        getContext().registerReceiver(receiver, filter);
    }

    protected void unregisterReceiver(BroadcastReceiver receiver) {
        getContext().unregisterReceiver(receiver);
    }

    protected void sendBroadcast(Intent intent) {
        getContext().sendBroadcast(intent);
    }

    private Context getContext() {
        return super.webView.getContext();
    }

    @Override
    public Object onMessage(String id, Object data) {

        try {
            fireEvent( id, data );
        } catch (JSONException e) {
            Log.e(TAG, String.format("userdata [%s] for event [%s] is not a valid json object!", data, id));
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    private void fireNativeEvent( final String eventName, JSONObject userData ) {
        if( eventName == null ) {
            throw new IllegalArgumentException("eventName parameter is null!");
        }

        final Intent intent = new Intent(eventName);

        if( userData != null ) {
            Bundle b = new Bundle();
            b.putString(USERDATA, userData.toString());
            intent.putExtras(b);
        }

        sendBroadcast( intent );
    }

    /**
     *
     * @param action          The action to execute.
     * @param args            The exec() arguments.
     * @param callbackContext The callback context used when calling back into JavaScript.
     * @return
     * @throws JSONException
     */
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if( action.equals("fireNativeEvent")) {

            final String eventName = args.getString(0);
            if( eventName==null || eventName.isEmpty() ) {
                callbackContext.error(EVENTNAME_ERROR);

            }
            final JSONObject userData = args.getJSONObject(1);

            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    fireNativeEvent(eventName, userData);
                }
            });

            callbackContext.success();
            return true;
        }
        else if (action.equals("addEventListener")) {

            final String eventName = args.getString(0);
            if (eventName == null || eventName.isEmpty()) {
                callbackContext.error(EVENTNAME_ERROR);
                return false;
            }
            if (!receiverMap.containsKey(eventName)) {

                final BroadcastReceiver r = new BroadcastReceiver() {

                    @Override
                    public void onReceive(Context context, final Intent intent) {
                        final Bundle bundle = intent.getExtras();
                        JSONObject jsonObject = toJsonObject(bundle);
                        try {
                            fireEvent(eventName, jsonObject);
                        } catch (JSONException e) {
                            Log.e(TAG, String.format("Error firing event '%s'", eventName), e);
                        }
                    }
                };

                registerReceiver(r, new IntentFilter(eventName));

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

                unregisterReceiver(r);
            }
            callbackContext.success();
            return true;
        }
        return false;
    }

    private static JSONObject toJsonObject(Bundle bundle) {
        if (bundle == null) {
            Log.v(TAG, "No extra information in intent bundle");
            return new JSONObject();
        }

        JSONObject jsonObject = new JSONObject();

        Set<String> keys = bundle.keySet();
        for (String key: keys) {
            Object value = bundle.get(key);
            try {
                jsonObject.put(key, wrapObject(value));
            } catch (JSONException e) {
                Log.e(TAG, String.format("Unable to convert bundle key '%s' to JSON", key), e);
            }
        }

        return jsonObject;
    }

    /**
     * Wraps the given object if necessary.
     * Copied from https://android.googlesource.com/platform/libcore/+/master/json/src/main/java/org/json/JSONObject.java
     *
     * <p>If the object is null or , returns {@link JSONObject#NULL}.
     * If the object is a {@code JSONArray} or {@code JSONObject}, no wrapping is necessary.
     * If the object is {@code NULL}, no wrapping is necessary.
     * If the object is an array or {@code Collection}, returns an equivalent {@code JSONArray}.
     * If the object is a {@code Map}, returns an equivalent {@code JSONObject}.
     * If the object is a primitive wrapper type or {@code String}, returns the object.
     * Otherwise if the object is from a {@code java} package, returns the result of {@code toString}.
     * If wrapping fails, returns null.
     */
    private static Object wrapObject(Object o) {
        if (o == null) {
            return JSONObject.NULL;
        }
        if (o instanceof JSONArray || o instanceof JSONObject) {
            return o;
        }
        if (o.equals(JSONObject.NULL)) {
            return o;
        }
        try {
            if (o instanceof byte[]) {
                return bytesToHex((byte[])o);
            } else if (o instanceof Collection) {
                return new JSONArray((Collection) o);
            } else if (o.getClass().isArray()) {
                return arrayToJsonArray(o);
            }
            if (o instanceof Map) {
                return new JSONObject((Map) o);
            }
            if (o instanceof Boolean ||
                    o instanceof Byte ||
                    o instanceof Character ||
                    o instanceof Double ||
                    o instanceof Float ||
                    o instanceof Integer ||
                    o instanceof Long ||
                    o instanceof Short ||
                    o instanceof String) {
                return o;
            }
            if (o.getClass().getPackage().getName().startsWith("java.")) {
                return o.toString();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * Creates a new {@code JSONArray} with values from the given primitive array.
     * Partially copied from https://android.googlesource.com/platform/libcore/+/master/json/src/main/java/org/json/JSONArray.java
     */
    private static JSONArray arrayToJsonArray(Object array) throws JSONException {
        if (!array.getClass().isArray()) {
            throw new JSONException("Not a primitive array: " + array.getClass());
        }
        final int length = Array.getLength(array);
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < length; ++i) {
            jsonArray.put(wrapObject(Array.get(array, i)));
        }
        return jsonArray;
    }

    // reference: http://stackoverflow.com/a/9855338
    final private static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     *
     */
    @Override
    public void onDestroy() {
        // deregister receiver
        for( BroadcastReceiver r : receiverMap.values() ) {
                    unregisterReceiver(r);
        }

        receiverMap.clear();

        super.onDestroy();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void sendJavascript(final String javascript) {
        webView.getView().post(new Runnable() {
           @Override
           public void run() {
               if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    webView.sendJavascript(javascript);
                   } else {
                    webView.loadUrl("javascript:".concat(javascript));
                    }
               }
            });
    }
}
