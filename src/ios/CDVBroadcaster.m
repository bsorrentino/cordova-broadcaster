/********* CDVbroadcaster.m Cordova Plugin Implementation *******/

#import "CDVBroadcaster.h"

@interface CDVBroadcaster () {
  // Member variables go here.
}

@end

@implementation CDVBroadcaster


- (void)fireNativeEvent:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;
    NSString* eventName = [command.arguments objectAtIndex:0];

    if (eventName == nil || [eventName length] == 0) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"eventName is null or empty"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }

    id data = [command.arguments objectAtIndex:1];

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
