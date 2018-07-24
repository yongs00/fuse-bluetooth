package kr.co.riv.bluetooth;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;
import android.util.Log;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

public class BluetoothControl extends AppCompatActivity {

    private Context context;
    private Activity activity;
    //BluetoothAdapter
    BluetoothAdapter mBluetoothAdapter;

    //블루투스 요청 액티비티 코드
    final static int BLUETOOTH_REQUEST_CODE = 100;

    static final private String SERIAL_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    private BluetoothSocket mBluetoothSocket;
	private UUID mUUID = UUID.fromString(SERIAL_UUID);
	private AtomicBoolean mIsConnection = new AtomicBoolean(false);
	private ExecutorService mReadExecutor; 
	private ExecutorService mWriteExecutor;
    private ExecutorService mHanlerExecutor;
	private Handler mMainHandler = new Handler(Looper.getMainLooper());
	private BluetoothDevice mConnectedDevice = null;
	private InputStream mInputStream;
	private OutputStream mOutputStream;

     //list - Device 목록 저장
    List<Map<String,String>> dataPaired;
    List<Map<String,String>> dataDevice;
    List<BluetoothDevice> bluetoothDevices; //검색된 디바이스 목록
    Set<BluetoothDevice> pairedDevices; //검색된 디바이스 목록
    int selectDevice;

    

    private static final int PERMISSIONS = 1;

    public static final  int DEVICE_SCAN = 10;
    public static final  int DEVICE_CONNECT = 20;
    Handler mHandler;

     public BluetoothControl(Activity activity, Handler handler) {
        this.context = activity;
        this.activity = activity;
        this.mHandler = handler;
    }

   
    public void init() {
   
        dataDevice = new ArrayList<>();
        dataPaired = new ArrayList<>();
        
        //검색된 블루투스 디바이스 데이터
        bluetoothDevices = new ArrayList<>();
        //선택한 디바이스 없음
        selectDevice = -1;

        //블루투스 지원 유무 확인
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //블루투스를 지원하지 않으면 null을 리턴한다
        if(mBluetoothAdapter == null){
            Toast.makeText(context, "블루투스를 지원하지 않는 단말기 입니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mReadExecutor = Executors.newSingleThreadExecutor();
		mWriteExecutor = Executors.newSingleThreadExecutor();
        mHanlerExecutor = Executors.newSingleThreadExecutor();

        //블루투스 브로드캐스트 리시버 등록
        //리시버1
        IntentFilter stateFilter = new IntentFilter();
        stateFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED); //BluetoothAdapter.ACTION_STATE_CHANGED : 블루투스 상태변화 액션
        context.registerReceiver(mBluetoothStateReceiver, stateFilter);
        //리시버2
        IntentFilter searchFilter = new IntentFilter();
        searchFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED); //BluetoothAdapter.ACTION_DISCOVERY_STARTED : 블루투스 검색 시작
        searchFilter.addAction(BluetoothDevice.ACTION_FOUND); //BluetoothDevice.ACTION_FOUND : 블루투스 디바이스 찾음
        searchFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED); //BluetoothAdapter.ACTION_DISCOVERY_FINISHED : 블루투스 검색 종료
        searchFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        context.registerReceiver(mBluetoothSearchReceiver, searchFilter);
      

