package com.serenegiant.net;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.serenegiant.utils.BuildCheck;
import com.serenegiant.utils.ComponentUtils;

public class NetworkChangedReceiver extends BroadcastReceiver {
	private static final boolean DEBUG = false; // FIXME 実働時はfalseにすること
	private static final String TAG = NetworkChangedReceiver.class.getSimpleName();

	public static final String KEY_NETWORK_CHANGED_IS_CONNECTED_OR_CONNECTING = "KEY_NETWORK_CHANGED_IS_CONNECTED_OR_CONNECTING";
	public static final String KEY_NETWORK_CHANGED_IS_CONNECTED = "KEY_NETWORK_CHANGED_IS_CONNECTED";
	public static final String KEY_NETWORK_CHANGED_ACTIVE_NETWORK_FLAG = "KEY_NETWORK_CHANGED_ACTIVE_NETWORK_FLAG";

	/**
	 * The Mobile data connection.  When active, all data traffic
	 * will use this network type's interface by default
	 * (it has a default route)
	 */
	public static final int NETWORK_TYPE_MOBILE = 1 << ConnectivityManager.TYPE_MOBILE;	// 1 << 0
	/**
	 * The WIFI data connection.  When active, all data traffic
	 * will use this network type's interface by default
	 * (it has a default route).
	 */
	public static final int NETWORK_TYPE_WIFI = 1 << ConnectivityManager.TYPE_WIFI;	// 1 << 1

	/**
	 * An MMS-specific Mobile data connection.  This network type may use the
	 * same network interface as TYPE_MOBILE or it may use a different
	 * one.  This is used by applications needing to talk to the carrier's
	 * Multimedia Messaging Service servers.
	 */
	public static final int NETWORK_TYPE_MOBILE_MMS = 1 << ConnectivityManager.TYPE_MOBILE_MMS;	// 1 << 2

	/**
	 * A SUPL-specific Mobile data connection.  This network type may use the
	 * same network interface as TYPE_MOBILE or it may use a different
	 * one.  This is used by applications needing to talk to the carrier's
	 * Secure User Plane Location servers for help locating the device.
	 */
	public static final int NETWORK_TYPE_MOBILE_SUPL = 1 << ConnectivityManager.TYPE_MOBILE_SUPL;	// 1 << 3

	/**
	 * A DUN-specific Mobile data connection.  This network type may use the
	 * same network interface as TYPE_MOBILE or it may use a different
	 * one.  This is sometimes by the system when setting up an upstream connection
	 * for tethering so that the carrier is aware of DUN traffic.
	 */
	public static final int NETWORK_TYPE_MOBILE_DUN = 1 << ConnectivityManager.TYPE_MOBILE_DUN;	// 1 << 4

	/**
	 * A High Priority Mobile data connection.  This network type uses the
	 * same network interface as TYPE_MOBILE but the routing setup
	 * is different.  Only requesting processes will have access to the
	 * Mobile DNS servers and only IP's explicitly requested via requestRouteToHost
	 * will route over this interface if no default route exists.
	 */
	public static final int NETWORK_TYPE_MOBILE_HIPRI = 1 << ConnectivityManager.TYPE_MOBILE_HIPRI;	// 1 << 5

	/**
	 * The WiMAX data connection.  When active, all data traffic
	 * will use this network type's interface by default
	 * (it has a default route).
	 */
	public static final int NETWORK_TYPE_WIMAX = 1 << ConnectivityManager.TYPE_WIMAX;	// 1 << 6

	/**
	 * The Bluetooth data connection.  When active, all data traffic
	 * will use this network type's interface by default
	 * (it has a default route).
	 * XXX 単にBluetooth機器を検出しただけじゃこの値は来ない, Bluetooth経由のネットワークに接続しないとダメみたい
	 */
	public static final int NETWORK_TYPE_BLUETOOTH = 1 << ConnectivityManager.TYPE_BLUETOOTH;	// 1 << 7

	/**
	 * The Ethernet data connection.  When active, all data traffic
	 * will use this network type's interface by default
	 * (it has a default route).
	 */
	public static final int NETWORK_TYPE_ETHERNET = 1 << ConnectivityManager.TYPE_ETHERNET;	// 1 << 9

	/**
	 * A virtual network using one or more native bearers.
	 * It may or may not be providing security services.
	 */
//	public static final int NETWORK_TYPE_VPN = 1 << ConnectivityManager.TYPE_VPN;	// 1 << 17

	private static final int NETWORK_MASK_INTERNET_WIFI = NETWORK_TYPE_WIFI | NETWORK_TYPE_WIMAX | NETWORK_TYPE_BLUETOOTH | NETWORK_TYPE_ETHERNET;

