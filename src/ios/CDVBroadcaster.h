/********* CDVbroadcaster.m Cordova Plugin Implementation *******/

#import <Cordova/CDV.h>

@interface CDVBroadcaster : CDVPlugin

- (void)fireNativeEvent:(CDVInvokedUrlCommand*)command;
@end

