    /* cordova is available under *either* the terms of the modified BSD license *or* the
    * MIT License (2008). See http://opensource.org/licenses/alphabetical for full text.
        *
        * Copyright (c) Roc Lee 2014
        * QQ:304594656 Email:y1peng@126.com
    * Copyright (c) 2014, enmuo Corporation
    */



    var exec = require("cordova/exec"),channel = require('cordova/channel');

//    channel.createSticky('onCordovaPushServiceReady');
//// Tell cordova channel to wait on the onCordovaPushServiceReady event
//    channel.waitForInitialization('onCordovaPushServiceReady');
//    channel.onCordovaReady.subscribe(function() {
//        try{
//            baiduPushService.call_native("init",null,function(){
//                channel.onCordovaPushServiceReady.fire();
//            });
//        }
//        catch(exception){
//            console.log(exception);
//        }
//    });

    function addCallbackManage(instance, name, fn){
        fns = instance.callbackManage[name]
        !fns && (fns=[], fns.push(fn));
    }
    function doCallbackManage(instance, name, data){
        var fns = instance.callbackManage[name];
        for (var i in fns) {
            fns[i] && fns[i](data);
        };
    }

    var BaiduPushService = function(){
    };

    BaiduPushService.prototype.isPlatformIOS = function(){
        return device.platform == "iPhone" || device.platform == "iPad" || device.platform == "iPod touch" || device.platform == "iOS"
    };

    BaiduPushService.prototype.error_callback = function(msg){
        console.log("Javascript Callback Error: " + msg)
    };
    BaiduPushService.prototype.callbackManage={};
    BaiduPushService.prototype.call_native = function(name, args, callback){

        var ret = exec(callback,this.error_callback,'BaiduPush',name,!args?[]:args);
        return ret;
    };
    BaiduPushService.prototype.init = function(callback){

        try{
            this.call_native("init", null, null);
            addCallbackManage(this, "onBind", callback);
        }
        catch(exception){
            console.log(exception);
        }
    };
    BaiduPushService.prototype.delTags = function(data,callback){

        try{
            this.call_native("delTags",[data],null);
            addCallbackManage(this,"onDelTags",callback);
        }
        catch(exception){
            console.log(exception);
        }
    };
    BaiduPushService.prototype.setTags = function(data,callback) {

        try {
            this.call_native("setTags", [data], null);
            addCallbackManage(this, "onSetTags", callback);
        }
        catch (exception) {
            console.log(exception);
        }
    };
    BaiduPushService.prototype.listTags = function(callback){

        try{
            this.call_native("listTags",null,null);
            addCallbackManage(this,"onListTags",callback);
        }
        catch(exception){
            console.log(exception);
        }
    };

    BaiduPushService.prototype.stopWork = function(callback){

        try{
            this.call_native("stopWork",null,null);
            addCallbackManage(this,"onUnbind",callback);
        }
        catch(exception){
            console.log(exception);
        }
    };
    BaiduPushService.prototype.resumeWork = function(){

        try{
            this.call_native("resumeWork",null,null);
        }
        catch(exception){
            console.log(exception);
        }
    };
    BaiduPushService.prototype.isPushEnabled = function(callback){

        try{
            this.call_native("isPushEnabled",null,callback)
        }
        catch(exception){
            console.log(exception);
        }
    };
    BaiduPushService.prototype.enableLbs = function(){

        try{
            this.call_native("enableLbs",null,null);
        }
        catch(exception){
            console.log(exception);
        }
    };
    BaiduPushService.prototype.disableLbs = function(){

        try{
            this.call_native("disableLbs",null,null);
        }
        catch(exception){
            console.log(exception);
        }
    };
    BaiduPushService.prototype.clearNotification = function(){

        try{
            this.call_native("clearNotification",null,null);
        }
        catch(exception){
            console.log(exception);
        }
    };
    BaiduPushService.prototype.enableDebugMode = function(enable){

        try{
            this.call_native("enableDebugMode",[enable],null);
        }
        catch(exception){
            console.log(exception);
        }
    };

    BaiduPushService.prototype.regOnMessage = function(callback){
        addCallbackManage(this,"onMessage",callback);
    };
    BaiduPushService.prototype.regOnNotification = function(callback){
        addCallbackManage(this,"onNotification",callback);
    };
    BaiduPushService.prototype.log = function(data){
        console.log("baidupush.requestcallback--data:"+JSON.stringify( data._pushbackdata));
    };
    var baiduPushService = new BaiduPushService();
    module.exports = baiduPushService;

    /*
     *
     * 参数 operation :onUnbind onMessage onNotification onSetTags onDelTags onListTags onUnbind
     * document.addEventListener("baidupush.requestcallback", function(data){}, false);
     * */
    document.addEventListener("baidupush.requestcallback", function(data){
        try{
            baiduPushService.log(data._pushbackdata);
            var bToObj = data._pushbackdata;
            if (bToObj.operation) {
                switch (bToObj.operation) {
                    case "onUnbind":
                    case "onMessage":
                    case "onNotification":
                    case "onSetTags":
                    case "onDelTags":
                    case "onListTags":
                    case "onBind":
                        doCallbackManage(baiduPushService,bToObj.operation,bToObj)
                        break;
                    default:
                        break;
                }
            }

        }
        catch(exception){
            console.log("baidupush.requestcallback:"+exception);
        }

    }, false);

