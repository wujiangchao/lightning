package com.open.remoting.util;

import com.open.remoting.config.ConfigManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollMode;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.ThreadFactory;

/**
 * @Description TODO
 * @Date 2022/11/13 16:22
 * @Author jack wu
 */
public class NettyEventLoopUtil {

    /** check whether epoll enabled, and it would not be changed during runtime. */
    private static boolean epollEnabled = ConfigManager.netty_epoll() && Epoll.isAvailable();

    /**
     * Create the right event loop according to current platform and system property, fallback to NIO when epoll not enabled.
     *
     * @param nThreads
     * @param threadFactory
     * @return an EventLoopGroup suitable for the current platform
     */
    public static EventLoopGroup newEventLoopGroup(int nThreads, ThreadFactory threadFactory) {
        return epollEnabled ? new EpollEventLoopGroup(nThreads, threadFactory)
                : new NioEventLoopGroup(nThreads, threadFactory);
    }

}
