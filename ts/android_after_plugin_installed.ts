#!/usr/bin/env node

/// <reference path="context.d.ts"/>

import * as fs from "fs";
import * as path from "path";

function copyFileSync( source, target ) {

    var targetFile = target;

    //if target is a directory a new file with the same name will be created
    if ( fs.existsSync( target ) ) {
        if ( fs.lstatSync( target ).isDirectory() ) {
            targetFile = path.join( target, path.basename( source ) );
        }
    }

    fs.writeFileSync(targetFile, fs.readFileSync(source));
}

module.exports = function(context:Context) {

    //console.dir( context, {depth:3} );
    //console.log( "root", context.opts.projectRoot);

    let plugins =  context.opts.cordova.plugins.filter( (p) => p==="cordova-plugin-broadcaster" );
    if( plugins.length===0 ) return;

    let platforms = context.opts.cordova.platforms.filter( (p) => p==="android" );
    if( platforms.length === 0) return;

    let rel = path.join(  'org', 'bsc', 'broadcasterApp');

    let source = path.join( context.opts.projectRoot, "android-assets", 'src', rel, 'MainActivity.java' );
    let target = path.join( context.opts.projectRoot, "platforms", platforms[0], 'app', 'src', 'main', 'java', rel );

    console.log( "copy\n", source, "\nto\n", target);

    copyFileSync(source,target);




}
