package com.ece4600.mainapp;

import static java.util.UUID.fromString;
import java.io.File;
import java.util.UUID;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

public class bleService_pedo extends Service {
	private final static String TAG = bleService_pedo.class.getSimpleName();
	private final static String DEBUG = "DEBUG";
	private BluetoothManager mBluetoothManager1;

	protected BluetoothAdapter mBluetoothAdapter1;
	private BluetoothGatt mConnectedGatt3;

	private enum mSensorState {CONNECTED, DISCONNECTED};

	private mSensorState mSensor3;
	private BluetoothAdapter myBluetoothAdapter;

	private static final long SCAN_PERIOD = 2500; // Used to scan for devices
													// for only 10 secs

	Context context;
	private Handler handler = new Handler();

	// TI SensorTag device info
	private final String device3_MAC = "BC:6A:29:AB:61:CF";

	public dataArray[] array_1d = new dataArray[1];

	private String filePath, fileName;
	private int count;

	// --------------------------------------------------------------------
	// TI SensorTag UUIDs
	// --------------------------------------------------------------------
	public final static UUID 
    	UUID_IRT_SERV = fromString("f000aa00-0451-4000-b000-000000000000"),
    	UUID_IRT_DATA = fromString("f000aa01-0451-4000-b000-000000000000"),
    	UUID_IRT_CONF = fromString("f000aa02-0451-4000-b000-000000000000"), // 0: disable, 1: enable

    	UUID_ACC_SERV = fromString("f000aa10-0451-4000-b000-000000000000"),
    	UUID_ACC_DATA = fromString("f000aa11-0451-4000-b000-000000000000"),
    	UUID_ACC_CONF = fromString("f000aa12-0451-4000-b000-000000000000"), // 0: disable, 1: enable
    	UUID_ACC_PERI = fromString("f000aa13-0451-4000-b000-000000000000"), // Period in tens of milliseconds

    	UUID_HUM_SERV = fromString("f000aa20-0451-4000-b000-000000000000"),
    	UUID_HUM_DATA = fromString("f000aa21-0451-4000-b000-000000000000"),
    	UUID_HUM_CONF = fromString("f000aa22-0451-4000-b000-000000000000"), // 0: disable, 1: enable

    	UUID_MAG_SERV = fromString("f000aa30-0451-4000-b000-000000000000"),
    	UUID_MAG_DATA = fromString("f000aa31-0451-4000-b000-000000000000"),
    	UUID_MAG_CONF = fromString("f000aa32-0451-4000-b000-000000000000"), // 0: disable, 1: enable
    	UUID_MAG_PERI = fromString("f000aa33-0451-4000-b000-000000000000"), // Period in tens of milliseconds

    	UUID_BAR_SERV = fromString("f000aa40-0451-4000-b000-000000000000"), 
    	UUID_BAR_DATA = fromString("f000aa41-0451-4000-b000-000000000000"),
    	UUID_BAR_CONF = fromString("f000aa42-0451-4000-b000-000000000000"), // 0: disable, 1: enable
    	UUID_BAR_CALI = fromString("f000aa43-0451-4000-b000-000000000000"), // Calibration characteristic

    	UUID_GYR_SERV = fromString("f000aa50-0451-4000-b000-000000000000"), 
    	UUID_GYR_DATA = fromString("f000aa51-0451-4000-b000-000000000000"),
    	UUID_GYR_CONF = fromString("f000aa52-0451-4000-b000-000000000000"), // 0: disable, bit 0: enable x, bit 1: enable y, bit 2: enable z

    	UUID_KEY_SERV = fromString("0000ffe0-0000-1000-8000-00805f9b34fb"), 
    	UUID_KEY_DATA = fromString("0000ffe1-0000-1000-8000-00805f9b34fb"),
    	UUID_CCC_DESC = fromString("00002902-0000-1000-8000-00805f9b34fb");

	final static String MY_ACTION = "MY_ACTION"; // what does this do?

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO do something useful
		initialize();

		createAppFolder();
		myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		filePath = Environment.getExternalStorageDirectory() + "/G01Capstone/";

