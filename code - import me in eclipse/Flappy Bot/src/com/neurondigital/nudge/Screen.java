package com.neurondigital.nudge;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class Screen extends Activity implements Runnable, OnTouchListener, SensorEventListener {
	private SurfaceHolder holder;
	private boolean locker = true, initialised = false;
	private Thread thread;
	//public WakeLock WL;
	private int width = 0, height = 0;
	public float cameraX = 0, cameraY = 0;

	public Activity activity = this;
	public boolean debug_mode = false;
	private long now = SystemClock.elapsedRealtime(), lastRefresh, lastfps;
	public SurfaceView surface;
	private int fps = 0, frames = 0;

	//sensor
	SensorManager sm;
	Sensor s;
	float sensorx, calibratex = 0;
	float sensory, calibratey = 0;
	private boolean default_lanscape = false;
	private int default_lanscape_rotation = 0;

	//world origin
	public final int TOP_LEFT = 0, BOTTOM_LEFT = 1;
	public int origin = TOP_LEFT;

	//layout
	public RelativeLayout layout;
	private AdView adView;
	public String BANNER_AD_UNIT_ID;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		activity = this;

		//full screen
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		//create surface
		layout = new RelativeLayout(this);
		surface = new SurfaceView(this);
		layout.addView(surface);

		setContentView(layout);
		holder = surface.getHolder();

		//listeners
		surface.setOnTouchListener(this);

		// start game loop
		thread = new Thread(this);
		thread.start();

		onCreate();

	}
	
	public void showBanner() {
		//banner ad
		if (BANNER_AD_UNIT_ID.length() > 0) {
			// Create an ad.
			adView = new AdView(this);
			adView.setAdSize(AdSize.BANNER);
			adView.setAdUnitId(BANNER_AD_UNIT_ID);

			//make ad visible on bottom of screen
			RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			params1.addRule(RelativeLayout.CENTER_HORIZONTAL);
			adView.setLayoutParams(params1);

			// Create an ad request. Check logcat output for the hashed device ID to
			// get test ads on a physical device.
			final AdRequest adRequest = new AdRequest.Builder()
					.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
					.addTestDevice("275D94C2B5B93B3C4014933E75F92565")///nexus7//////testing
					.addTestDevice("91608B19766D984A3F929C31EC6AB947") /////////////////testing//////////////////remove///////////
					.addTestDevice("6316D285813B01C56412DAF4D3D80B40") ///test htc sensesion xl
					.addTestDevice("8C416F4CAF490509A1DA82E62168AE08")//asus transformer
					.addTestDevice("7B4C6D080C02BA40EF746C4900BABAD7")//Galaxy S4
					.build();

			// Start loading the ad in the background.
			adView.loadAd(adRequest);
			adView.setAdListener(new AdListener() {
				public void onAdLoaded() {
					View parent = (View) adView.getParent();
					if (parent != null) {
						if (!parent.equals(layout)) {
							layout.addView(adView);
						}
					} else
						layout.addView(adView);
				}
			});
		}
	}

	/* Main game loop.......................................................... */
	@Override
	public void run() {
		int rand = (int) (Math.random() * 100);
		synchronized (ACCESSIBILITY_SERVICE) {

			while (locker) {
				//System.out.println("start-");

				now = SystemClock.elapsedRealtime();
				if (now - lastRefresh > 28) {
					lastRefresh = SystemClock.elapsedRealtime();
					if (!holder.getSurface().isValid()) {
						continue;
					}

					//fps
					if (now - lastfps > 1000) {
						fps = frames;
						frames = 0;
						lastfps = SystemClock.elapsedRealtime();
					} else {
						frames++;
					}

					//step
					if (initialised)
						Step(rand);

					//draw screen
					Canvas canvas = holder.lockCanvas();
					if (initialised)
						Draw(canvas);
					else {
						//initialise game
						width = canvas.getWidth();
						height = canvas.getHeight();
						Start();
						initialised = true;
					}
					holder.unlockCanvasAndPost(canvas);
				}
				//System.out.println("finish-----");
				//try {
				//	Thread.sleep(10);
				//} catch (InterruptedException e) {
				//	e.printStackTrace();
				//}
			}
		}

	}

	/* Detect and override back press */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		super.onKeyDown(keyCode, event);
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			BackPressed();
			return false;
		}

		return false;
	}

	/* Events.................................................................. */
	public void onCreate() {

	}

	public void Start() {

	}

	synchronized public void Step(int rand) {

	}

	public void Draw(Canvas canvas) {
		if (debug_mode) {
			Paint paint = new Paint();
			paint.setColor(Color.BLACK);
			paint.setTextSize(dpToPx(20));
			canvas.drawText("Width: " + width + ", Height: " + height, 5, dpToPx(20), paint);
			canvas.drawText("default landscape: " + default_lanscape + " Rotation: " + default_lanscape_rotation, 5, 5 + dpToPx(20) * 2, paint);
			canvas.drawText("FPS: " + fps, 5, 5 + dpToPx(20) * 3, paint);
		}

	}

	public void Finish() {

	}

	public void Pause() {
		locker = false;

		while (true) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			break;
		}
		thread = null;
	}

	public void Resume() {
		locker = true;
		thread = new Thread(this);
		thread.start();
	}

	public synchronized void BackPressed() {

	}

	public synchronized void onTouch(float TouchX, float TouchY, MotionEvent event) {
	}

	public synchronized void onAccelerometer(PointF point) {
	}

	/* Functions............................................................... */
	public void Exit() {
		locker = false;

		while (true) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			break;
		}
		thread = null;

		System.exit(0);
		activity.finish();
	}

	public Activity getActivity() {
		return activity;
	}

	public void setDebugMode(boolean debugModeOn) {
		debug_mode = debugModeOn;
	}

	//screen related
	public int ScreenWidth() {
		return width;
	}

	public int ScreenHeight() {
		return height;
	}

	/**
	 * World X to Screen X
	 * 
	 * @param worldX
	 *            The x-coordinate relative to the world
	 */
	public int ScreenX(float worldX) {
		return (int) (worldX - cameraX);
	}

	/**
	 * World Y to Screen Y
	 * 
	 * @param worldY
	 *            The Y-coordinate relative to the world
	 */
	public int ScreenY(float worldY) {
		if (origin == TOP_LEFT)
			return (int) (worldY - cameraY);
		else
			return ScreenHeight() - (int) (worldY - cameraY);
	}

	/**
	 * World origin (0,0)
	 * 
	 * @param origin
	 *            TOP_LEFT or BOTTOM_LEFT
	 */
	public void setOrigin(int origin) {
		this.origin = origin;
	}

	public boolean inScreen(float x, float y) {
		return ((ScreenY(y) > 0 && ScreenY(y) < ScreenHeight()) && (ScreenX(x) > 0 && ScreenX(x) < ScreenWidth()));
	}

	public int dpToPx(int dp) {
		float density = getApplicationContext().getResources().getDisplayMetrics().density;
		return Math.round((float) dp * density);
	}

	//sensor related
	public void initialiseAccelerometer() {
		//device has its default landscape or portrait
		Display display = ((WindowManager) activity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		int rotation = display.getRotation();
		if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
			//portrait
			if (rotation == Surface.ROTATION_0)
				default_lanscape = false;
			if (rotation == Surface.ROTATION_180)
				default_lanscape = false;
			if (rotation == Surface.ROTATION_90)
				default_lanscape = true;
			if (rotation == Surface.ROTATION_270)
				default_lanscape = true;
		} else {
			//landscape
			if (rotation == Surface.ROTATION_0)
				default_lanscape = true;
			if (rotation == Surface.ROTATION_180)
				default_lanscape = true;
			if (rotation == Surface.ROTATION_90)
				default_lanscape = false;
			if (rotation == Surface.ROTATION_270)
				default_lanscape = false;
		}
		default_lanscape_rotation = rotation;

		sm = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
		if (sm.getSensorList(Sensor.TYPE_ACCELEROMETER).size() != 0) {
			s = sm.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
			sm.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL);
		}

	}

	public void CalibrateAccelerometer() {
		calibratex = sensorx * Math.abs(sensorx);
		calibratey = sensory * Math.abs(sensory);
	}

	public PointF getAccelerometer() {
		return new PointF((sensorx * Math.abs(sensorx) - calibratex), (sensory * Math.abs(sensory) - calibratey));
	}

	/* Touch events.......................................................... */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (initialised) {
			onTouch(event.getX(), event.getY(), event);
		}
		return true;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (initialised) {
			//read values
			if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
				if (default_lanscape) {
					sensorx = -event.values[1];
					sensory = -event.values[0];
				} else {
					sensory = event.values[1];
					sensorx = -event.values[0];
				}
			} else {
				if (default_lanscape) {
					sensory = event.values[1];
					sensorx = -event.values[0];
				} else {
					sensorx = event.values[1];
					sensory = event.values[0];
				}
			}

			//call accelerometer event
			onAccelerometer(new PointF((sensorx - calibratex), (sensory - calibratey)));

		}
		//sleep for a while
		try {
			Thread.sleep(16);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
	}

	/* pause, destroy, resume................................................ */
	@Override
	protected void onResume() {
		super.onResume();
		Resume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Pause();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Finish();
	}

}
