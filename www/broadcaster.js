"use strict";
var exec = require('cordova/exec');
var channel = require('cordova/channel');
var Broadcaster = /** @class */ (function () {
    function Broadcaster() {
        var _this = this;
        this._debug = false;
        this._channels = {};
        this._channelCreate = function (c) {
            if (_this._debug)
                console.log("CHANNEL " + c + " CREATED! ");
            _this._channels[c] = channel.create(c);
        };
        this._channelDelete = function (c) {
            delete _this._channels[c];
            if (_this._debug)
                console.log("CHANNEL " + c + " DELETED! ");
        };
        this._channelSubscribe = function (c, f) {
            var channel = _this._channels[c];
            channel.subscribe(f);
            if (_this._debug)
                console.log("CHANNEL " + c + " SUBSCRIBED! " + channel.numHandlers);
            return channel.numHandlers;
        };
        this._channelUnsubscribe = function (c, f) {
            var channel = _this._channels[c];
            channel.unsubscribe(f);
            if (_this._debug)
                console.log("CHANNEL " + c + " UNSUBSCRIBED! " + channel.numHandlers);
            return channel.numHandlers;
        };
        this._channelFire = function (event) {
            if (_this._debug)
                console.log("CHANNEL " + event.type + " FIRED! ");
            _this._channels[event.type].fire(event);
        };
        this._channelExists = function (c) {
            return _this._channels.hasOwnProperty(c);
        };
    }
    /**
     * fire native evet
     *
     * @param type
     * @param globalFlagOrData
     * @param data
     * @param success
     * @param error
     */
    Broadcaster.prototype.fireNativeEvent = function (type, globalFlagOrData, data, success, error) {
        var isGlobal = false;
        var oData = null;
        if (typeof globalFlagOrData === 'boolean') {
            isGlobal = globalFlagOrData;
            oData = data !== null && data !== void 0 ? data : null;
        }
        else if (typeof globalFlagOrData === 'object') {
            oData = globalFlagOrData;
        }
        //function instanceOfAndroidData( object:any ): object is AndroidData {
        //  return ( 'extras' in object && 'flags' in object && 'category' in object )
        //}
        //if( oData!=null && this._instanceOfAndroidData(oData) ) {
        //  return exec(success, error, "broadcaster", "fireNativeEvent", [ type, oData.extras, isGlobal, oData.flags, oData.category ]);
        //}
        exec(success, error, "broadcaster", "fireNativeEvent", [type, oData, isGlobal]);
    };
    /**
     * fire local event
     *
     * @param type
     * @param data
     */
    Broadcaster.prototype.fireEvent = function (type, data) {
        if (!this._channelExists(type))
            return;
        var event = document.createEvent('Event');
        event.initEvent(type, false, false);
        if (data) {
            for (var i in data) {
                if (data.hasOwnProperty(i)) {
                    event[i] = data[i];
                }
            }
        }
        this._channelFire(event);
    };
    /**
     * add a listener
     *
     * @param eventname
     * @param globalFlagOrListener
     * @param listener
     */
    Broadcaster.prototype.addEventListener = function (eventname, globalFlagOrListener, listener) {
        var _this = this;
        var isGlobal = false;
        var f = function () { };
        if (typeof globalFlagOrListener === 'boolean') {
            isGlobal = globalFlagOrListener;
            if (!listener)
                throw "listener must be defined!";
            f = listener;
        }
        else if (typeof globalFlagOrListener === 'function') {
            f = globalFlagOrListener;
        }
        if (!this._channelExists(eventname)) {
            this._channelCreate(eventname);
            exec(function () { return _this._channelSubscribe(eventname, f); }, function (err) { return console.log("ERROR addEventListener: ", err); }, "broadcaster", "addEventListener", [eventname, isGlobal]);
        }
        else {
            this._channelSubscribe(eventname, f);
        }
    };
    /**
     * remove a listener
     *
     * @param eventname
     * @param listener
     */
    Broadcaster.prototype.removeEventListener = function (eventname, listener) {
        var _this = this;
        if (this._channelExists(eventname)) {
            if (this._channelUnsubscribe(eventname, listener) === 0) {
                exec(function () { return _this._channelDelete(eventname); }, function (err) { return console.log("ERROR removeEventListener: ", err); }, "broadcaster", "removeEventListener", [eventname]);
            }
        }
    };
    return Broadcaster;
}());
module.exports = new Broadcaster();
