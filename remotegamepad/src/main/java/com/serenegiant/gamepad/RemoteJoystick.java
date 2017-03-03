package com.serenegiant.gamepad;

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
 * Created by saki on 2017/03/02.
 *
 */

public class RemoteJoystick extends IGamePad {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private static final String TAG = RemoteJoystick.class.getSimpleName();

//================================================================================
	private final RemoteJoystickEvent mRemoteJoystickEvent = new RemoteJoystickEvent();
	private final SslContext mSslContext;
	private EventLoopGroup mGroup;

	public RemoteJoystick(final String host, final int port)
		throws SSLException, InterruptedException {

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

	private void release() {
		if (mGroup != null) {
			mGroup.shutdownGracefully();
			mGroup = null;
		}
	}

	@Override
	public void updateState(final boolean[] downs, final long[] down_times,
		final int[] analog_sticks, final boolean force) {

		mRemoteJoystickEvent.updateState(downs, down_times, analog_sticks, force);
	}

	private class RemoteGamePadClientHandler extends ChannelInboundHandlerAdapter {
		@Override
		public void channelActive(final ChannelHandlerContext ctx) throws Exception {
			if (DEBUG) Log.v(TAG, "channelActive:");
			ctx.writeAndFlush(1);
		}

		@Override
		public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
			// データを読んだ時の処理
			if (DEBUG) Log.v(TAG, "channelRead:msg=" + msg);
			mRemoteJoystickEvent.set((RemoteJoystickEvent)msg);
			// 次のデータを要求
			ctx.writeAndFlush(1);
		}

		@Override
		public void channelReadComplete(final ChannelHandlerContext ctx) throws Exception {
			if (DEBUG) Log.v(TAG, "channelReadComplete:");
			ctx.flush();
		}

		@Override
		public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
			Log.w(TAG, cause);
			ctx.close();
		}
	}
}
