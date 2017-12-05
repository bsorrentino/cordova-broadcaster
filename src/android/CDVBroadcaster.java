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
                    method = String.format("javascript:window.broadcaster.fireEvent( '%s', null );", eventName );
                }
                else if( data instanceof JSONObject ) {
                    method = String.format("javascript:window.broadcaster.fireEvent( '%s', %s );", eventName, data.toString() );
                }
                else  {
                    method = String.format("javascript:window.broadcaster.fireEvent( '%s', '%s' );", eventName, data.toString() );
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

    private void fireNativeEvent( final String eventName, JSONObject userData ) {
        if( eventName == null ) {
            throw new IllegalArgumentException("eventName parameter is null!");
        }

        final Intent intent = new Intent(eventName);

        intent.putExtras(toBundle( new Bundle(), userData ));

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
     * Credit: https://github.com/darryncampbell/darryncampbell-cordova-plugin-intent
     *
     * @param bundle
     * @param object
     * @return
     */
    private static Bundle toBundle(final Bundle bundle, JSONObject object) {
        if( bundle == null || object == null ) return bundle;

        final java.util.Iterator<String> keys = object.keys();

        while( keys.hasNext() ) {
            final String key = keys.next();

            if (object.isNull(key)) {
                continue;
            }

            final Object value = object.opt(key);

            if (value instanceof Boolean) {
                bundle.putBoolean(key, (Boolean) value);
            } else if (value instanceof Long) {
                bundle.putLong(key, (Long) value);
            } else if (value instanceof Double) {
                bundle.putDouble(key, (Double) value);
            } else if (value instanceof Integer) {
                bundle.putInt(key, (Integer) value);
            } else if (value instanceof JSONObject) {
                bundle.putBundle(key, toBundle(new Bundle(), (JSONObject) value));
            } else if (value instanceof JSONArray) {

                try {
                    final JSONArray values = (JSONArray) value;

                    final JSONArray index = new JSONArray();
                    for (int i = 0; i < values.length(); ++i) {
                        index.put(String.valueOf(i));
                    }

                    bundle.putBundle(key, toBundle(new Bundle(), values.toJSONObject(index)));
                } catch (JSONException e) {
                    Log.w(TAG, String.format("error creating bundle from array for key %s", key), e);
                }
            } else {
                bundle.putCharSequence(key, String.valueOf(value));
            }
        }

        return bundle;
    }

    /**
     * Credit: https://github.com/darryncampbell/darryncampbell-cordova-plugin-intent
     *
     * @param bundle
     * @return
     */
    private static JSONObject toJsonObject(final Bundle bundle) {
        final JSONObject result = new JSONObject();

        if( bundle != null ) {

            for (final String key : bundle.keySet()) {
                try {
                    result.putOpt(key, toJsonValue(bundle.get(key)));
                } catch (JSONException e) {
                    Log.w( TAG, String.format("error parsing Bundle key %s", key), e);
                }
            }

        }

        return result;
    }

    /**
     *
     * Credit: https://github.com/darryncampbell/darryncampbell-cordova-plugin-intent
     *
     * @param value
     * @return
     * @throws JSONException
     */
    private static Object toJsonValue(final Object value) {

        if (value == null) return JSONObject.NULL;

        if (value.getClass().isArray()) {
            final JSONArray result = new JSONArray();
            int length = java.lang.reflect.Array.getLength(value);
            for (int i = 0; i < length; ++i) {
                final Object v = java.lang.reflect.Array.get(value, i);
                try {
                    result.put(i, toJsonValue(v));
                } catch (JSONException e) {
                    Log.w( TAG, String.format("error parsing array element %d vaule %s", i,v), e);
                }
            }
            return result;
        } else if ( value instanceof String
                        || value instanceof Boolean
                        || value instanceof Integer
                        || value instanceof Long
                        || value instanceof Double) {
            return value;
        } else {
            return String.valueOf(value);
        }
    }
}
