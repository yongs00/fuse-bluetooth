using Uno;
using Uno.Permissions;
using Uno.Array;
using Uno.Threading;
using Uno.Compiler.ExportTargetInterop;
using Fuse;
using Fuse.Controls;
using Fuse.Triggers;
using Fuse.Resources;
using Android;
using Fuse.Bluetooth;

namespace Fuse.Bluetooth.Android
{
	[Require("AndroidManifest.Permission", "android.permission.BLUETOOTH")]
    [Require("AndroidManifest.Permission", "android.permission.BLUETOOTH_ADMIN")]
    [Require("AndroidManifest.Permission", "android.permission.ACCESS_FINE_LOCATION")]
    [Require("AndroidManifest.Permission", "android.permission.ACCESS_COARSE_LOCATION")]

	//[ForeignInclude(Language.Java, "kr.mobidoo.sonicgeneratorsdk.SonicGenerator")]
	//extern(ANDROID) public abstract class BluetoothAndroid { }

	//[ForeignInclude(Language.Java, "kr.co.riv.bluetooth.BluetoothControl", "android.util.Log")]
	/*[ForeignInclude(Language.Java, "kr.mobidoo.sonicgeneratorsdk.SonicGenerator",
		"com.mobidoo.generator.ServerConnect",
		"kr.mobidoo.sonicgeneratorsdk.NativeModule",
		"android.util.Log",
		"android.os.Bundle",
		"android.os.Message",
		"android.content.Intent", 
		"org.json.JSONObject",
		"android.os.Environment")]
        */
	[ForeignInclude(Language.Java, "kr.co.riv.bluetooth.BluetoothControl",
		"android.util.Log",
		"android.widget.Toast",
		"android.content.Context",
		"android.os.Handler",
		"android.os.Looper",
		"android.os.Message"
		)]

	//extern(ANDROID) public class BluetoothMgr : BluetoothAndroid
	extern(ANDROID) public class BluetoothAndroid
	{
		//
		static BluetoothAndroid()
		{
			var permissions = new PlatformPermission[]
			{
				Permissions.Android.ACCESS_FINE_LOCATION
                , Permissions.Android.ACCESS_COARSE_LOCATION
			};
			Permissions.Request(permissions).Then(OnPermissionsGranted, OnPermissionsRejected);
		}

		static void OnPermissionsGranted(PlatformPermission[] permissions)
		{
			debug_log("PREMISSIONS GRANTED!");
			
		}

		static void OnPermissionsRejected(Exception e)
		{
			debug_log("PREMISSIONS REJECTED: " + e.Message);
		}


		private static BluetoothAndroid instance;
		
		public static BluetoothAndroid Instance { 
			get { 
				if(instance == null){
					
					instance =  new BluetoothAndroid(); 

				}
				return instance;
			} 
		}

		static Java.Object _bluetoothAndroid;

		//생성자 함수
		BluetoothAndroid()
		{
			init();
		}

		[Foreign(Language.Java)]
		public void init() 
		@{
			//java단의 이벤트를 uno단에서 받아 처리하기 위해 핸들러 처리 
			Handler mHandler = new Handler(Looper.getMainLooper()) {
		        public void handleMessage(Message msg) {
				   	super.handleMessage(msg);

					switch (msg.what) {
		                case BluetoothControl.DEVICE_SCAN : //디바이스 스캔
							Log.d("BluetoothAndroid", (String)msg.obj);
							@{BluetoothAndroid.ScanEventMsg:Set((String)msg.obj)};
							break;
						case BluetoothControl.DEVICE_CONNECT : //디바이스 연결
							Log.d("BluetoothAndroid", (String)msg.obj);
							@{BluetoothAndroid.ConnectEventMsg:Set((String)msg.obj)};
							break;
						default:
		                    break;
		            }
				}
			};

			android.app.Activity a = (android.app.Activity) com.fuse.Activity.getRootActivity();
			BluetoothControl bleControl = new BluetoothControl(a, mHandler);
			bleControl.init();

			@{BluetoothAndroid._bluetoothAndroid:Set(bleControl)};
		@}


		[Foreign(Language.Java)]
		public bool ScanStart() 
		@{
            Log.d("Bluetooth","ScanStart  ");
			BluetoothControl bleControl = (BluetoothControl)@{BluetoothAndroid._bluetoothAndroid:Get()};
			bleControl.ScanStart();
        	
			return true;
		@}

		[Foreign(Language.Java)]
		public String GetListPairedDevice() 
		@{
            Log.d("Bluetooth","ScanStart  ");
			BluetoothControl bleControl = (BluetoothControl)@{BluetoothAndroid._bluetoothAndroid:Get()};

			return bleControl.GetListPairedDevice();
		@}

		[Foreign(Language.Java)]
		public void PairingDevice(String addr) 
		@{
            Log.d("Bluetooth","ScanStart  ");
			BluetoothControl bleControl = (BluetoothControl)@{BluetoothAndroid._bluetoothAndroid:Get()};
			
			bleControl.pairingDevice(addr);
		@}

		[Foreign(Language.Java)]
		public bool Connect(String addr) 
		@{
            Log.d("Bluetooth","Connect  ");
			BluetoothControl bleControl = (BluetoothControl)@{BluetoothAndroid._bluetoothAndroid:Get()};

			return bleControl.connect(addr);
		@}

		[Foreign(Language.Java)]
		public bool Write(String data) 
		@{
            Log.d("Bluetooth","ScanStart  ");
			BluetoothControl bleControl = (BluetoothControl)@{BluetoothAndroid._bluetoothAndroid:Get()};

			return bleControl.write(data);
		@}

		

		[Foreign(Language.Java)]
		public static void Toast(String txt)
		@{
			android.util.Log.d("Toast message #######", txt);
			android.app.Activity a = @(Activity.Package).@(Activity.Name).GetRootActivity();
			Context context = a;

			a.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					// This code will always run on the UI thread, therefore is safe to modify UI elements.
					android.app.Activity b = @(Activity.Package).@(Activity.Name).GetRootActivity();
					Toast toast = Toast.makeText(b, txt, Toast.LENGTH_SHORT);
					toast.show();
					//b.finish();
				}
			});
		@}

		//스캔이벤트 (Custom Event) 처리 (msdn참조)
		public delegate void scanEventHandler(MessageEventArgs myArgs);
        static String scanEventmsg; 

        public static event scanEventHandler onScanMessageEvent;

		static String ScanEventMsg
        {
        	set 
         	{
               	scanEventmsg = value;
            	MessageEventArgs myArgs = new MessageEventArgs(scanEventmsg);
               	onScanMessageEvent(myArgs);
            }
       	}	

		//연결이벤트 (Custom Event) 처리 (msdn참조)
		public delegate void connectEventHandler(MessageEventArgs myArgs);
        static String connectEventmsg; 

        public static event connectEventHandler onConnectMessageEvent;

		static String ConnectEventMsg
        {
        	set 
         	{
               	connectEventmsg = value;
            	MessageEventArgs myArgs = new MessageEventArgs(connectEventmsg);
               	onConnectMessageEvent(myArgs);
            }
       	}	

	}
}