/********* CDVbroadcaster.m Cordova Plugin Implementation *******/

#import <Cordova/CDV.h>

@interface CDVBroadcaster : CDVPlugin {
  // Member variables go here.
}

- (void)fireEvent:(CDVInvokedUrlCommand*)command;
@end

@implementation CDVBroadcaster

- (void)fireEvent:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;
    NSString* echo = [command.arguments objectAtIndex:0];

    if (echo != nil && [echo length] > 0) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:echo];
    } else {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    }

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

@end
