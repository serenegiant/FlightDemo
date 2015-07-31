package com.serenegiant.flightdemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;

import java.sql.Date;

public class PilotingActivity extends Activity {
	private static String TAG = PilotingActivity.class.getSimpleName();
	public static String EXTRA_DEVICE_SERVICE = "pilotingActivity.extra.device.service";

	public DeviceController deviceController;
	public ARDiscoveryDeviceService service;

	private Button emergencyBt;
	private Button takeoffBt;
	private Button landingBt;

	private Button gazUpBt;
	private Button gazDownBt;
	private Button yawLeftBt;
	private Button yawRightBt;

	private Button forwardBt;
	private Button backBt;
	private Button rollLeftBt;
	private Button rollRightBt;

	private Button testBtn;

	private TextView batteryLabel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pilot);

		emergencyBt = (Button) findViewById(R.id.emergencyBt);
		emergencyBt.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
			stopTest();
			if (deviceController != null) {
				deviceController.sendEmergency();
			}
			}
		});

		takeoffBt = (Button) findViewById(R.id.takeoffBt);
		takeoffBt.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
			stopTest();
			if (deviceController != null) {
				deviceController.sendTakeoff();
			}
			}
		});
		landingBt = (Button) findViewById(R.id.landingBt);
		landingBt.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
			stopTest();
			if (deviceController != null) {
				deviceController.sendLanding();
			}
			}
		});

		gazUpBt = (Button) findViewById(R.id.gazUpBt);
		gazUpBt.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
			stopTest();
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				v.setPressed(true);
				if (deviceController != null) {
					deviceController.setGaz((byte) 50);
				}
				break;
			case MotionEvent.ACTION_UP:
				v.setPressed(false);
				if (deviceController != null) {
					deviceController.setGaz((byte) 0);
				}
				break;
			default:
				break;
			}
			return true;
			}
		});

		gazDownBt = (Button) findViewById(R.id.gazDownBt);
		gazDownBt.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
			stopTest();
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				v.setPressed(true);
				if (deviceController != null) {
					deviceController.setGaz((byte) -50);
				}
				break;
			case MotionEvent.ACTION_UP:
				v.setPressed(false);
				if (deviceController != null) {
					deviceController.setGaz((byte) 0);
				}
				break;
			default:
				break;
			}
			return true;
			}
		});
		yawLeftBt = (Button) findViewById(R.id.yawLeftBt);
		yawLeftBt.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
			stopTest();
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				v.setPressed(true);
				if (deviceController != null) {
					deviceController.setYaw((byte) -50);
				}
				break;
			case MotionEvent.ACTION_UP:
				v.setPressed(false);
				if (deviceController != null) {
					deviceController.setYaw((byte) 0);
				}
				break;
			default:
				break;
			}
			return true;
			}
		});
		yawRightBt = (Button) findViewById(R.id.yawRightBt);
		yawRightBt.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
			stopTest();
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				v.setPressed(true);
				if (deviceController != null) {
					deviceController.setYaw((byte) 50);
				}
				break;
			case MotionEvent.ACTION_UP:
				v.setPressed(false);
				if (deviceController != null) {
					deviceController.setYaw((byte) 0);
				}
				break;
			default:
				break;
			}
			return true;
			}
		});

		forwardBt = (Button) findViewById(R.id.forwardBt);
		forwardBt.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
			stopTest();
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				v.setPressed(true);
				if (deviceController != null) {
					deviceController.setPitch((byte) 50);
					deviceController.setFlag((byte) 1);
				}
				break;
			case MotionEvent.ACTION_UP:
				v.setPressed(false);
				if (deviceController != null) {
					deviceController.setPitch((byte) 0);
					deviceController.setFlag((byte) 0);
				}
				break;
			default:
				break;
			}
			return true;
			}
		});
		backBt = (Button) findViewById(R.id.backBt);
		backBt.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
			stopTest();
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				v.setPressed(true);
				if (deviceController != null) {
					deviceController.setPitch((byte) -50);
					deviceController.setFlag((byte) 1);
				}
				break;
			case MotionEvent.ACTION_UP:
				v.setPressed(false);
				if (deviceController != null) {
					deviceController.setPitch((byte) 0);
					deviceController.setFlag((byte) 0);
				}
				break;
			default:
				break;
			}
			return true;
			}
		});
		rollLeftBt = (Button) findViewById(R.id.rollLeftBt);
		rollLeftBt.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
			stopTest();
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				v.setPressed(true);
				if (deviceController != null) {
					deviceController.setRoll((byte) -50);
					deviceController.setFlag((byte) 1);
				}
				break;
			case MotionEvent.ACTION_UP:
				v.setPressed(false);
				if (deviceController != null) {
					deviceController.setRoll((byte) 0);
					deviceController.setFlag((byte) 0);
				}
				break;
			default:
				break;
			}
			return true;
			}
		});
		rollRightBt = (Button) findViewById(R.id.rollRightBt);
		rollRightBt.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
			stopTest();
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				v.setPressed(true);
				if (deviceController != null) {
					deviceController.setRoll((byte) 50);
					deviceController.setFlag((byte) 1);
				}
				break;
			case MotionEvent.ACTION_UP:
				v.setPressed(false);
				if (deviceController != null) {
					deviceController.setRoll((byte) 0);
					deviceController.setFlag((byte) 0);
				}
				break;
			default:
				break;
			}
			return true;
			}
		});

		testBtn = (Button) findViewById(R.id.test_btn);
		testBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
			}
		});

		batteryLabel = (TextView) findViewById(R.id.batteryLabel);

		final Intent intent = getIntent();
		service = intent.getParcelableExtra(EXTRA_DEVICE_SERVICE);

		deviceController = new DeviceController(this, service);
		deviceController.setListener(mDeviceControllerListener);
	}

	@Override
	public void onStart() {
		super.onStart();
		startDeviceController();
	}

	private void startDeviceController() {
		if (deviceController != null) {
			// FIXME AlertDialogを使うのはあまり良くない ProgressDialogに変える
			final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PilotingActivity.this);
			alertDialogBuilder.setTitle(R.string.connecting);
			final AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();

			new Thread(new Runnable() {
				@Override
				public void run() {
					boolean failed = false;

					failed = deviceController.start();

					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							//alertDialog.hide();
							alertDialog.dismiss();
						}
					});

					if (failed) {
						getFragmentManager().popBackStack();
					} else {
						//only with RollingSpider in version 1.97 : date and time must be sent to permit a reconnection
						Date currentDate = new Date(System.currentTimeMillis());
						deviceController.sendDate(currentDate);
						deviceController.sendTime(currentDate);
					}
				}
			}).start();

		}
	}

	private void stopDeviceController() {
		if (deviceController != null) {
			// FIXME AlertDialogを使うのはあまり良くない ProgressDialogに変える
			final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PilotingActivity.this);

			// set title
			alertDialogBuilder.setTitle("Disconnecting ...");

			// create alert dialog
			final AlertDialog alertDialog = alertDialogBuilder.create();

			// show it
			alertDialog.show();

			new Thread(new Runnable() {
				@Override
				public void run() {
				deviceController.stop();

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
					//alertDialog.hide();
					alertDialog.dismiss();
					finish();
					}
				});
				}
			}).start();
		}
	}

	@Override
	protected void onStop() {
		stopDeviceController();

		super.onStop();
	}

	@Override
	public void onBackPressed() {
		stopDeviceController();
	}

	private final DeviceControllerListener mDeviceControllerListener = new DeviceControllerListener() {
		@Override
		public void onDisconnect() {
			stopDeviceController();
		}

		@Override
		public void onUpdateBattery(final byte percent) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					batteryLabel.setText(String.format("%d%%", percent));
				}
			});
		}
	};

	private void startTest() {

	}

	private void stopTest() {

	}
}
