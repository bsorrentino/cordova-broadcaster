#!/usr/bin/env node
"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
var fs = require("fs");
var path = require("path");
function copyFileSync(source, target) {
    var targetFile = target;
    if (fs.existsSync(target)) {
        if (fs.lstatSync(target).isDirectory()) {
            targetFile = path.join(target, path.basename(source));
        }
    }
    fs.writeFileSync(targetFile, fs.readFileSync(source));
}
module.exports = function (context) {
    var plugins = context.opts.cordova.plugins.filter(function (p) { return p === "cordova-plugin-broadcaster"; });
    if (plugins.length === 0)
        return;
    var platforms = context.opts.cordova.platforms.filter(function (p) { return p === "android"; });
    if (platforms.length === 0)
        return;
    var rel = path.join('org', 'bsc', 'broadcasterApp');
    var source = path.join(context.opts.projectRoot, "android-assets", 'src', rel, 'MainActivity.java');
    var target = path.join(context.opts.projectRoot, "platforms", platforms[0], 'app', 'src', 'main', 'java', rel);
    console.log("copy\n", source, "\nto\n", target);
    copyFileSync(source, target);
};
