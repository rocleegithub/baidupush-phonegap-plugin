package com.phonegap.plugins.baidupushservice;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.res.Resources;
import com.baidu.android.pushservice.CustomPushNotificationBuilder;
import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.baidu.android.pushservice.PushSettings;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Roc Lee on 2014/8/6.
 */
public class PushService extends CordovaPlugin {
    public static boolean DEVICE_READY = false;
    static int notificationBuilderId = 1;
    private static final String LOG_TAG = "PushService";
    private final static List<String> methodList =
            Arrays.asList(
                    "init",
                    "delTags",
                    "setTags",
                    "listTags",
                    "stopWork",
                    "resumeWork",
                    "isPushEnabled",
                    "enableLbs",
                    "disableLbs",
                    "enableDebugMode",
                    "clearNotification"
            );

    private ExecutorService threadPool = Executors.newFixedThreadPool(1);
    public static JSONObject notificationJsonData = null;

    private static PushService instance;

    public PushService() {
        instance = this;
        System.out.println("PushService 构造函数");
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        init(cordova);

    }

    void init(CordovaInterface cordova) {
        Resources resource = cordova.getActivity().getResources();
        String pkgName = cordova.getActivity().getPackageName();
        // Push: 以apikey的方式登录，一般放在主Activity的onCreate中。
        // 这里把apikey存放于manifest文件中，只是一种存放方式，
        // 您可以用自定义常量等其它方式实现，来替换参数中的Utils.getMetaValue(PushDemoActivity.this,
        // "api_key")
        // 通过share preference实现的绑定标志开关，如果已经成功绑定，就取消这次绑定
        if (!Utils.hasBind(cordova.getActivity().getApplicationContext())) {
            //！！ 请将AndroidManifest.xml 104行处 api_key 字段值修改为自己的 api_key 方可使用 ！！
            //！！ ATTENTION：You need to modify the value of api_key to your own at row 104 in AndroidManifest.xml to use this Demo !!
            PushManager.startWork(cordova.getActivity().getApplicationContext(),
                    PushConstants.LOGIN_TYPE_API_KEY,
                    Utils.getMetaValue(cordova.getActivity(), "api_key"));
            // Push: 如果想基于地理位置推送，可以打开支持地理位置的推送的开关
            // PushManager.enableLbs(getApplicationContext());
        }

        // Push: 设置自定义的通知样式，具体API介绍见用户手册，如果想使用系统默认的可以不加这段代码
        // 请在通知推送界面中，高级设置->通知栏样式->自定义样式，选中并且填写值：1，
        // 与下方代码中 PushManager.setNotificationBuilder(this, 1, cBuilder)中的第二个参数对应
        CustomPushNotificationBuilder cBuilder = new CustomPushNotificationBuilder(
                cordova.getActivity().getApplicationContext(), resource.getIdentifier(
                "notification_custom_builder", "layout", pkgName),
                resource.getIdentifier("notification_icon", "id", pkgName),
                resource.getIdentifier("notification_title", "id", pkgName),
                resource.getIdentifier("notification_text", "id", pkgName)
        );
        cBuilder.setNotificationFlags(Notification.FLAG_AUTO_CANCEL);
        cBuilder.setNotificationDefaults(Notification.DEFAULT_SOUND
                | Notification.DEFAULT_VIBRATE);
        cBuilder.setStatusbarIcon(cordova.getActivity().getApplicationInfo().icon);
        cBuilder.setLayoutDrawable(resource.getIdentifier(
                "simple_notification_icon", "drawable", pkgName));
        PushManager.setNotificationBuilder(cordova.getActivity(), notificationBuilderId, cBuilder);

        //callbackContext.success();

    }

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action          The action to execute.
     * @param args            JSONArray of arguments for the plugin.
     * @param callbackContext The callback context used when calling back into JavaScript.
     * @return True when the action was valid, false otherwise.
     */
    public boolean execute(final String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        DEVICE_READY = true;
        System.out.println("PushService:execute action:"+action);
        if (!methodList.contains(action)) {
            return false;
        }
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Method method = PushService.class.getDeclaredMethod(action,
                            JSONArray.class, CallbackContext.class);
                    method.invoke(PushService.this, args, callbackContext);
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }
        });
        return true;
    }

    static void requestcallback(JSONObject json) {
        System.out.println("requestcallback");
        if (instance == null) {
            return;
        }
        JSONObject obj = new JSONObject();
        try {
            obj.put("_pushbackdata", json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String jsEvent = String
                .format("cordova.fireDocumentEvent('baidupush.requestcallback',%s)",
                        obj);
        instance.webView.sendJavascript(jsEvent);
        System.out.println("sendJavascript");
    }

    void init(JSONArray data,
              CallbackContext callbackContext) {
        System.out.println("init");
        if (notificationJsonData != null) {
            System.out.println("notificationJsonData");
            requestcallback(notificationJsonData);
            notificationJsonData = null;
        }
        callbackContext.success();
    }

    // 删除tag操作
    void delTags(JSONArray data,
                 CallbackContext callbackContext) {

        HashSet<String> tags = null;
        try {
            String tagStr;
            if (data == null) {
                //tags=null;
            } else if (data.length() == 0) {
                tags = new HashSet<String>();
            } else {
                tagStr = data.getString(0);
                String[] tagArray = tagStr.split(",");
                for (String tag : tagArray) {
                    if (tags == null) {
                        tags = new HashSet<String>();
                    }
                    tags.add(tag);
                }
            }
            //Set<String> validTags = JPushInterface.filterValidTags(tags);
            PushManager.delTags(this.cordova.getActivity()
                    .getApplicationContext(), new ArrayList<String>(tags));
            callbackContext.success();
        } catch (JSONException e) {
            e.printStackTrace();
            callbackContext.error("Error reading tags JSON");
        }
    }

    // 设置标签,以英文逗号隔开
    void setTags(JSONArray data,
                 CallbackContext callbackContext) {

        HashSet<String> tags = null;
        try {
            String tagStr;
            if (data == null) {
                //tags=null;
            } else if (data.length() == 0) {
                tags = new HashSet<String>();
            } else {
                tagStr = data.getString(0);
                String[] tagArray = tagStr.split(",");
                for (String tag : tagArray) {
                    if (tags == null) {
                        tags = new HashSet<String>();
                    }
                    tags.add(tag);
                }
            }
            //Set<String> validTags = JPushInterface.filterValidTags(tags);
            PushManager.setTags(this.cordova.getActivity()
                    .getApplicationContext(), new ArrayList<String>(tags));
            callbackContext.success();
        } catch (JSONException e) {
            e.printStackTrace();
            callbackContext.error("Error reading tags JSON");
        }


    }

    void listTags(JSONArray data,
                  CallbackContext callbackContext) {

        PushManager.listTags(this.cordova.getActivity()
                .getApplicationContext());
        callbackContext.success();

    }

    void stopWork(JSONArray data,
                  CallbackContext callbackContext) {
        PushManager.stopWork(this.cordova.getActivity()
                .getApplicationContext());
        callbackContext.success();
    }

    void resumeWork(JSONArray data,
                    CallbackContext callbackContext) {
        PushManager.resumeWork(this.cordova.getActivity()
                .getApplicationContext());
        callbackContext.success();
    }

    void isPushEnabled(JSONArray data,
                       CallbackContext callbackContext) {

        callbackContext.success(PushManager.isPushEnabled(this.cordova.getActivity()
                .getApplicationContext()) ? "1" : "0");
    }

    void clearNotification(JSONArray data,
                           CallbackContext callbackContext) {
        NotificationManager notificationManager = (NotificationManager)
                cordova.getActivity().getSystemService(cordova.getActivity().NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        callbackContext.success(1);
    }

    void enableLbs(JSONArray data,
                   CallbackContext callbackContext) {
        PushManager.enableLbs(this.cordova.getActivity()
                .getApplicationContext());
        callbackContext.success();
    }

    void disableLbs(JSONArray data,
                    CallbackContext callbackContext) {

        PushManager.disableLbs(this.cordova.getActivity()
                .getApplicationContext());
        callbackContext.success();
    }

    void enableDebugMode(JSONArray data,
                         CallbackContext callbackContext) {
        boolean enable = false;
        if (data == null) {
            //tags=null;
        } else {
            try {
                enable = data.getBoolean(0);
                PushSettings.enableDebugMode(this.cordova.getActivity()
                        .getApplicationContext(), enable);
                callbackContext.success();
            } catch (JSONException e) {
                e.printStackTrace();
                callbackContext.error("Error reading tags JSON");
            }
        }

    }

}
