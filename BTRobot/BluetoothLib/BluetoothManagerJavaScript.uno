using Uno;
using Uno.Collections;
using Fuse;
using Uno.UX;
using Fuse.Scripting;
using Fuse.Bluetooth.Android;

namespace Fuse.Bluetooth
{
	[UXGlobalModule]
	public class BluetoothJavaScript : NativeEventEmitterModule
	{
		static readonly BluetoothJavaScript _instance;
		
		public BluetoothJavaScript() : base(true, "deviceConnected", "scanDiscovered", "recevieData")
		{
			if(_instance != null) return;
			
			Resource.SetGlobalKey(_instance = this, "Fuse/Bluetooth");
			AddMember(new NativeFunction("scanStart", (NativeCallback)ScanStart));
			AddMember(new NativeFunction("getListPairedDevice", (NativeCallback)GetListPairedDevice));
			AddMember(new NativeFunction("pairingDevice", (NativeCallback)PairingDevice));
			AddMember(new NativeFunction("connect", (NativeCallback)Connect));
			AddMember(new NativeFunction("write", (NativeCallback)Write));
			
			var scanDiscovered = new NativeEvent("scanDiscovered");
			On("scanDiscovered", scanDiscovered);
			AddMember(scanDiscovered);
			
			var deviceConnected = new NativeEvent("deviceConnected");
			On("deviceConnected", deviceConnected);
			AddMember(deviceConnected);

			var recevieData = new NativeEvent("recevieData");
			On("recevieData", recevieData);
			AddMember(recevieData);
			
			
			if defined(ANDROID){
				//핸들러 처리 후 이벤트 처리 선언 (msdn 참조)
				BluetoothAndroid.onScanMessageEvent += new BluetoothAndroid.scanEventHandler(scanMessage);
				BluetoothAndroid.onConnectMessageEvent += new BluetoothAndroid.connectEventHandler(connectMessage);
				BluetoothAndroid.onRecevieDataEvent += new BluetoothAndroid.recevieDataHandler(recevieDataMessage);
			}
		}

		//디바이스 스캔
		static object ScanStart(Context context, object[] args)
		{
			debug_log("BluetoothJavaScript ScanStart");
			BluetoothManager.ScanStart();
			return null;
		}

		//페어링 된 디바이스 가져옴
		static object GetListPairedDevice(Context context, object[] args)
		{
			debug_log("BluetoothJavaScript  ScanStart");
			return 	BluetoothManager.GetListPairedDevice();
		}

		//스캔된 디바이스를 페어링 요청
		static object PairingDevice(Context context, object[] args)
		{
			debug_log("BluetoothJavaScript  PairingDevice");
			BluetoothManager.PairingDevice(args[0] as string);
			return 	null;
		}

		//페어링된 디바이스에 연결
		static object Connect(Context context, object[] args)
		{
			debug_log("BluetoothJavaScript  Connect");
			//var position = Marshal.ToInt(args[0]);
			return 	BluetoothManager.Connect(args[0] as string);
		}

		//블루투스 소켓통신 (Write)
		static object Write(Context context, object[] args)
		{
			debug_log("BluetoothJavaScript  Write");
			return 	BluetoothManager.Write(args[0] as string);
		}
		
		//디바이스 스캔 이벤트 화면단에 발생(1건 발견시 마다 발생)
		void OnScanDiscovered(string message)
		{
			debug_log("OnScanDiscovered : "+message);
			Emit("scanDiscovered", message);
		}

		//스캔 후 이벤트 핸들러 
		void scanMessage(MessageEventArgs e)
		{
			String msg = e.Message;

		    debug_log("scanMessage : "+msg);
			if (msg != null){
			    OnScanDiscovered(msg);
			}
		}

		//디바이스 연결 이벤트 화면단에 발생
		void deviceConnected(string message)
		{
			debug_log("deviceConnected : "+message);
			Emit("deviceConnected", message);
		}

		//연결 후 이벤트 핸들러 
		void connectMessage(MessageEventArgs e)
		{
			String msg = e.Message;

		    debug_log("connectMessage : "+msg);
			if (msg != null){
			    deviceConnected(msg);
			}
		}

		//연결 후 이벤트 핸들러 
		void recevieDataMessage(MessageEventArgs e)
		{
			String msg = e.Message;

		    debug_log("recevieDataMessage : "+msg);
			if (msg != null){
			  // Emit("recevieData", msg);
			}
		}
	}
}