	/** ローカルブロードキャスト受信時のコールバックリスナー */
	public interface OnNetworkChangedListener {
		/**
		 * @param isConnectedOrConnecting 接続中かread/write可能
		 * @param isConnected read/write可能
		 * @param activeNetworkFlag アクティブなネットワークの選択フラグ 接続しているネットワークがなければ0
		 */
		public void onNetworkChanged(
			final int isConnectedOrConnecting, final int isConnected, final int activeNetworkFlag);
	}

	public static void enable(final Context context) {
		ComponentUtils.enable(context, NetworkChangedReceiver.class);
	}

	public static void disable(final Context context) {
		ComponentUtils.disable(context, NetworkChangedReceiver.class);
	}

	/**
	 * LocalBroadcastManagerにローカルブロードキャスト受信用のレシーバーを登録する
	 * @param context
	 * @param listener
	 * @return
	 */
	public static NetworkChangedReceiver registerNetworkChangedReceiver(final Context context, final OnNetworkChangedListener listener) {
		final NetworkChangedReceiver receiver = new NetworkChangedReceiver(listener);
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ACTION_LOCAL_CONNECTIVITY_CHANGE);
		final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context.getApplicationContext());
		broadcastManager.registerReceiver(receiver, intentFilter);
		receiver.onReceiveGlobal(context, null);
		return receiver;
	}

	/**
	 * LocalBroadcastManagerからローカルブロードキャスト受信用のレシーバーを登録解除する
	 * @param context
	 * @param receiver
	 */
	public static void unregisterNetworkChangedReceiver(final Context context, final NetworkChangedReceiver receiver) {
		final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context.getApplicationContext());
		broadcastManager.unregisterReceiver(receiver);
	}

	private OnNetworkChangedListener mListener = null;

	public NetworkChangedReceiver() {
	}

	public NetworkChangedReceiver(final OnNetworkChangedListener listener) {
		if (listener == null) {
			throw new NullPointerException("onNetworkChangedListenerコールバックリスナーがnullだよ");
		}
		mListener = listener;
	}

	private static final int[] NETWORKS;
	static {
		NETWORKS = new int[] {
			ConnectivityManager.TYPE_MOBILE, NETWORK_TYPE_MOBILE,
			ConnectivityManager.TYPE_WIFI, NETWORK_TYPE_WIFI,
			ConnectivityManager.TYPE_MOBILE_MMS, NETWORK_TYPE_MOBILE_MMS,
			ConnectivityManager.TYPE_MOBILE_SUPL, NETWORK_TYPE_MOBILE_SUPL,
			ConnectivityManager.TYPE_MOBILE_DUN, NETWORK_TYPE_MOBILE_DUN,
			ConnectivityManager.TYPE_MOBILE_HIPRI, NETWORK_TYPE_MOBILE_HIPRI,
			ConnectivityManager.TYPE_WIMAX, NETWORK_TYPE_WIMAX,
			ConnectivityManager.TYPE_BLUETOOTH, NETWORK_TYPE_BLUETOOTH,
			ConnectivityManager.TYPE_ETHERNET, NETWORK_TYPE_ETHERNET,
//			ConnectivityManager.TYPE_VPN, NETWORK_TYPE_VPN,
		};
	};

	private static final Object sSync = new Object();
	private static int sIsConnectedOrConnecting = 0;
	private static int sIsConnected = 0;
	private static int sActiveNetworkFlag = 0;

	public static final String ACTION_GLOBAL_CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";
	public static final String ACTION_LOCAL_CONNECTIVITY_CHANGE = "com.serenegiant.net.CONNECTIVITY_CHANGE";

	@Override
	public void onReceive(final Context context, final Intent intent) {
		final String action = intent != null ? intent.getAction() : null;
		if (ACTION_GLOBAL_CONNECTIVITY_CHANGE.equals(action)) {
			onReceiveGlobal(context, intent);
		} else if (ACTION_LOCAL_CONNECTIVITY_CHANGE.equals(action)) {
			onReceiveLocal(context, intent);
		}
	}

	@SuppressLint("NewApi")
	private void onReceiveGlobal(final Context context, final Intent intent) {
		// Intentに接続状態が入っているみたいだけど自前で取得しなおす
		final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context.getApplicationContext());
