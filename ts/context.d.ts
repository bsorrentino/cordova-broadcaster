declare interface Context {
    hook:string,
    opts:{
      cordova:{
          platforms: [string],
          plugins: [string],
          version: string
        },
       plugin:any,
       nohooks:any,
       projectRoot:string
     },
    cmdLine: string,
    cordova:{
       binname: any,
       on:Function,
       off: Function,
       removeListener: Function,
       removeAllListeners: Function,
       emit: Function,
       trigger: Function,
       raw:Object,
       findProjectRoot: Function,
       prepare: Function,
       build: Function,
       create: Function,
       emulate: Function,
       plugin: Function,
       plugins: Function,
       platform: Function,
       platforms: Function,
       compile: Function,
       run: Function,
       info: Function,
       targets: Function,
       requirements: Function,
       projectMetadata: Function,
       clean: Function
     },
    scriptLocation: string
}
