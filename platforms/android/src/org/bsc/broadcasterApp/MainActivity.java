/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */

package org.bsc.broadcasterApp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


import org.apache.cordova.*;
import org.bsc.cordova.CDVBroadcaster;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends CordovaActivity {

    private static String TAG = CDVBroadcaster.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Set by <content src="index.html" /> in config.xml
        loadUrl(launchUrl);


    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Log.d(TAG, "send message");
        final Intent intent = new Intent("didShow");

        Bundle b = new Bundle();
        b.putString("userdata", "{ data: 'test'}");
        intent.putExtras(b);

        LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcastSync(intent);

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.e(TAG, String.format("onCreateOptionsMenu"));

        menu.add( "sendMessage")
        ;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.e(TAG, String.format("onStart"));

        final BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final JSONObject data;
                try {
                    data = new JSONObject(intent.getStringExtra("userdata"));
                    Log.d(TAG,
                            String.format("Native event [%s] received with data [%s]", intent.getAction(), data.toString()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        };

        LocalBroadcastManager.getInstance(super.appView.getContext())
                .registerReceiver(receiver, new IntentFilter("test.event"));


    }
}
