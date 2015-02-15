package com.ece4600.mainapp;


import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Pedometer extends Activity{
	
	private TextView currentX, currentY, currentZ, maxX, maxY, maxZ, step, speed, steppre;
	Button reset, returnbutton, start, stop;
	private int stepnum = 0, stepdetect = 0, stepthres = 0;
	private double speednum = 0;
	private long timedetect = 0, timeSecondsstart = 0, timestart = 0, timeSecondsstop = 0, timestop = 0;
	private boolean startflag = false;
	private BluetoothAdapter myBluetoothAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pedometer);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		initializeViews();
		reset = (Button) findViewById(R.id.pedo_reset);
		returnbutton = (Button) findViewById(R.id.returnpedo);
		start = (Button) findViewById(R.id.pedo_start);
		stop = (Button) findViewById(R.id.pedo_stop);
		myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		bluetoothTest();
		
		IntentFilter intentFilter = new IntentFilter("PEDOMETER_EVENT");
        registerReceiver(broadcastRx, intentFilter);

	}

	public void initializeViews() {
		currentX = (TextView) findViewById(R.id.pedo_xaxisdata);
		currentY = (TextView) findViewById(R.id.pedo_yaxisdata);
		currentZ = (TextView) findViewById(R.id.pedo_zaxisdata);
		maxX = (TextView) findViewById(R.id.pedo_accxmax);
		maxY = (TextView) findViewById(R.id.pedo_accymax);
		maxZ = (TextView) findViewById(R.id.pedo_acczmax);
		step = (TextView) findViewById(R.id.pedo_stepnum);
		speed = (TextView) findViewById(R.id.pedo_speednum);
		steppre = (TextView) findViewById(R.id.pedo_prenum);
	}
	
	public void bluetoothTest(){
		int state = myBluetoothAdapter.getState();
		if (state == 10){
			AlertDialog.Builder alertDialogHint = new AlertDialog.Builder(this);
			alertDialogHint.setMessage("Bluetooth is OFF! Connection Fail!");
			alertDialogHint.setPositiveButton("Bluetooth Setting",
			new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent i = new Intent(Pedometer.this,Bluetooth.class);
					startActivity(i);
					finish();
				}
			});
			alertDialogHint.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			AlertDialog alertDialog = alertDialogHint.create();
			alertDialog.show();
		}
	}

	 @Override
	 protected void onDestroy() {
	  super.onDestroy();
	  //un-register BroadcastReceiver
	  unregisterReceiver(broadcastRx);
	 }

	

	@Override
	
	protected void onResume() {
	super.onResume();
	
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction("PEDOMETER_ACTION");
    registerReceiver(broadcastRx, intentFilter);
	}
	
	@Override
	protected void onPause() {
	super.onPause();

    LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);   
    bManager.unregisterReceiver(broadcastRx);
	}

	

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.pedo_start:
			onResume();
			timestart = System.currentTimeMillis();
			timeSecondsstart = TimeUnit.MILLISECONDS.toSeconds(timestart);
			//countdowndisplay();
			stepthres = stepnum;
			speednum = 0;
			startflag = true;
			final Toast toast = Toast.makeText(getApplicationContext(), "Step Detection Start", Toast.LENGTH_SHORT);
		    toast.show();
		    Handler handler = new Handler();
		        handler.postDelayed(new Runnable() {
		           @Override
		           public void run() {
		               toast.cancel(); 
		           }
		    }, 500);
			break;
		case R.id.pedo_stop:
			onPause();
			startflag = false;
			final Toast toaststop = Toast.makeText(getApplicationContext(), "Step Detection Stop", Toast.LENGTH_SHORT);
		    toaststop.show();
		    Handler handlerstop = new Handler();
		        handlerstop.postDelayed(new Runnable() {
		           @Override
		           public void run() {
		               toaststop.cancel(); 
		           }
		    }, 500);
			break;
		case R.id.returnpedo:
			startActivity(new Intent(Pedometer.this, MainActivity.class));
			finish();
			break;
		case R.id.pedo_reset:
			currentX.setText("0.0");
			currentY.setText("0.0");
			currentZ.setText("0.0");
			maxX.setText("0.0");
			maxY.setText("0.0");
			maxZ.setText("0.0");
			step.setText("0.0");
			speed.setText("0.0");
			steppre.setText("0.0");
			startflag = false;		
			break;
		default:
			break;
		}
	}

//	private void countdowndisplay() {
//		final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
//		alertDialog.setMessage("00:05");
//		alertDialog.show();
//
//		new CountDownTimer(5000, 1000) {
//			public void onTick(long millisUntilFinished) {
//				alertDialog.setMessage("00:" + (millisUntilFinished / 1000));
//			}
//
//			@Override
//			public void onFinish() {
//				alertDialog.setMessage("Completed");
//				alertDialog.dismiss();
//			}
//		}.start();
//	}

	public void onBackPressed() {
		// do something on back.return;
		startActivity(new Intent(Pedometer.this, MainActivity.class));
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.pedometer, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case R.id.pedomenu_heart:
			startActivity(new Intent(this, Heartrate.class));
			finish();
			break;
		case R.id.pedomenu_loca:
			startActivity(new Intent(this, Location.class));
			finish();
			break;
		case R.id.pedomenu_post:
			startActivity(new Intent(this, Posture.class));
			finish();
			break;
		}
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return true;
	}

	// Broadcast receiver
	// Receives updates from postureService
		

		
		private BroadcastReceiver broadcastRx = new BroadcastReceiver() {
		    @Override
		    public void onReceive(Context context, Intent intent) {
		        
		        	stepnum = intent.getIntExtra("STEP", stepnum);
		        	stepdetect = stepnum - stepthres;
		        	timestop = System.currentTimeMillis();
					timeSecondsstop = TimeUnit.MILLISECONDS.toSeconds(timestop);
					timedetect = timeSecondsstop - timeSecondsstart;
					if (timedetect != 0){
						speednum = stepdetect*60/ timedetect;
						Log.i("Speed", "speed"+ speednum + "step"+ stepdetect + "time" + timedetect);
					}
		        	float MaxX  = intent.getFloatExtra("MaxX", 0.0f);
		        	float MaxY  = intent.getFloatExtra("MaxY", 0.0f);
		        	float MaxZ  = intent.getFloatExtra("MaxZ", 0.0f);
		        	
		        	float CurrentX  = intent.getFloatExtra("CurrentX", 0.0f);
		        	float CurrentY  = intent.getFloatExtra("CurrentY", 0.0f);
		        	float CurrentZ  = intent.getFloatExtra("CurrentZ", 0.0f);
		        	
		        	int Index =  intent.getIntExtra("Frequency", 0);
		        	double Peak = intent.getDoubleExtra("Peak", 0.0d);
		        	
		        	if (startflag == true){
		        	maxX.setText(Float.toString(MaxX)); // This is different from posture. Perhaps you dont have to convert your float value
		        	maxY.setText(Float.toString(MaxY));
		        	maxZ.setText(Float.toString(MaxZ));
		        	currentX.setText(Float.toString(CurrentX));
		        	currentY.setText(Float.toString(CurrentY));
		        	currentZ.setText(Float.toString(CurrentZ));
		        	step.setText(Integer.toString(stepdetect));
		        	speed.setText(Double.toString(speednum)+" steps/min");
		        	steppre.setText(Integer.toString(Index));
		        	}
		        	
		       }
		        
		};

}
