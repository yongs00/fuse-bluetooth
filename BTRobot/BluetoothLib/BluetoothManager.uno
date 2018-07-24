using Uno;
using Uno.Collections;
using Fuse;
using Fuse.Bluetooth.Android;

namespace Fuse.Bluetooth
{
	
	public class BluetoothManager
	{
		
		//초기화
		public static void Init()
		{
			if defined(ANDROID)
			{
				BluetoothAndroid mgr =  BluetoothAndroid.Instance;
			}
			else
			{
				debug_log("Not supported yet");
			}

		}

		//디바이스 스캔
		public static void ScanStart()
		{
			if defined(ANDROID)
			{
				debug_log("Scan Start");
				BluetoothAndroid mgr =  BluetoothAndroid.Instance;
				mgr.ScanStart();
				
			}
			else
			{
				debug_log("Not supported yet");
			}

		}

		//페어링 된 디바이스 가져옴
		public static String GetListPairedDevice()
		{
			if defined(ANDROID)
			{
				BluetoothAndroid mgr =  BluetoothAndroid.Instance;
				return mgr.GetListPairedDevice();
			}
			else
			{
				debug_log("Not supported yet");
				return null;
			}

		}

		//스캔된 디바이스를 페어링 요청
		public static void PairingDevice(String addr)
		{
			if defined(ANDROID)
			{
				BluetoothAndroid mgr =  BluetoothAndroid.Instance;
				mgr.PairingDevice(addr);
			}
			else
			{
				debug_log("Not supported yet");
				//return false;
			}

		}

		//페어링된 디바이스에 연결
		public static bool Connect(String addr)
		{
			if defined(ANDROID)
			{
				BluetoothAndroid mgr =  BluetoothAndroid.Instance;
				return mgr.Connect(addr);
			}
			else
			{
				debug_log("Not supported yet");
				return false;
			}

		}

		//블루투스 소켓통신 (Write)
		public static bool Write(String data)
		{
			if defined(ANDROID)
			{
				BluetoothAndroid mgr =  BluetoothAndroid.Instance;
				return mgr.Write(data);
			}
			else
			{
				debug_log("Not supported yet");
				return false;
			}

		}

		
	}
}
