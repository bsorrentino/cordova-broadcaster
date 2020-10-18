# Cordova Broadcaster

Cordova Plugin to allow message exchange between javascript and native (and viceversa).

[![npm](https://img.shields.io/npm/v/cordova-plugin-broadcaster.svg)](https://www.npmjs.com/package/cordova-plugin-broadcaster) [![Join the chat at https://gitter.im/bsorrentino/cordova-broadcaster](https://badges.gitter.im/bsorrentino/cordova-broadcaster.svg)](https://gitter.im/bsorrentino/cordova-broadcaster?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)


## Ingredient Technologies

Broadcaster plugin providing bridge for the following native technologies:

  target OS | Native Technology
 ----|----
 IOS | **[NotificationCenter](https://developer.apple.com/library/mac/documentation/Cocoa/Reference/Foundation/Classes/NSNotificationCenter_Class/index.html#//apple_ref/occ/instm/NSNotificationCenter/addObserverForName%3aobject%3aqueue%3ausingBlock%3a)**
Android | **[LocalBroadcastManager](http://developer.android.com/reference/android/support/v4/content/LocalBroadcastManager.html)**

## News
  date |  infos | refs
---- | ---- | ----
Mar 19, 2020 | Concerning **Android** I've added support for **broadcast Intent to external Apps**, **receive broadcast Intents from external Apps**, **Flags & Category on Intent** | insipred by [navarrojava's fork](https://github.com/navarrojava/cordova-broadcaster/)
Jan 16, 2018 | I've developed a complete **ionic3** sample project using **broadcaster** | [ionic-broadcaster-sample](https://github.com/bsorrentino/ionic-broadcaster-sample)
Jan 28, 2017 | such plugin has been added to [ionic-native](https://ionicframework.com/docs/v2/native/broadcaster/) distribution | **How to** is available   [here](https://ionicframework.com/docs/v2/native/broadcaster/)



## Installation

```javascript
$ cordova create <PATH> [ID [NAME [CONFIG]]] [options]
$ cd <PATH>
$ cordova platform add [ios|android]
$ cordova plugin add cordova-plugin-broadcaster
```

## Usage:

### From Native to Javascript

#### Javascript
```javascript
    console.log( "register didShow received!" );

    var listener = function( e ) {
      //log: didShow received! userInfo: {"data":"test"}
      console.log( "didShow received! userInfo: " + JSON.stringify(e)  );
    }

    window.broadcaster.addEventListener( "didShow", listener);
```
### From ANDROID to Javascript - Extra ANDROID features

#### Listen for global message
```javascript
if( cordova.platformId === "android" ) {

    var listener = function( e ) {
      //log: didShow received! userInfo: {"data":"test"}
      console.log( "CONNECTIVITY_CHANGE: " + JSON.stringify(e)  );
    }
    var isGlobal = true
    window.broadcaster.addEventListener( 'android.net.conn.CONNECTIVITY_CHANGE', isGlobal, listener);
}
```


#### ANDROID

```Java
final Intent intent = new Intent("didShow");

final Bundle child = new Bundle();
child.putString( "name", "joker");

final Bundle b = new Bundle();
b.putString( "data", "test");
b.putBoolean( "valid", true );
b.putBundle( "child", child );

intent.putExtras( b);

LocalBroadcastManager.getInstance(this).sendBroadcastSync(intent);
```

#### IOS

##### Objective-C
```Objective-C
    NSDictionary * payload = @{
        @"data":@"test",
        @"valid": [NSNumber numberWithBool:YES],
        @"child": @{ @"name": @"joker" }
    };

    [[NSNotificationCenter defaultCenter] postNotificationName:@"TEST.EVENT"
                                                        object:nil
                                                      userInfo:payload];
```

##### Swift 5.x
```swift

  let payload:[String:Any] = [
          "data":"test",
          "valid": true,
          "child":[ "name": "joker" ]
      ]

  let nc = NotificationCenter.default
  nc.post(name:Notification.Name("didShow"), object: nil, userInfo: payload)
```

#### BROWSER

```javascript

let event = new CustomEvent("didShow", { detail: { data:"test"} } );
document.dispatchEvent( event )

```
### From Javascript to Native - ANDROID,BROWSER,IOS

#### Javascript

```javascript
  window.broadcaster.fireNativeEvent( "test.event", { item:'test data' }, function() {
    console.log( "event fired!" );
    } );
```

### From Javascript to ANDROID - Extra ANDROID features

#### Send a message with "flags" and "category"
```javascript
if( cordova.platformId === "android" ) {

  // send a message with "flags" and "category"
  window.broadcaster.fireNativeEvent( "message", { extras:{ item:'test data' }, flags:0, category:'android.intent.category.INFO', packageName:'org.bsc'}, function() {
    console.log( "event fired!" );
  });
}
```

#### Send a global message
```javascript
if( cordova.platformId === "android" ) {

  // send a global message
  var isGlobal = true
  window.broadcaster.fireNativeEvent( "GLOBAL_ACTION", isGlobal, { item:'test data' }, function() {
    console.log( "event fired!" );
  });

}
```

#### Send a global message with "flags" and "category"
```javascript
if( cordova.platformId === "android" ) {

  // send a global message with "flags" and "category"
  var isGlobal = true
  window.broadcaster.fireNativeEvent( "GLOBAL_ACTION", isGlobal, { extras:{ item:'test data' }, flags:0, category:'android.intent.category.INFO', packageName:'org.bsc'}, function() {
    console.log( "event fired!" );
  });

}
```

#### ANDROID

```Java
final BroadcastReceiver receiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
        String data = intent.getExtras().getString("data");

        Log.d("CDVBroadcaster",
                String.format("Native event [%s] received with data [%s]", intent.getAction(), data));

    }
};

LocalBroadcastManager.getInstance(this)
            .registerReceiver(receiver, new IntentFilter("test.event"));
}
```

#### IOS

##### Objective-C

```Objective-C
[[NSNotificationCenter defaultCenter] addObserverForName:@"test.event"
                                                  object:nil
                                                   queue:[NSOperationQueue mainQueue]
                                              usingBlock:^(NSNotification *notification) {
                                                      NSLog(@"Handled 'test.event' [%@]", notification.userInfo[@"item"]);
                                                    }];
```

##### Swift 5.x

```swift
let nc = NotificationCenter.default
nc.addObserver(forName:Notification.Name(rawValue:"test.event"),
               object:nil, queue:nil) {
  notification in
  print( "\(notification.userInfo)")
}
```

#### BROWSER

```javascript

document.addEventListener( "test.event", ( ev:Event ) => {
  console.log( "test event", ev.detail );
});

```
