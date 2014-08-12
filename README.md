baidupush-phonegap-plugin
=========================

百度推送phonegap插件


BaiduPush Plugin

首页>帮助文档首页>云服务>云推送
http://developer.baidu.com/wiki/index.php?title=docs/cplat/push

使用方法
	一、安装Phongap或Cordova（http://cordova.apache.org/docs/en/edge/guide_cli_index.md.html#The%20Command-Line%20Interface），使用添加插件命令 cordova plugin add https://github.com/rocleegithub/baidupush-phonegap-plugin
	二、手动步骤
		1、请将工程的 Application 类继承 FrontiaApplication 类，在 onCreate 函数中加上:
			super.onCreate()，否则会崩溃;
			另外一种方法是：在自定义 Application 的 onCreate 方法中调用 Push 的接口：
			FrontiaApplication.initFrontia(Context context)，否则 push 的接口无法使用。

			在自定义 Application 中进行初始化调用，有三种方法：
				a.  (推荐使用方法，最简单)  直接在 AndroidManifest.xml 中指定 Application 的 android:name 属
				性值为 FrontiaApplication 类。
					<application android:name="com.baidu.frontia.FrontiaApplication">
					<!--  其它的略去-- >
					</application>
				b.（PushDemo 所示方法）请将工程的 Application 类继承 FrontiaApplication 类，在 onCreate 函数
			中加上: super.onCreate()，否则会崩溃，示例如下:
					import com.baidu.frontia.FrontiaApplication;
					public class DemoApplication extends FrontiaApplication {
						@Override
						public void onCreate() {
							//必须加上这一句，否则会崩溃
							super.onCreate();
						}
					}
					同时还需要在  AndroidManifest 文件中的 Application 标签中指定 android:name 属性值为该
			Application。如下：
					<application android:name="com.baidu.push.example.DemoApplication"
						android:icon="@drawable/ic_launcher"
						android:label="@string/app_name"、>
						<!-- ……..  其它的略去-- >
					</application>
				c.  在自定义 Application 的 onCreate 方法中调用 Push 的接口：
					FrontiaApplication.initFrontiaApplication(Context context)


		2、参数申请及权限开通,获取应用 ID 及 API Key 
			开发者需要使用百度账号登录百度开发者中心注册成为百度开发者并创建应用，方可获取应用
			ID、对应的 API Key 及 Secret Key 等信息。具体信息，请参考百度开发者中心上的“创建应用”的相关
			介绍。
			其中，应用 ID（即：APP ID）用于标识开发者创建的应用程序；API Key（即：Client_id）是开
			发者创建的应用程序的唯一标识，开发者在调用百度 API 时必须传入此参数。

			<application ....>
			<meta-data android:name="api_key" android:value="API Key" />
			</application>
		3、设置LAUNCHER activity android:launchMode="singleTask",已保证点击通知栏以单任务启动一个Activity，以保证已运行的状态不必重启启动