		Time now = new Time();
		now.setToNow();
		fileName = "DEVICES " + now.format("%Y-%m-%d %H-%M-%S") + ".txt";
		count = 0;

		mSensor3 = mSensorState.DISCONNECTED; // set the sensor indicator value
												// to disconnected
		MyThread myThread = new MyThread(); // creating a new thread?
		myThread.start();
		
		int state = myBluetoothAdapter.getState();
		if (state == 10){
			final Toast toast = Toast.makeText(bleService_pedo.this,"Bluetooth is OFF!", Toast.LENGTH_SHORT);
		    toast.show();
		    Handler handlerstop = new Handler();
		        handlerstop.postDelayed(new Runnable() {
		           @Override
		           public void run() {
		               toast.cancel(); 
		           }
		    }, 500);
		}

		// handler.postDelayed(test, 100);

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onCreate() {

	}

	@Override
	public void onDestroy() { // disconnects the sensortag connection after
								// quitting service
		mConnectedGatt3.disconnect();
		mBluetoothAdapter1.stopLeScan(mLeScanCallback3);
	}

	private void createAppFolder() {
		final String PATH = Environment.getExternalStorageDirectory() + "/G01Capstone/";
  		if(!(new File(PATH)).exists()) 
  		new File(PATH).mkdirs();
	}

	private boolean initialize() {
		// For API level 18 and above, get a reference to BluetoothAdapter
		// through
		// BluetoothManager.
		if (mBluetoothManager1 == null) { // not sure how the logic works here
			mBluetoothManager1 = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			if (mBluetoothManager1 == null) {
				Log.e(TAG, "Unable to initialize BluetoothManager.");
				return false;
			}
		}

		if (mBluetoothAdapter1 == null)
			mBluetoothAdapter1 = mBluetoothManager1.getAdapter();
		if (mBluetoothAdapter1 == null) {
			return false;
		}
		return true;
	}

