cordova.define('cordova/plugin_list', function(require, exports, module) {
module.exports = [
    {
        "file": "plugins/org.apache.cordova.device/www/device.js",
        "id": "org.apache.cordova.device.device",
        "clobbers": [
            "device"
        ]
    },
    {
        "file": "plugins/org.apache.cordova.console/www/console-via-logger.js",
        "id": "org.apache.cordova.console.console",
        "clobbers": [
            "console"
        ]
    },
    {
        "file": "plugins/org.apache.cordova.console/www/logger.js",
        "id": "org.apache.cordova.console.logger",
        "clobbers": [
            "cordova.logger"
        ]
    },
    {
        "file": "plugins/org.bsc.cordova.broadcaster/www/broadcaster.js",
        "id": "org.bsc.cordova.broadcaster.broadcaster",
        "clobbers": [
            "broadcaster"
        ]
    }
];
module.exports.metadata = 
// TOP OF METADATA
{
    "org.apache.cordova.device": "0.2.13-dev",
    "org.apache.cordova.console": "0.2.11",
    "org.bsc.cordova.broadcaster": "1.0.0-dev"
}
// BOTTOM OF METADATA
});