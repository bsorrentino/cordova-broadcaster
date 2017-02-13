# Cordova Broadcaster

Cordova Plugin to allow message exchange between javascript and native (and viceversa).

[![npm](https://img.shields.io/npm/v/cordova-plugin-broadcaster.svg)](https://www.npmjs.com/package/cordova-plugin-broadcaster)


## Ingredient Technologies

Broadcaster plugin providing bridge for the following native technologies:

  target OS | Native Technology
 ----|----
 IOS | **[NotificationCenter](https://developer.apple.com/library/mac/documentation/Cocoa/Reference/Foundation/Classes/NSNotificationCenter_Class/index.html#//apple_ref/occ/instm/NSNotificationCenter/addObserverForName%3aobject%3aqueue%3ausingBlock%3a)**
Android | **[LocalBroadcastManager](http://developer.android.com/reference/android/support/v4/content/LocalBroadcastManager.html)**

## News

 Jan 28, 2017 | such plugin has been added to [ionic-native](https://ionicframework.com/docs/v2/native/) distribution | `How to` is available   [here](https://ionicframework.com/docs/v2/native/broadcaster/)
 ---- | ---- | ----


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
    var listener = window.broadcaster.addEventListener( "didShow", function( e ) {
                //log: didShow received! userInfo: {"data":"test"}
                console.log( "didShow received! userInfo: " + JSON.stringify(e)  );
    });
```

### IOS

#### Objective-C

```Objective-C
[[NSNotificationCenter defaultCenter] postNotificationName:@"didShow"
                                                    object:nil
                                                  userInfo:@{ @"data":@"test"}];
```
#### Swift 2.2
```swift
let nc = NSNotificationCenter.defaultCenter()
nc.postNotificationName("didShow",
                        object: nil,
                        userInfo: ["data":"test"])
```
#### Swift 3.0
```swift
let nc = NSNotificationCenter.default
nc.post(name:"didShow", object: nil, userInfo: ["data":"test"])
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

#### Objective-C

```Objective-C
[[NSNotificationCenter defaultCenter] addObserverForName:@"test.event"
                                                  object:nil
                                                   queue:[NSOperationQueue mainQueue]
                                              usingBlock:^(NSNotification *notification) {
                                                      NSLog(@"Handled 'test.event' [%@]", notification.userInfo[@"item"]);
                                                    }];
```

#### Swift 2.2

```swift
let nc = NSNotificationCenter.defaultCenter()
nc.addObserverForName("test.event",
               object: nil,
               queue:nil ) {
  notification in
    print( "\(notification.userInfo)")
}

```

#### Swift 3.0

```swift
let nc = NotificationCenter.default
nc.addObserver(forName:Notification.Name(rawValue:"test.event"),
               object:nil, queue:nil) {
  notification in
  print( "\(notification.userInfo)")
}
```


### ANDROID

```Java
final BroadcastReceiver receiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            final JSONObject data = new JSONObject( intent.getExtras().getString("userdata"));

            Log.d("CDVBroadcaster",
                    String.format("Native event [%s] received with data [%s]", intent.getAction(), String.valueOf(data)));

        } catch (JSONException e) {
           throw new RuntimeException(e);
        }
    }
};

LocalBroadcastManager.getInstance(this)
            .registerReceiver(receiver, new IntentFilter("test.event"));
}
```
