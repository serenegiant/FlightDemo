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
		Channel channel;
		synchronized (mSync) {
			channel = mChannel;
			mChannel = null;
		}
		if (channel != null) {
			try {
				channel.closeFuture().sync();
			} catch (final InterruptedException e) {
				// ignore
			}
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
		@Override
		public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
			// 何か送られてきたらRemoteKeyEventを送り返す
			ctx.writeAndFlush(mEvent);
		}

		@Override
		public void channelReadComplete(final ChannelHandlerContext ctx) throws Exception {
			ctx.flush();
		}

		@Override
		public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
			Log.w(TAG, cause);
			ctx.close();
		}
	}

}
