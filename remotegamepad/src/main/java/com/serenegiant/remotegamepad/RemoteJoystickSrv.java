package com.serenegiant.remotegamepad;

import android.content.Context;
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
 * Created by saki on 2017/03/02.
 *
 */
public class RemoteJoystickSrv {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private static final String TAG = RemoteJoystickSrv.class.getSimpleName();

	public static final boolean ENABLE_SSL = false;

	private final Object mSync = new Object();
	private final RemoteJoystickEvent mEvent = new RemoteJoystickEvent();
	private Joystick mJoystick;

	private final SslContext mSslContext;
	private EventLoopGroup mBossGroup;
	private EventLoopGroup mWorkerGroup;
	private Channel mChannel;
	private volatile boolean mReleased;

	public RemoteJoystickSrv(final Context context, final int port)
		throws CertificateException, SSLException, InterruptedException {

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

	public boolean dispatchKeyEvent(final KeyEvent event) {
		final boolean result = mJoystick != null && mJoystick.dispatchKeyEvent(event);
		if (result) {
			mEvent.set(mJoystick);
		}
		return result;
	}

	public boolean dispatchGenericMotionEvent(final MotionEvent event) {
		final boolean result = mJoystick != null && mJoystick.dispatchGenericMotionEvent(event);
		if (result) {
			mEvent.set(mJoystick);
		}
		return result;
	}

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

//		@Override
//		public void channelActive(final ChannelHandlerContext ctx) throws Exception {
//			super.channelActive(ctx);
//			if (DEBUG) Log.v(TAG, "channelActive:");
//		}

//		@Override
//		public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
//			super.channelInactive(ctx);
//			if (DEBUG) Log.v(TAG, "channelInactive:");
//		}

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
		}
	}

}
