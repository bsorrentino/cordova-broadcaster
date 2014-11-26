Cordova Broadcaster
=====================

Cordova Plugin to allow message exchange between javascript and native (and viceversa)

IOS
===

> Providing bridge to **[NotificationCenter](https://developer.apple.com/library/mac/documentation/Cocoa/Reference/Foundation/Classes/NSNotificationCenter_Class/index.html#//apple_ref/occ/instm/NSNotificationCenter/addObserverForName%3aobject%3aqueue%3ausingBlock%3a)**


Android
=======

> Providing bridge to **[LocalBroadcastManager](http://developer.android.com/reference/android/support/v4/content/LocalBroadcastManager.html)**


INSTALL
========

 ```javascript
 # cordova create <PATH> [ID [NAME [CONFIG]]] [options]
 # cd <PATH>
 # cordova platform add [ios|android]
 # cordova plugin add https://github.com/bsorrentino/cordova-broadcaster.git
 ```

USAGE:
======

## From Native to Javascript

### Javascript

```javascript
    console.log( "register didShow received!" );
    window.broadcaster.addEventListener( "didShow", function( e ) {

                console.log( "didShow received!" );
    });
```

### IOS

```Objective-C
    [[NSNotificationCenter defaultCenter] postNotificationName:@"didShow"
                                                        object:nil
                                                      userInfo:@{ @"data":@"test"}];
```

### ANDROID

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

                                                      NSLog(@"Handled 'test.event' [%@]", note);

                                                    }];
```

### ANDROID
