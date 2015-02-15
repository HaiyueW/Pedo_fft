package com.ece4600.mainapp;

import com.badlogic.gdx.audio.analysis.FFT;

import java.util.concurrent.TimeUnit;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

public class PedometerService extends Service{

	private float lastX = 0, lastY = 0, lastZ = 0;
	private float deltaX = 0;
	private float deltaY = 0;
	private float deltaZ = 0;
	private float test = 0;
	private float MaxX = 0, xoldvalue = 0, MaxY = 0, yoldvalue = 0, MaxZ = 0, zoldvalue = 0;
	private int stepnum = 0, i = 0, xp = 0, yp = 0, zp = 0, xn = 0, yn = 0, zn = 0, iteration = 500;
	private long LastStepDetection = 0;
	private float StepDetectionDelta = 0, speednum = 0;
	private double DifferenceDelta = 1.0;
	private double minPeak = 0.2;
	public static dataArrayFloat[] array_1d = new dataArrayFloat[1];
	
	private double peak = 0, fftpeak = 0;
	private static int fs = 50;
	private int N = 64;
	private int index = 0, freqindex = 0, j = 0;
	private float[] arrayX = new float[N];
	private float[] arrayY = new float[N];
	private float[] arrayZ = new float[N];
	private float[] arrayfftx = new float[N];
	private float[] arrayffty = new float[N];
	private float[] arrayfftz = new float[N];
	private double[] new_sig;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public int onStartCommand(Intent intent, int flags, int startId){
		/*Called by the system when an app component requests that a 
		 * Service start using startService()
		 * Once started, it can run in the background indefinitely*/
		
		float X = intent.getFloatExtra("X", 0.0f);
		float Y = intent.getFloatExtra("Y", 0.0f);
		float Z = intent.getFloatExtra("Z", 0.0f);
		dataArrayFloat data = new dataArrayFloat(X, Y, Z);
		array_1d[0] = data;
		Log.i("Pedometer", String.valueOf(X) + "," + String.valueOf(Y) + ","+String.valueOf(Z));
		
		stepDetection();
		
		if (j < N) {
			arrayX[j] = X;
			arrayY[j] = Y;
			arrayZ[j] = Z;
			j++;
			Log.i("FFT", String.valueOf(X) + "," + String.valueOf(Y) + ","+String.valueOf(Z));
		}else{
			new_sig = fft(N, fs, arrayX, arrayY, arrayZ);
			j = 0;
		}
		
		return super.onStartCommand(intent,flags, startId);
	}
	
	public void onCreate(){
		/* Called when service is first created
		 * one time setup. Not called if already running*/
		MaxX = 0;
		MaxY = 0;
		MaxZ = 0;		
		i = 0;
		stepnum = 0;
		speednum = 0;
		xoldvalue = 0;
		yoldvalue = 0;
		zoldvalue = 0;
		deltaX = 0;
		deltaY = 0;
		deltaZ = 0;
		
		super.onCreate();
	}
	
	public void onDestroy(){
		/*Called when a service no longer used and being destroyed*/
		super.onDestroy();
	}// End of onDestroy
	
//	private Runnable resend = new Runnable() {
//		   @Override
//		   public void run() {
//			 
//			  Intent i = new Intent("PEDOMETER_EVENT");
//			  i.putExtra("STEP", stepnum);
//			  i.putExtra("SPEED", speednum);
//			  sendBroadcast(i);
//			 
//			  Handler h = new Handler(Looper.getMainLooper()); //handler to delay the scan, if can't connect, then stop attempts to scan
//			  h.postDelayed(this, 1000);
//			  
//		   }};
		   