	final class MyThread extends Thread {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			// mBleWrapper.startScanning();
			dataArray data = new dataArray(0.0f, 0.0f, 0.0f);
			array_1d[0] = data;
			startScan();
		}
	}

	// --------------------------
	// Start Scanning for devices
	// --------------------------
	private void startScan() {
		if (mSensor3 == mSensorState.DISCONNECTED)
			mBluetoothAdapter1.startLeScan(mLeScanCallback3);

		Log.i(DEBUG, "start scan");
		Handler h = new Handler(Looper.getMainLooper()); // handler to delay the
															// scan, if can't
															// connect, then
															// stop attempts to
															// scan
		h.postDelayed(mStopScanRunnable, SCAN_PERIOD);
	}

	private BluetoothAdapter.LeScanCallback mLeScanCallback3 = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
			Handler h = new Handler(Looper.getMainLooper());
			h.post(new Runnable() {
				@Override
				public void run() {
					Log.i("BLE", "New LE Device: " + device.getName() + " @ " + rssi + device.getAddress());
					if (device.getAddress().equals(device3_MAC) && (mSensor3 == mSensorState.DISCONNECTED)) {
						mConnectedGatt3 = device.connectGatt(context, false, mGattCallback3);
						stopScan();
					}
				}
			});
		}
	};

	// --------------------------
	// Stop scanning for devices
	// --------------------------
	public void stopScan() {
		Log.i(DEBUG, "Stop scan");
		mBluetoothAdapter1.stopLeScan(mLeScanCallback3);
	}

	private Runnable mStopScanRunnable = new Runnable() {
		@Override
		public void run() {
			stopScan();
		}
	};

	// --------------------------
	// GATT callback 1
	// --------------------------
	private BluetoothGattCallback mGattCallback3 = new BluetoothGattCallback() {

		/* What occurs once the device is connected: */
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status,
				int newState) {
			if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
				/*
				 * Once successfully connected, we must next discover all the
				 * services on the device before we can read and write their
				 * characteristics.
				 */
				Handler h = new Handler(Looper.getMainLooper());
				h.post(new Runnable() {
					@Override
					public void run() {
						Log.i(DEBUG, "Conntection successful, Getting Services");
						final Toast toast = Toast.makeText(bleService_pedo.this,"Device 3 connected", Toast.LENGTH_SHORT);
					    toast.show();
					    Handler handlerstop = new Handler();
					        handlerstop.postDelayed(new Runnable() {
					           @Override
					           public void run() {
					               toast.cancel(); 
					           }
					    }, 500);
					}
				});
				mSensor3 = mSensorState.CONNECTED;
				gatt.discoverServices();

			} else if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_DISCONNECTED) {
				mSensor3 = mSensorState.DISCONNECTED;
			} else if (status != BluetoothGatt.GATT_SUCCESS) {
				/*
				 * If there is a failure at any stage, simply disconnect
				 */
				gatt.disconnect();
			}
		}

		@Override
		/* New services connected */
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				Log.i(DEBUG, "onServicesDiscovered received: " + status);
			} else {
				Log.w(TAG, "onServicesDiscovered received: " + status);
			}

			enableSensor(gatt);
		}

		public void enableSensor(BluetoothGatt gatt) {
			BluetoothGattCharacteristic c;
			Log.i(DEBUG, "Enabling accelerometers");
			c = gatt.getService(UUID_ACC_SERV).getCharacteristic(UUID_ACC_CONF);
			c.setValue(new byte[] { 0x01 });

			gatt.writeCharacteristic(c);
		}

		/* Writing to a characteristic */
		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt,BluetoothGattCharacteristic characteristic, int status) {
			// After writing the enable flag, next we read the initial value
			Handler h = new Handler(Looper.getMainLooper());
			h.post(new Runnable() {
				@Override
				public void run() {
					// Log.i(DEBUG, "Connection successful, Getting Services");
					final Toast toast = Toast.makeText(bleService_pedo.this,"Device 3 accelerometers enabled", Toast.LENGTH_SHORT);
				    toast.show();
				    Handler handlerstop = new Handler();
				        handlerstop.postDelayed(new Runnable() {
				           @Override
				           public void run() {
				               toast.cancel(); 
				           }
				    }, 500);
					Intent dialogIntent = new Intent(bleService_pedo.this, MainActivity.class);
					dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					bleService_pedo.this.startActivity(dialogIntent);
				}
			});

			// readSensor(gatt);

			poll();
			// stopScan();
		}

		/* On characteristic read: */
		@Override
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic c, int status) {
			byte[] rawValue = c.getValue();
			String strValue = null;
			int intValue = 0;

			float[] vector;
			vector = new float[3];
			vector = sensorTag.getAccelerometerValue(c);
			dataArray data = new dataArray(vector[0], vector[1], vector[2]);
			array_1d[0] = data;
			// Log.d(DEBUG, "DEVICE 1 Accelerometer data:" + vector[0] + "," +
			// vector[1] + "," + vector[2] );
		}
	};// End of mGattCallback3

	// POLLING
	private void poll() {
		handler.postDelayed(runnable, 20);
	}
	
	private Runnable runnable = new Runnable() {
		   @Override
		   public void run() {
			  
			  readDevice3();
			  handler.postDelayed(this, 20);
		       
			  if (mSensor3 == mSensorState.CONNECTED){
	          String data = "D1," + array_1d[0].xaxis +  "," + array_1d[0].yaxis +  "," + array_1d[0].zaxis;
	          Log.i(DEBUG, data);
	          
	          Intent i = new Intent(bleService_pedo.this, PedometerService.class);
			  i.putExtra("X", (float) array_1d[0].xaxis);
			  i.putExtra("Y",(float) array_1d[0].yaxis);
			  i.putExtra("Z", (float) array_1d[0].zaxis);
			  startService(i);
			  		  
			  }
		   else{
			   handler.removeCallbacks(this);
			   stopScan();
			   stopSelf();
		   }
		};
	};

	private void readDevice3() {
		if (mSensor3 == mSensorState.CONNECTED) {
			BluetoothGattCharacteristic c;
			c = mConnectedGatt3.getService(UUID_ACC_SERV).getCharacteristic(UUID_ACC_DATA);
			mConnectedGatt3.readCharacteristic(c);
		}
	}

}