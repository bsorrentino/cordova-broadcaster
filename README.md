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

#### ANDROID

```Java
final Intent intent = new Intent("didShow");

Bundle b = new Bundle();
b.putString( "data", "test" );
intent.putExtras( b);

LocalBroadcastManager.getInstance(this).sendBroadcastSync(intent);
```

#### IOS

##### Objective-C
```Objective-C
[[NSNotificationCenter defaultCenter] postNotificationName:@"didShow"
                                                    object:nil
                                                  userInfo:@{ @"data":@"test"}];
```

##### Swift
```swift
let nc = NSNotificationCenter.default
nc.post(name:"didShow", object: nil, userInfo: ["data":"test"])
```

#### BROWSER

```javascript

let event = new CustomEvent("didShow", { detail: { data:"test"} } );
document.dispatchEvent( event )

```
### From Javascript to Native

#### Javascript

```javascript
  window.broadcaster.fireNativeEvent( "test.event", { item:'test data' }, function() {
    console.log( "event fired!" );
    } );
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

##### Swift 3.0

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
