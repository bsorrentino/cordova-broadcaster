Cordova Broadcaster
=====================

Cordova Plugin to allow message exchange between javascript and native (and viceversa).
From *2.0.1* deployed as [NPM package](https://www.npmjs.com/package/cordova-plugin-broadcaster)  

IOS
===

> Providing bridge to **[NotificationCenter](https://developer.apple.com/library/mac/documentation/Cocoa/Reference/Foundation/Classes/NSNotificationCenter_Class/index.html#//apple_ref/occ/instm/NSNotificationCenter/addObserverForName%3aobject%3aqueue%3ausingBlock%3a)**


Android
=======

> Providing bridge to **[LocalBroadcastManager](http://developer.android.com/reference/android/support/v4/content/LocalBroadcastManager.html)**


INSTALL
========

```javascript
$ cordova create <PATH> [ID [NAME [CONFIG]]] [options]
$ cd <PATH>
$ cordova platform add [ios|android]
$ cordova plugin add cordova-plugin-broadcaster
```

USAGE:
======

## From Native to Javascript

### Javascript

```javascript
    console.log( "register didShow received!" );
    window.broadcaster.addEventListener( "didShow", function( e ) {
                //log: didShow received! userInfo: {"data":"test"}
                console.log( "didShow received! userInfo: " + JSON.stringify(e)  );
    });
```

### IOS

```Objective-C
[[NSNotificationCenter defaultCenter] postNotificationName:@"didShow"
                                                    object:nil
                                                  userInfo:@{ @"data":@"test"}];
```

### ANDROID

```Java
final Intent intent = new Intent("didShow");

Bundle b = new Bundle();
b.putString( "userdata", "{ data: 'test'}" );
intent.putExtras( b);

LocalBroadcastManager.getInstance(this).sendBroadcastSync(intent);
```

## From Javascript to Native

### Javascript

```javascript
  window.broadcaster.fireNativeEvent( "test.event", { item:'test data' }, function() {
    console.log( "event fired!" );
    } );
 ```

### IOS

```Objective-C
[[NSNotificationCenter defaultCenter] addObserverForName:@"test.event"
                                                  object:nil
                                                   queue:[NSOperationQueue mainQueue]
                                              usingBlock:^(NSNotification *note) {

                                                      NSLog(@"Handled 'test.event' [%@]", note.userInfo[@"item"]);

                                                    }];
```

### ANDROID

```Java
final BroadcastReceiver receiver = new BroadcastReceiver() {
  @Override
  public void onReceive(Context context, Intent intent) {
          final JSONObject data = new JSONObject( intent.getBundle().getString("userdata"));

          Log.d("CDVBroadcaster",
            String.format("Naptive event [%s] received with data [%s]", intent.getAction(), data.toString()));
  }
};

LocalBroadcastManager.getInstance(this)
            .registerReceiver(receiver, new IntentFilter("test.event"));
}
```