      //블루투스가 꺼져있으면 사용자에게 활성화 요청하기
        if(!mBluetoothAdapter.isEnabled()){
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(intent, BLUETOOTH_REQUEST_CODE);
        }

    }

    //블루투스 상태변화 BroadcastReceiver (현재는 로직 처리 안함)
    BroadcastReceiver mBluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //BluetoothAdapter.EXTRA_STATE : 블루투스의 현재상태 변화
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);

            //블루투스 활성화
            if(state == BluetoothAdapter.STATE_ON){
                //txtState.setText("블루투스 활성화");
            }
            //블루투스 활성화 중
            else if(state == BluetoothAdapter.STATE_TURNING_ON){
                //txtState.setText("블루투스 활성화 중...");
            }
            //블루투스 비활성화
            else if(state == BluetoothAdapter.STATE_OFF){
                //txtState.setText("블루투스 비활성화");
            }
            //블루투스 비활성화 중
            else if(state == BluetoothAdapter.STATE_TURNING_OFF){
                //txtState.setText("블루투스 비활성화 중...");
            }
        }
    };

    //블루투스 검색결과 BroadcastReceiver
    BroadcastReceiver mBluetoothSearchReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch(action){
                //블루투스 디바이스 검색 종료
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    dataDevice.clear();
                    bluetoothDevices.clear();
                    Toast.makeText(context, "블루투스 검색 시작", Toast.LENGTH_SHORT).show();
                    break;
                //블루투스 디바이스 찾음
                case BluetoothDevice.ACTION_FOUND:
                    //검색한 블루투스 디바이스의 객체를 구한다
                    //Toast.makeText(MainActivity.this, "블루투스 검색 됐다", Toast.LENGTH_SHORT).show();
                    final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    //데이터 저장
                    Map map = new HashMap();
                    map.put("name", device.getName()); //device.getName() : 블루투스 디바이스의 이름
                    map.put("address", device.getAddress()); //device.getAddress() : 블루투스 디바이스의 MAC 주소
                    dataDevice.add(map);
                   
                   // Toast.makeText(context, "블루투스 검색 :: " +  device.getName() + "/" +  device.getAddress(), Toast.LENGTH_SHORT).show();

                    //블루투스 디바이스 저장 
                    bluetoothDevices.add(device);

                    //검색된 디바이스 핸들러 처리
                    mHanlerExecutor.execute(new Runnable() {
 
                        @Override
                        public void run() {
                            try{
                                JSONObject jo = new JSONObject();
                                jo.put("NAME", device.getName());    //device.getName() : 블루투스 디바이스의 이름
                                jo.put("ADDRESS", device.getAddress());     //device.getAddress() : 블루투스 디바이스의 MAC 주소
                                
                                Message message = Message.obtain(mHandler, DEVICE_SCAN, jo.toString());
                                mHandler.sendMessage(message);
                            } catch(JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    break;
                //블루투스 디바이스 검색 종료
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    Toast.makeText(context, "블루투스 검색 종료", Toast.LENGTH_SHORT).show();
                    //btnSearch.setEnabled(true);
                    break;
                //블루투스 디바이스 페어링 상태 변화
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    BluetoothDevice paired = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if(paired.getBondState()==BluetoothDevice.BOND_BONDED){
                       //페어링 된 정보 이벤트 처리 해야하는데 아직 못함.
                        //데이터 저장
                       /* Map map2 = new HashMap();
                        map2.put("name", paired.getName()); //device.getName() : 블루투스 디바이스의 이름
                        map2.put("address", paired.getAddress()); //device.getAddress() : 블루투스 디바이스의 MAC 주소
                        dataPaired.add(map2);
                        */
                       
                    }
                    break;
            }
        }
    };

     @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case BLUETOOTH_REQUEST_CODE:
                //블루투스 활성화 승인
                if(resultCode == Activity.RESULT_OK){
                   //블루투스 활성화 시 로직 추가 
                }
                //블루투스 활성화 거절
                else{
                    Toast.makeText(this, "블루투스를 활성화해야 합니다.", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                break;
        }
    }


    //블루투스 검색 버튼 클릭
    public void ScanStart(){
        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
        }
        mBluetoothAdapter.startDiscovery();
    }

     //디바이스 페어링
    public void pairingDevice(String addr){
        BluetoothDevice device = null;
        
        for(BluetoothDevice scanDevice : bluetoothDevices) {
            if(addr.equals(scanDevice.getAddress())) {
                device = scanDevice;
            }
        }

        if(device == null)
            return;

        try {
            //선택한 디바이스 페어링 요청
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
            //selectDevice = position;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //이미 페어링된 목록 가져오기
    public String GetListPairedDevice(){
        //Set<BluetoothDevice> pairedDevice = mBluetoothAdapter.getBondedDevices();
        pairedDevices  = mBluetoothAdapter.getBondedDevices();
        JSONArray arr = new JSONArray();
        try {
        
            if(pairedDevices.size() > 0){
                for(BluetoothDevice device : pairedDevices){
                    //데이터 저장
                    JSONObject jo = new JSONObject();
                    jo.put("NAME", device.getName());    //device.getName() : 블루투스 디바이스의 이름
                    jo.put("ADDRESS", device.getAddress());     //device.getAddress() : 블루투스 디바이스의 MAC 주소
                    arr.put(jo);
                }
            }

        } catch(JSONException e) {
            e.printStackTrace();
        }
        
        Log.d("getResults", arr.toString());

        return arr.toString();
    }
   
    //디바이스 연걸
	public boolean connect(final String addr) {
		if(!isEnabled()) return false;
        Log.d("addr", addr);
        for(BluetoothDevice device : pairedDevices) {
            if(addr.equals(device.getAddress())) {
                mConnectedDevice = device;
                
                Log.d("addr + device", addr + " / " + device.getAddress());
            }
        }

        Log.d("mConnectedDevice", mConnectedDevice.getAddress());

		if(isConnection()) {
			mWriteExecutor.execute(new Runnable() {
				@Override
				public void run() {
					try {
						mIsConnection.set(false);
						mBluetoothSocket.close();
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}  catch (IOException e) {
						e.printStackTrace();
					}
					connect(addr);
				}
			});
		} else {
            mIsConnection.set(true);
			connectClient();
		}
		return true;
	}

    /**
	 * 블루투스 디바이스와 연결 되어있는지를 가져온다. 
	 * @return true/false 
	 */
	public boolean isConnection() {
		return mIsConnection.get();
	}
	
	/**
	 * 연결된 블루투스 디바이스를 가져온다.
	 * @return 만약 연결된 블루투스 디바이스가 없다면 null.
	 */
	public BluetoothDevice getConnectedDevice() {
		return mConnectedDevice;
	}
	
    /**
	 * 블루투스가 사용 가능한 상태인지 확인.
	 * @return false 라면 블루투스가 off 된 상태거나 사용할 수 없다. 
	 */
	public boolean isEnabled() {
		return mBluetoothAdapter.isEnabled();
	}
	
	
	private void connectClient() {
        try {
			mBluetoothSocket = mConnectedDevice.createRfcommSocketToServiceRecord(mUUID);
		} catch (IOException e) {
			close();
			e.printStackTrace();
            connectResult("failed");
			return;
		}
		mWriteExecutor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					mBluetoothAdapter.cancelDiscovery();
					mBluetoothSocket.connect();
					manageConnectedSocket(mBluetoothSocket);
	            	//mReadExecutor.execute(mReadRunnable);

                    connectResult("connected");
	            } catch (final IOException e) {
	            	close();
	            	e.printStackTrace();
	            	mIsConnection.set(false);
                    connectResult("failed");
	            	try {
	                	mBluetoothSocket.close();
	                } catch (Exception ec) {
	                    ec.printStackTrace();
	                } 
	            }		            
			}
		});
	}

    private void connectResult(final String msg){
         mHanlerExecutor.execute(new Runnable() {
 
            @Override
            public void run() {
                
                Message message = Message.obtain(mHandler, DEVICE_CONNECT, msg);
                mHandler.sendMessage(message);
               
            }
        });
    }
	

	private void manageConnectedSocket(BluetoothSocket socket) throws IOException {
		mInputStream =  socket.getInputStream();
        mOutputStream = socket.getOutputStream();
	}
	
    //블루투스 소켓 통신 (보내기)
	public boolean write(String data) {
        data += '\0';
		final byte[] buffer = data.getBytes();

		if(!mIsConnection.get()) return false;  
		mWriteExecutor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					mOutputStream.write(buffer);
				} catch (Exception e) {
					close();
					e.printStackTrace();
				}
			}
		});
		return true;
	}

    private boolean close() {
		mConnectedDevice = null;
		if(mIsConnection.get()) {
			mIsConnection.set(false);
			try {
				mBluetoothSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			//mMainHandler.post(mCloseRunable);
			return true;
		}
		return false;
	}

    @Override
    protected void onDestroy() {
        unregisterReceiver(mBluetoothStateReceiver);
        unregisterReceiver(mBluetoothSearchReceiver);
        super.onDestroy();
    }
}
