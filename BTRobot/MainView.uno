using Uno;
using Uno.Threading;

using Fuse;

using Fuse.Bluetooth;

public partial class MainView
{

	public MainView()
	{
		InitializeUX();

		if defined(ANDROID)
		{
			BluetoothManager.Init();
		}
		

	}

}
