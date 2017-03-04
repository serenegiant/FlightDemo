package com.serenegiant.remotegamepad;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.serenegiant.gamepad.Joystick;

import java.security.cert.CertificateException;

import javax.net.ssl.SSLException;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

/**
 * Netty経由でゲームパッド入力を他端末へ送信するためのクラス
 * 当然だけどアプリにネットワークアクセスのパーミッションが必要
 */
public class RemoteJoystickSrv {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static final String TAG = RemoteJoystickSrv.class.getSimpleName();

	/**
	 * SSLを使うかどうか...今はSSLを使わない
	 */
	public static final boolean ENABLE_SSL = false;
	/**
	 * デフォルトのポート番号
	 */
	public static final int DEFAULT_PORT = 9876;

	/**
	 * RemoteJoystickSrvからのコールバックリスナー
	 */
	public interface RemoteJoystickSrvListener {
		public void onConnect(final RemoteJoystickSrv srv, final String remote);
		public void onDisconnect(final RemoteJoystickSrv srv, final String remote);
		public void onError(final RemoteJoystickSrv srv, final Exception e);
	}

//================================================================================
	private final Object mSync = new Object();
	private final RemoteJoystickEvent mEvent = new RemoteJoystickEvent();
	private final RemoteJoystickSrvListener mListener;
	private Joystick mJoystick;
	private volatile boolean mReleased;
	// Netty関係のフィールド
	private final SslContext mSslContext;
	private EventLoopGroup mBossGroup;
	private EventLoopGroup mWorkerGroup;
	private Channel mChannel;

	/**
	 * コンストラクタ, portはDEFAULT_PORT
	 * @param context
	 * @param listener
	 * @throws CertificateException
	 * @throws SSLException
	 * @throws InterruptedException
	 */
	public RemoteJoystickSrv(final Context context, @NonNull final RemoteJoystickSrvListener listener)
		throws CertificateException, SSLException, InterruptedException {

		this(context, DEFAULT_PORT, listener);
	}

	/**
	 * コンストラクタ
	 * @param context
	 * @param port
	 * @param listener
	 * @throws CertificateException
	 * @throws SSLException
	 * @throws InterruptedException
	 */
	public RemoteJoystickSrv(final Context context, final int port, @NonNull final RemoteJoystickSrvListener listener)
		throws CertificateException, SSLException, InterruptedException {

		mListener = listener;

		if (ENABLE_SSL) {
			final SelfSignedCertificate ssc;
			ssc = new SelfSignedCertificate();
			mSslContext = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
		} else {
			mSslContext = null;
		}
		mBossGroup = new NioEventLoopGroup();
		mWorkerGroup = new NioEventLoopGroup();
		final ServerBootstrap b = new ServerBootstrap();
		b.group(mBossGroup, mWorkerGroup)
			.channel(NioServerSocketChannel.class)
			.handler(new LoggingHandler(LogLevel.INFO))
			.childHandler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(final SocketChannel ch) throws Exception {
					final ChannelPipeline p = ch.pipeline();
					if (mSslContext != null) {
						p.addLast(mSslContext.newHandler(ch.alloc()));
					}
					// プリミティブまたはSerializableを実装したオブジェクトの送受信を可能にする
					p.addLast(
						new ObjectEncoder(),
						new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
						new RemoteGamePadServerHandler());
				}
			})
			.childOption(ChannelOption.SO_KEEPALIVE, true)
			.childOption(ChannelOption.SO_REUSEADDR, true);
		final Channel channel = b.bind(port).sync().channel(); // .closeFuture().sync();
		synchronized (mSync) {
			mChannel = channel;
		}
		mJoystick = Joystick.getInstance(context);
		if (mJoystick != null) {
			mJoystick.register();
		}
	}

	@Override
	protected void finalize() throws Throwable {
		release();
		super.finalize();
	}

	/**
	 * 関連するリソースを開放する
	 * 再利用は出来ない
	 */
	public void release() {
		if (DEBUG) Log.v(TAG, "release:");
		mReleased = true;
		Channel channel;
		synchronized (mSync) {
			channel = mChannel;
			mChannel = null;
		}
		if (channel != null) {
			channel.close();
		}
		if (mBossGroup != null) {
			mBossGroup.shutdownGracefully();
			mBossGroup = null;
		}
		if (mWorkerGroup != null) {
			mWorkerGroup.shutdownGracefully();
			mWorkerGroup = null;
		}
		if (mJoystick != null) {
			mJoystick.release();
			mJoystick = null;
		}
		if (DEBUG) Log.v(TAG, "release:finished");
	}

	/**
	 * キー入力イベントの処理
	 * Activity#dispatchKeyEventから呼ぶこと
	 * @param event
	 * @return
	 */
	public boolean dispatchKeyEvent(final KeyEvent event) {
		final boolean result = mJoystick != null && mJoystick.dispatchKeyEvent(event);
		if (result) {
			mEvent.set(mJoystick);
		}
		return result;
	}

	/**
	 * モーションイベントの処理
	 * Activity#dispatchGenericMotionEventから呼ぶこと
	 * @param event
	 * @return
	 */
	public boolean dispatchGenericMotionEvent(final MotionEvent event) {
		final boolean result = mJoystick != null && mJoystick.dispatchGenericMotionEvent(event);
		if (result) {
			mEvent.set(mJoystick);
		}
		return result;
	}