	public void stepDetection() {
			float accX = array_1d[0].xaxis;
			float accY = array_1d[0].yaxis;
			float accZ = array_1d[0].zaxis;
		
			deltaX = accX - lastX;
			deltaY = accY - lastY;
			deltaZ = accZ - lastZ;
			
			long time = System.currentTimeMillis();
			long delta = time - LastStepDetection;
			long timeSeconds = TimeUnit.MILLISECONDS.toSeconds(delta);
			test = Math.max(Math.abs(accX), Math.max(Math.abs(accY), Math.abs(accZ)));
			// if the change is below 1.5, it is just plain noise
			if ((deltaX < 0.05) && (deltaY < 0.05) && (deltaZ < 0.05)) {
				deltaX = 0;
				deltaY = 0;
				deltaZ = 0;
			} else if (iteration == 500) {
				iteration = 0;
				if ((test == Math.abs(accX)) && (test > 0.5)) {
					i = 1;
				} else if ((test == Math.abs(accY)) && (test > 0.5)) {
					i = 2;
				} else if ((test == Math.abs(accZ)) && (test > 0.5)) {
					i = 3;
				}
			}

			switch (i) {
			case 1:
				if (deltaX > 0) {
					xp++;
					MaxX = Math.max(MaxX, Math.max(Math.abs(accX), xoldvalue));
				} else if (xp > 2) {
					if (deltaX < 0) {
						xn++;
						if (xn > 1 && delta > StepDetectionDelta && MaxX - Math.abs(accX) > minPeak) {
							LastStepDetection = time;
							stepnum++;
							//timeSeconds += timeSeconds;
							iteration++;
							xp = 0;
							xn = 0;
							MaxX = 0;
						}
					}
				}
				xoldvalue = Math.abs(accX);
				Log.i("Pedometer", "Step detected Xaxis" + stepnum + "Delta time" + timeSeconds);
				break;
			case 2:
				if (deltaY > 0) {
					yp++;
					MaxY = Math.max(MaxY, Math.max(Math.abs(accY), yoldvalue));
				} else if (yp > 2) {
					if (deltaY < 0) {
						yn++;
						if (yn > 1 && delta > StepDetectionDelta && MaxY - Math.abs(accY) > minPeak) {
							LastStepDetection = time;
							stepnum++;
							//timeSeconds += timeSeconds;
							iteration++;
							yp = 0;
							yn = 0;
							MaxY = 0;
						}
					}
				}
				yoldvalue = Math.abs(accY);
				Log.i("Pedometer", "Step detected Yaxis " + stepnum + "Delta time" + timeSeconds);
				break;
			case 3:
				if (deltaZ > 0) {
					zp++;
					MaxZ = Math.max(MaxZ, Math.max(Math.abs(accZ), zoldvalue));
				} else if (zp > 2) {
					if (deltaZ < 0) {
						zn++;
						if (zn > 1 && delta > StepDetectionDelta && MaxZ - Math.abs(accZ) > minPeak) {
							LastStepDetection = time;
							stepnum++;
							//timeSeconds += timeSeconds;
							iteration++;
							zp = 0;
							zn = 0;
							MaxZ = 0;
						}
					}
				}
				zoldvalue = Math.abs(accZ);
				Log.i("Pedometer", "Step detected Zaxis " + stepnum + "Delta time" + timeSeconds);
				break;
			default:
				break;
			}
			// set the last know values of x,y,z
			lastX = accX;
			lastY = accY;
			lastZ = accZ;
			
			
			//Perhaps you dont need a handler
			Handler acc = new Handler(Looper.getMainLooper());
			acc.post(new Runnable(){
				@Override
				public void run(){
					Intent i = new Intent("PEDOMETER_EVENT");
					
					i.putExtra("MaxX", MaxX);
					i.putExtra("MaxY", MaxY);
					i.putExtra("MaxZ", MaxZ);
					
					i.putExtra("CurrentX", lastX);
					i.putExtra("CurrentY", lastY);
					i.putExtra("CurrentZ", lastZ);
					
					i.putExtra("STEP", stepnum);
					sendBroadcast(i);
				}
			});
		}
	
	private double[] fft(int N, int fs, float[] arrayX, float[] arrayY, float[] arrayZ) {
		float[] fft_imx, fft_imy, fft_imz, fft_rex, fft_rey, fft_rez;
		// float[] mod_spec =new float[array.length/2];
		double[] fft = new double[N];
		double fft_x, fft_y, fft_z;

//		// Zero Pad signal
//		for (int i = 0; i < N; i++) {
//
//			if (i < array.length) {
//				new_arrayX[i] = array[i];
//				new_arrayY[i] = array[i];
//				new_arrayZ[i] = array[i];
//			} else {
//				new_arrayX[i] = 0;
//				new_arrayY[i] = 0;
//				new_arrayZ[i] = 0;
//			}
//		}

		FFT fftx = new FFT(N, fs);
		FFT ffty = new FFT(N, fs);
		FFT fftz = new FFT(N, fs);
		fftx.forward(arrayX);
		ffty.forward(arrayY);
		fftz.forward(arrayZ);
		fft_imx = fftx.getImaginaryPart();
		fft_rex = fftx.getRealPart();
		fft_imy = ffty.getImaginaryPart();
		fft_rey = ffty.getRealPart();
		fft_imz = fftz.getImaginaryPart();
		fft_rez = fftz.getRealPart();
		for (int k = 0; k < N/2; k++) {
			fft_x = Math.sqrt(Math.pow(fft_imx[k],2) + (Math.pow(fft_rex[k],2)));
			fft_y = Math.sqrt(Math.pow(fft_imy[k],2) + (Math.pow(fft_rey[k],2)));
			fft_z = Math.sqrt(Math.pow(fft_imz[k],2) + (Math.pow(fft_rez[k],2)));
			double fftt = (Math.pow(fft_x, 2) + Math.pow(fft_y, 2) + Math.pow(fft_z, 2));
			//double fftt = -Math.log10((1/(fs*N)) * (Math.pow(fft_x, 2) + Math.pow(fft_y, 2) + Math.pow(fft_z, 2)));
			Log.i("fftv", "Value " + fftt + "X" + fft_x + "Y" + fft_y + "Z" + fft_z);
			if (fftt > peak) {
				peak = fftt;
				index = k;
			}
		}
		freqindex = index;
		fftpeak = peak;
		Log.i("fft", "Frequency " + freqindex + "Peak" + fftpeak);
//		tmpi = fft.getImaginaryPart();
//		tmpr = fft.getRealPart();
		Handler ffts = new Handler(Looper.getMainLooper());
		ffts.post(new Runnable(){
			@Override
			public void run(){
				Intent i = new Intent("PEDOMETER_EVENT");
				
				i.putExtra("Frequency", freqindex);
				i.putExtra("Peak", fftpeak);
				sendBroadcast(i);
			}
		});
		return fft;

	}
	}
