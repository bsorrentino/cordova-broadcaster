/********* CDVbroadcaster.m Cordova Plugin Implementation *******/

#import "CDVBroadcaster.h"


static inline void throwWithName( NSError *error, NSString* name )
{
    if (error) {
        @throw [NSException exceptionWithName:name
                                       reason:error.debugDescription
                                     userInfo:@{ @"NSError" : error }];
    }
}

@interface CDVBroadcaster () {
  // Member variables go here.
}

@property (nonatomic,strong) NSMutableDictionary *observerMap;

- (void)fireNativeEvent:(CDVInvokedUrlCommand*)command;
- (void)fireEvent:(NSString *)eventName data:(NSDictionary*)data;
- (void)addEventListener:(CDVInvokedUrlCommand*)command;
- (void)removeEventListener:(CDVInvokedUrlCommand*)command;

@end


@implementation CDVBroadcaster

- (void)dealloc
{
    
    for ( id observer in self.observerMap) {
    
        [[NSNotificationCenter defaultCenter] removeObserver:observer];
        
    }
    
    [_observerMap removeAllObjects];
    
    _observerMap = nil;
    
}

-(NSMutableDictionary *)observerMap
{
    if (!_observerMap) {
        _observerMap = [[NSMutableDictionary alloc] initWithCapacity:100];
    }
    
    return _observerMap;
}

- (void)fireEvent:(NSString *)eventName data:(NSDictionary*)data
{
    if (!self.commandDelegate ) {
        return;
    }
    
    if (eventName == nil || [eventName length] == 0) {
        
        @throw [NSException exceptionWithName:NSInvalidArgumentException
                                       reason:@"eventName is null or empty"
                                     userInfo:nil];

    }
    
    NSString *jsonDataString = @"{}";
    
    if( data  ) {
        
        NSError *error;
        NSData *jsonData = [NSJSONSerialization dataWithJSONObject:data
                                                           options:(NSJSONWritingOptions)0
                                                             error:&error];
        
        if (! jsonData) {

            throwWithName(error, @"JSON Serialization exception");
            return;
            
        }
        
        jsonDataString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
        

    }
    
    NSString *func = [NSString stringWithFormat:@"window.broadcaster.fireEvent('%@', %@);", eventName, jsonDataString];
    
    [self.commandDelegate evalJs:func];
    
    
}

- (void)addEventListener:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult;
    
    __block NSString* eventName = command.arguments[0];
    
    if (eventName == nil || [eventName length] == 0) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"eventName is null or empty"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }

    id observer = self.observerMap[eventName];
    
    if (!observer) {
        __typeof(self) __weak weakSelf = self;
        
        observer = [[NSNotificationCenter defaultCenter] addObserverForName:eventName
                                                                     object:nil
                                                                      queue:[NSOperationQueue mainQueue]
                                                                 usingBlock:^(NSNotification *note) {
                                                                     
                                                                     __typeof(self) __strong strongSelf = weakSelf;
                                                                     
                                                                     [strongSelf fireEvent:eventName data:note.userInfo];
                                                                     
                                                                 }];
        [self.observerMap setObject:observer forKey:eventName];
    }
    
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    
}


- (void)removeEventListener:(CDVInvokedUrlCommand*)command
{

    CDVPluginResult* pluginResult;

    __block NSString* eventName = command.arguments[0];
    
    if (eventName == nil || [eventName length] == 0) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"eventName is null or empty"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }
    
    id observer = self.observerMap[ eventName ];
    
    if (observer) {
        
        [[NSNotificationCenter defaultCenter] removeObserver:observer
                                                        name:eventName
                                                      object:self];
    }
    
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    
}



- (void)fireNativeEvent:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;
    NSString* eventName = command.arguments[0];

    if (eventName == nil || [eventName length] == 0) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"eventName is null or empty"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }

    id data = command.arguments[1];

    if (data!=nil && ![data isKindOfClass:[NSDictionary class]]) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"data is not a json object"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }

    [[NSNotificationCenter defaultCenter] postNotificationName:eventName object:self userInfo:data];

    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

@end
