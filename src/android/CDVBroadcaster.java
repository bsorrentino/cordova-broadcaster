package org.bsc.cordova;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;

import static java.lang.String.format;

/**
 * This class echoes a string called from JavaScript.
 */
public class CDVBroadcaster extends CordovaPlugin {

    private static String TAG =  CDVBroadcaster.class.getSimpleName();

    public static final String EVENTNAME_ERROR = "event name null or empty.";

    final java.util.Map<String,BroadcastReceiver> receiverMap =
                    new java.util.HashMap<String,BroadcastReceiver>(10);

    /**
     *
     * @param eventName
     * @param data
     * @param <T>
     */
    protected <T> void fireEvent( final String eventName, final Object data) {

        cordova.getActivity().runOnUiThread( new Runnable() {

            @Override
            public void run() {
                String method = null;

                if( data == null ) {
                    method = format("javascript:window.broadcaster.fireEvent( '%s', null );", eventName );
                }
                else if( data instanceof JSONObject ) {
                    method = format("javascript:window.broadcaster.fireEvent( '%s', %s );", eventName, data.toString() );
                }
                else  {
                    method = format("javascript:window.broadcaster.fireEvent( '%s', '%s' );", eventName, data.toString() );
                }
                CDVBroadcaster.this.webView.loadUrl(method);
            }
        });
    }

    /**
     *
     * @param receiver
     * @param filter
     */
    protected void registerReceiver(android.content.BroadcastReceiver receiver, android.content.IntentFilter filter) {
        LocalBroadcastManager.getInstance(super.webView.getContext()).registerReceiver(receiver,filter);
    }

    /**
     *
     * @param receiver
     */
    protected void unregisterReceiver(android.content.BroadcastReceiver receiver) {
        LocalBroadcastManager.getInstance(super.webView.getContext()).unregisterReceiver(receiver);
    }

    /**
     *
     * @param intent
     * @return
     */
    protected boolean sendBroadcast(android.content.Intent intent) {
        return LocalBroadcastManager.getInstance(super.webView.getContext()).sendBroadcast(intent);
    }

    /**
     *
     * @param id            The message id
     * @param data          The message data
     * @return
     */
    @Override
    public Object onMessage(String id, Object data) {

        if( receiverMap.containsKey(id) ) {
            fireEvent( id, data );
        }
        return super.onMessage( id, data );
    }

    /**
     *
     * @param eventName
     * @param userData
     */
    private void fireNativeEvent( final String eventName, JSONObject userData ) {
        if( eventName == null ) {
            throw new IllegalArgumentException("eventName parameter is null!");
        }

        final Intent intent = new Intent(eventName);

        final Bundle bundle = (userData == null ) ? new Bundle() : toBundle( userData );

        intent.putExtras(bundle);

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
            if( userData==null ) {
                Log.w( TAG, "user data provided to native event is null!");
            }

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

                        final Bundle b = intent.getExtras();

                        fireEvent(eventName, toJsonObject(b) );

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

            final BroadcastReceiver r = receiverMap.remove(eventName);

            if (r != null) {
                unregisterReceiver(r);
            }
            callbackContext.success();
            return true;
        }
        return false;
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

    /**
     *  Credit: https://github.com/napolitano/cordova-plugin-intent
     *
     * @param bundle
     * @return
     */
    private static JSONObject toJsonObject(Bundle bundle) {

        if( bundle == null ) {
            Log.w( TAG, "bundle is null!" );
            return new JSONObject();
        }
        return (JSONObject) toJsonValue(bundle);
    }

    /**
     * Credit: https://github.com/napolitano/cordova-plugin-intent
     *
     * @param value
     * @return
     * @throws JSONException
     */
    private static Object toJsonValue(final Object value) {

        // Null
        if (value == null) {
            return JSONObject.NULL;
        }
        // Bundle
        else if (value instanceof Bundle) {
            final JSONObject result = new JSONObject();
            final Bundle bundle = (Bundle) value;
            for (final String key : bundle.keySet()) {
                try {
                    final Object bundle_value = bundle.get(key);
                    result.put(key, toJsonValue(bundle_value));
                } catch (JSONException e) {
                    Log.w( TAG, format( "error getting key %s from bundle\n%s", key), e);
                }
            }
            return result;
        }
        // Native Array
        else if ((value.getClass().isArray())) {
            final JSONArray result = new JSONArray();
            int length = Array.getLength(value);
            for (int i = 0; i < length; ++i) {
                final Object array_value = Array.get(value, i);
                result.put(toJsonValue(array_value));
            }
            return result;
        }
        // ArrayList<?>
        else if (value instanceof ArrayList<?>) {
            final ArrayList arrayList = (ArrayList<?>)value;
            final JSONArray result = new JSONArray();
            for ( Object array_value : arrayList ) {
                result.put( toJsonValue(array_value) );
            }
            return result;
        }
        // Boolean | Integer | Long | Double
        else if (
                value instanceof String
                        || value instanceof Boolean
                        || value instanceof Integer
                        || value instanceof Long
                        || value instanceof Double) {
            return value;
        }
        // Other(s)
        else {
            return String.valueOf(value);
        }
    }

    /**
     * Credit: https://github.com/napolitano/cordova-plugin-intent
     *
     * @param obj
     * @return
     */
    private Bundle toBundle(final JSONObject obj) {
        final Bundle returnBundle = new Bundle();

        if (obj == null) {
            return null;
        }

        final Iterator<?> keys = obj.keys();

        while(keys.hasNext())
        {
            final String key = (String)keys.next();

            try {
                final Object compare = obj.get(key);
                // String
                if (compare instanceof String)
                    returnBundle.putString(key, obj.getString(key));
                    // Boolean
                else if (compare instanceof Boolean)
                    returnBundle.putBoolean(key, obj.getBoolean(key));
                    // Integer
                else if (compare instanceof Integer)
                    returnBundle.putInt(key, obj.getInt(key));
                    // Long
                else if (compare instanceof Long)
                    returnBundle.putLong(key, obj.getLong(key));
                    // Double
                else if (compare instanceof Double)
                    returnBundle.putDouble(key, obj.getDouble(key));
                    // Array | JSONArray
                else if (compare.getClass().isArray() || compare instanceof JSONArray) {
                    final JSONArray jsonArray = obj.getJSONArray(key);
                    int length = jsonArray.length();
                    if (jsonArray.get(0) instanceof String) {
                        final String[] stringArray = new String[length];
                        for (int j = 0; j < length; j++)
                            stringArray[j] = jsonArray.getString(j);
                        returnBundle.putStringArray(key, stringArray);
                        //returnBundle.putParcelableArray(key, obj.get);
                    } else {
                        if (key.equals("PLUGIN_CONFIG")) {
                            final ArrayList<Bundle> bundleArray = new ArrayList<Bundle>();
                            for (int k = 0; k < length; k++) {
                                bundleArray.add(toBundle(jsonArray.getJSONObject(k)));
                            }
                            returnBundle.putParcelableArrayList(key, bundleArray);
                        } else {
                            final Bundle[] bundleArray = new Bundle[length];
                            for (int k = 0; k < length; k++)
                                bundleArray[k] = toBundle(jsonArray.getJSONObject(k));
                            returnBundle.putParcelableArray(key, bundleArray);
                        }
                    }
                }
                // JSONObject
                else if (compare instanceof JSONObject)
                    returnBundle.putBundle(key, toBundle((JSONObject) obj.get(key)));
            }
            catch (JSONException e) {
                Log.w( TAG, format( "error processing key %s \n%s", key ), e );
            }

        }


        return returnBundle;
    }
}