//		final NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//		final NetworkInfo mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		int isConnectedOrConnecting = 0;
		int isConnected = 0;

		if (BuildCheck.isAndroid5()) {	// API>=21
			final Network[] networks = connectivityManager.getAllNetworks();
			if (networks != null) {
				for (final Network network: networks) {
					final NetworkInfo info = connectivityManager.getNetworkInfo(network);
					if (info != null) {
						isConnectedOrConnecting |= info.isConnectedOrConnecting() ? (1 << info.getType()) : 0;
						isConnected |= info.isConnected() ? (1 << info.getType()) : 0;
					}
				}
			}
		} else {
			final int n = NETWORKS.length;
			for (int i = 0; i < n; i += 2) {
				final NetworkInfo info = connectivityManager.getNetworkInfo(NETWORKS[i]);
				if (info != null) {
					isConnectedOrConnecting |= info.isConnectedOrConnecting() ? NETWORKS[i + 1] : 0;
					isConnected |= info.isConnected() ? NETWORKS[i + 1] : 0;
				}
			}
		}
		final NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		final int activeNetworkFlag = (activeNetworkInfo != null ? 1 << activeNetworkInfo.getType() : 0);
		synchronized (sSync) {
			sIsConnectedOrConnecting = isConnectedOrConnecting;
			sIsConnected = isConnected;
			sActiveNetworkFlag = activeNetworkFlag;
			sSync.notifyAll();
		}
		final Intent networkChangedIntent = new Intent(ACTION_LOCAL_CONNECTIVITY_CHANGE);

		if (DEBUG) Log.v(TAG, String.format("onNetworkChanged:isConnectedOrConnecting=%08x,isConnected=%08x,activeNetworkMask=%08x",
			isConnectedOrConnecting, isConnected, activeNetworkFlag));
		networkChangedIntent.putExtra(KEY_NETWORK_CHANGED_IS_CONNECTED_OR_CONNECTING, isConnectedOrConnecting);
		networkChangedIntent.putExtra(KEY_NETWORK_CHANGED_IS_CONNECTED, isConnected);
		networkChangedIntent.putExtra(KEY_NETWORK_CHANGED_ACTIVE_NETWORK_FLAG, activeNetworkFlag);
		broadcastManager.sendBroadcast(networkChangedIntent);
	}

	private void onReceiveLocal(final Context context, final Intent intent) {
		final int isConnectedOrConnecting = intent.getIntExtra(KEY_NETWORK_CHANGED_IS_CONNECTED_OR_CONNECTING, 0);
		final int isConnected = intent.getIntExtra(KEY_NETWORK_CHANGED_IS_CONNECTED, 0);
		final int activeNetworkInfo = intent.getIntExtra(KEY_NETWORK_CHANGED_ACTIVE_NETWORK_FLAG, 0);
		if (mListener != null) {
			try {
				mListener.onNetworkChanged(isConnectedOrConnecting, isConnected, activeNetworkInfo);
			} catch (final Exception e) {
				Log.w(TAG, e);
			}
		}
	}

	public static boolean isWifiNetworkReachable() {
		final int isConnectedOrConnecting;
		synchronized (sSync) {
			isConnectedOrConnecting = sIsConnectedOrConnecting;
		}
		return (isConnectedOrConnecting & NETWORK_MASK_INTERNET_WIFI) != 0;
	}

	public static boolean isWifiNetworkReachable(final Context context) {
		final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo activeNetworkInfo = connMgr.getActiveNetworkInfo();
		if ((activeNetworkInfo != null) && (activeNetworkInfo.isConnectedOrConnecting())) {
			final int type = activeNetworkInfo.getType();
			return (type == ConnectivityManager.TYPE_WIFI)
				|| (type == ConnectivityManager.TYPE_WIMAX)
				|| (type == ConnectivityManager.TYPE_BLUETOOTH)
				|| (type == ConnectivityManager.TYPE_ETHERNET);
		}
		return false;
	}

	public static boolean isMobileNetworkReachable() {
		final int isConnectedOrConnecting;
		synchronized (sSync) {
			isConnectedOrConnecting = sIsConnectedOrConnecting;
		}
		return (isConnectedOrConnecting & NETWORK_TYPE_MOBILE) != 0;
	}

	public static boolean isMobileNetworkReachable(final Context context) {
		final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo activeNetworkInfo = connMgr.getActiveNetworkInfo();
		if ((activeNetworkInfo != null) && (activeNetworkInfo.isConnectedOrConnecting())) {
			final int type = activeNetworkInfo.getType();
			return (type == ConnectivityManager.TYPE_MOBILE);
		}
		return false;
	}

	public static boolean isNetworkReachable() {
		final int isConnectedOrConnecting;
		synchronized (sSync) {
			isConnectedOrConnecting = sIsConnectedOrConnecting;
		}
		return isConnectedOrConnecting != 0;
	}

	public static boolean isNetworkReachable(final Context context) {
		final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo activeNetworkInfo = connMgr.getActiveNetworkInfo();
		return (activeNetworkInfo != null) && (activeNetworkInfo.isConnectedOrConnecting());
	}
}