//================================================================================
	private void callOnConnect(final String remote) {
		if (DEBUG) Log.v(TAG, "callOnConnect:");
		try {
			mListener.onConnect(this, remote);
		} catch (final Exception e) {
			Log.w(TAG, e);
		}
	}

	private void callOnDisconnect(final String remote) {
		if (DEBUG) Log.v(TAG, "callOnDisconnect:");
		try {
			mListener.onDisconnect(this, remote);
		} catch (final Exception e) {
			Log.w(TAG, e);
		}
	}

	private void callOnError(final Exception e) {
		if (DEBUG) Log.v(TAG, "callOnError:");
		try {
			mListener.onError(this, e);
		} catch (final Exception e1) {
			Log.w(TAG, e1);
		}
	}

	/**
	 * Nettyからのイベント処理
	 */
	private class RemoteGamePadServerHandler extends ChannelInboundHandlerAdapter {
//		@Override
//		public void channelRegistered(final ChannelHandlerContext ctx) throws Exception {
//			super.channelRegistered(ctx);
//			if (DEBUG) Log.v(TAG, "channelRegistered:");
//		}

//		@Override
//		public void channelUnregistered(final ChannelHandlerContext ctx) throws Exception {
//			super.channelUnregistered(ctx);
//			if (DEBUG) Log.v(TAG, "channelUnregistered:");
//		}

		@Override
		public void channelActive(final ChannelHandlerContext ctx) throws Exception {
			super.channelActive(ctx);
//			if (DEBUG) Log.v(TAG, "channelActive:");
			try {
				callOnConnect(ctx.channel().remoteAddress().toString());
			} catch (final Exception e) {
				Log.w(TAG, e);
			}
		}

		@Override
		public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
			super.channelInactive(ctx);
//			if (DEBUG) Log.v(TAG, "channelInactive:");
			try {
				callOnDisconnect(ctx.channel().remoteAddress().toString());
			} catch (final Exception e) {
				Log.w(TAG, e);
			}
		}

//		@Override
//		public void channelWritabilityChanged(final ChannelHandlerContext ctx) throws Exception {
//			super.channelWritabilityChanged(ctx);
//			if (DEBUG) Log.v(TAG, "channelWritabilityChanged:");
//		}

		@Override
		public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
//			if (DEBUG) Log.v(TAG, "channelRead:");
			// 何か送られてきたらRemoteKeyEventを送り返す
			if (!mReleased) {
				ctx.writeAndFlush(mEvent);
			}
		}

		@Override
		public void channelReadComplete(final ChannelHandlerContext ctx) throws Exception {
//			if (DEBUG) Log.v(TAG, "channelReadComplete:");
			ctx.flush();
		}

		@Override
		public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
			Log.w(TAG, cause);
			ctx.close();
			callOnError(new Exception (cause));
		}
	}

}
