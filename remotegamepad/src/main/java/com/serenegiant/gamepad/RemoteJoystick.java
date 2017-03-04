package com.serenegiant.gamepad;

import android.support.annotation.NonNull;
import android.util.Log;

import com.serenegiant.remotegamepad.RemoteJoystickSrv;
import com.serenegiant.remotegamepad.RemoteJoystickEvent;

import javax.net.ssl.SSLException;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

/**
 * Netty経由でリモートからのゲームパッド入力を受信するためのクラス
 * 当然だけどアプリにネットワークアクセスのパーミッションが必要
 */
public class RemoteJoystick extends IGamePad {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static final String TAG = RemoteJoystick.class.getSimpleName();

	public interface RemoteJoystickListener {
		public void onConnect(final RemoteJoystick joystick);
		public void onDisconnect(final RemoteJoystick joystick);
		public void onUpdate(final RemoteJoystick joystick);
		public void onError(final RemoteJoystick joystick, final Exception e);
	}

//================================================================================
	private final RemoteJoystickEvent mRemoteJoystickEvent = new RemoteJoystickEvent();
	private volatile boolean mReleased;
	private final RemoteJoystickListener mListener;
	// Netty関係のフィールド
	private final SslContext mSslContext;
	private EventLoopGroup mGroup;

	/**
	 * コンストラクタ
	 * portはRemoteJoystickSrv#DEFAULT_PORT
	 * @param host
	 * @param listener
	 * @throws SSLException
	 * @throws InterruptedException
	 */
	public RemoteJoystick(final String host, @NonNull final RemoteJoystickListener listener)
		throws SSLException, InterruptedException {

		this(host, RemoteJoystickSrv.DEFAULT_PORT, listener);
	}

	/**
	 * コンストラクタ
	 * @param host
	 * @param port
	 * @param listener
	 * @throws SSLException
	 * @throws InterruptedException
	 */
	public RemoteJoystick(final String host, final int port, @NonNull final RemoteJoystickListener listener)
		throws SSLException, InterruptedException {

		mListener = listener;
		if (RemoteJoystickSrv.ENABLE_SSL) {
			mSslContext = SslContextBuilder.forClient()
				.trustManager(InsecureTrustManagerFactory.INSTANCE).build();
		} else {
			mSslContext = null;
		}
		mGroup = new NioEventLoopGroup();
		final Bootstrap b = new Bootstrap();
		b.group(mGroup)
			.channel(NioSocketChannel.class)
			.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				public void initChannel(SocketChannel ch) throws Exception {
					final ChannelPipeline p = ch.pipeline();
					if (mSslContext != null) {
						p.addLast(mSslContext.newHandler(ch.alloc(), host, port));
					}
					p.addLast(
						new ObjectEncoder(),
						new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
						new RemoteGamePadClientHandler());
					}
			});

		// Start the connection attempt.
		b.connect(host, port); // .sync().channel().closeFuture().sync();
	}

	@Override
	protected void finalize() throws Throwable {
		release();
		super.finalize();
	}

	public void release() {
		if (DEBUG) Log.v(TAG, "release:");
		mReleased = true;
		mRemoteJoystickEvent.clear();
		if (mGroup != null) {
			mGroup.shutdownGracefully();
			mGroup = null;
		}
		if (DEBUG) Log.v(TAG, "release:finished");
	}

	/**
	 * リモートから送られてきたゲームパッド入力を読み込む
	 * @param downs
	 * @param down_times
	 * @param analog_sticks
	 * @param force
	 */
	@Override
	public void updateState(final boolean[] downs, final long[] down_times,
		final int[] analog_sticks, final boolean force) {

		mRemoteJoystickEvent.updateState(downs, down_times, analog_sticks, force);
	}

//================================================================================
	private void callOnConnect() {
		if (DEBUG) Log.v(TAG, "callOnConnect:");
		mRemoteJoystickEvent.clear();
		try {
			mListener.onConnect(this);
		} catch (final Exception e) {
			Log.w(TAG, e);
		}
	}

	private void callOnDisconnect() {
		if (DEBUG) Log.v(TAG, "callOnDisconnect:");
		mRemoteJoystickEvent.clear();
		try {
			mListener.onDisconnect(this);
		} catch (final Exception e) {
			Log.w(TAG, e);
		}
	}

	private void callOnUpdate() {
//		if (DEBUG) Log.v(TAG, "callOnUpdate:");
		try {
			mListener.onUpdate(this);
		} catch (final Exception e) {
			Log.w(TAG, e);
		}
	}

	private void callOnError(final Exception e) {
		if (DEBUG) Log.v(TAG, "callOnError:");
		mRemoteJoystickEvent.clear();
		try {
			mListener.onError(this, e);
		} catch (final Exception e1) {
			Log.w(TAG, e1);
		}
	}

	/**
	 * Nettyのイベント処理
	 */
	private class RemoteGamePadClientHandler extends ChannelInboundHandlerAdapter {
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
//			if (DEBUG) Log.v(TAG, "channelActive:");
			if (!mReleased) {
				ctx.writeAndFlush(1);	// 何でもいいけどIntegerを送る
				callOnConnect();
			}
		}

		@Override
		public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
			super.channelInactive(ctx);
//			if (DEBUG) Log.v(TAG, "channelInactive:");
			callOnDisconnect();
		}

//		@Override
//		public void channelWritabilityChanged(final ChannelHandlerContext ctx) throws Exception {
//			super.channelWritabilityChanged(ctx);
//			if (DEBUG) Log.v(TAG, "channelWritabilityChanged:");
//		}

		@Override
		public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
			// データを読んだ時の処理
//			if (DEBUG) Log.v(TAG, "channelRead:msg=" + msg);
			if (msg instanceof RemoteJoystickEvent) {
				mRemoteJoystickEvent.set((RemoteJoystickEvent)msg);
				callOnUpdate();
			}
			if (!mReleased) {
				// 次のデータを要求, 何でもいいけどIntegerを送る
				ctx.writeAndFlush(1);
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
			callOnError(new Exception(cause));
		}
	}
}
