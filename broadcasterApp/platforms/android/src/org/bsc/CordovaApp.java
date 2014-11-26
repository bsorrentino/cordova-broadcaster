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

package org.bsc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.apache.cordova.*;

public class CordovaApp extends CordovaActivity {


    final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
                Log.d("CDVBroadcaster", String.format("Naptive event [%s] received", intent.getAction()));
        }
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        //super.setBooleanProperty("showTitle", true);
        super.onCreate(savedInstanceState);
        super.init();
        // Set by <content src="index.html" /> in config.xml
        loadUrl(launchUrl);

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(receiver, new IntentFilter("test.event"));
    }

    @Override
    public void onDestroy() {

        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(receiver);

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        final MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.options, menu);

        return true;
    }

    /*
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }
    */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.sendNativeMsg) {

            final Intent intent = new Intent("didShow");

            Bundle b = new Bundle();
            b.putString( "userdata", "{ data: 'test'}" );
            intent.putExtras( b);

            LocalBroadcastManager.getInstance(this).sendBroadcastSync(intent);


        }

        return (super.onOptionsItemSelected(item));


    }
}