package com.open.remoting.rpc;

import com.open.remoting.AbstractRemotingServer;
import com.open.remoting.Connection;
import com.open.remoting.NamedThreadFactory;
import com.open.remoting.Url;
import com.open.remoting.config.ConfigManager;
import com.open.remoting.rpc.protocol.UserProcessor;
import com.open.remoting.util.NettyEventLoopUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.flush.FlushConsolidationHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;

import javax.net.ssl.SSLEngine;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @Description TODO
 * @Date 2022/11/13 16:18
 * @Author jack wu
 */
public class RpcServer extends AbstractRemotingServer {

    /**
     * logger
     */
    private static final Logger logger = BoltLoggerFactory
            .getLogger("RpcRemoting");
    /**
     * server bootstrap
     */
    private ServerBootstrap bootstrap;

    /**
     * boss event loop group, boss group should not be daemon, need shutdown manually
     */
    private final EventLoopGroup bossGroup = NettyEventLoopUtil
            .newEventLoopGroup(
                    1,
                    new NamedThreadFactory(
                            "Rpc-netty-server-boss",
                            false));
    /**
     * worker event loop group. Reuse I/O worker threads between rpc servers.
     */
    private static final EventLoopGroup workerGroup = NettyEventLoopUtil
            .newEventLoopGroup(
                    Runtime
                            .getRuntime()
                            .availableProcessors() * 2,
                    new NamedThreadFactory(
                            "Rpc-netty-server-worker",
                            true));

    @Override
    protected void doInit() {

        //启动器 负责组装netty组件，启动服务器
        this.bootstrap = new ServerBootstrap();
        this.bootstrap
                //
                .group(bossGroup, workerGroup)
                //选择服务器的 serverSockerChannel实现
                .channel(NettyEventLoopUtil.getServerSocketChannelClass())
                .option(ChannelOption.SO_BACKLOG, ConfigManager.tcp_so_backlog())
                .option(ChannelOption.SO_REUSEADDR, ConfigManager.tcp_so_reuseaddr())
                .childOption(ChannelOption.TCP_NODELAY, ConfigManager.tcp_nodelay())
                .childOption(ChannelOption.SO_KEEPALIVE, ConfigManager.tcp_so_keepalive())
                .childOption(ChannelOption.SO_SNDBUF,
                        tcpSoSndBuf != null ? tcpSoSndBuf : ConfigManager.tcp_so_sndbuf())
                .childOption(ChannelOption.SO_RCVBUF,
                        tcpSoRcvBuf != null ? tcpSoRcvBuf : ConfigManager.tcp_so_rcvbuf());

        this.bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            //链接建立后 会初始化
            @Override
            protected void initChannel(SocketChannel channel) {
                ChannelPipeline pipeline = channel.pipeline();
                ExtendedNettyChannelHandler extendedHandlers = option(BoltServerOption.EXTENDED_NETTY_CHANNEL_HANDLER);
                if (extendedHandlers != null) {
                    List<ChannelHandler> frontHandlers = extendedHandlers.frontChannelHandlers();
                    if (frontHandlers != null) {
                        for (ChannelHandler channelHandler : frontHandlers) {
                            pipeline.addLast(channelHandler.getClass().getName(), channelHandler);
                        }
                    }
                }

                Boolean sslEnable = option(BoltServerOption.SRV_SSL_ENABLE);
                if (!sslEnable) {
                    // fixme: remove in next version
                    sslEnable = RpcConfigManager.server_ssl_enable();
                }
                if (sslEnable) {
                    SSLEngine engine = initSSLContext().newEngine(channel.alloc());
                    engine.setUseClientMode(false);
                    // fixme: update in next version
                    engine.setNeedClientAuth(option(BoltServerOption.SRV_SSL_NEED_CLIENT_AUTH)
                            || RpcConfigManager.server_ssl_need_client_auth());
                    pipeline.addLast(Constants.SSL_HANDLER, new SslHandler(engine));
                }

                if (flushConsolidationSwitch) {
                    pipeline.addLast("flushConsolidationHandler", new FlushConsolidationHandler(
                            1024, true));
                }
                pipeline.addLast("decoder", codec.newDecoder());
                pipeline.addLast("encoder", codec.newEncoder());
                if (idleSwitch) {
                    pipeline.addLast("idleStateHandler", new IdleStateHandler(0, 0, idleTime,
                            TimeUnit.MILLISECONDS));
                    pipeline.addLast("serverIdleHandler", serverIdleHandler);
                }
                pipeline.addLast("connectionEventHandler", connectionEventHandler);
                pipeline.addLast("handler", rpcHandler);
                if (extendedHandlers != null) {
                    List<ChannelHandler> backHandlers = extendedHandlers.backChannelHandlers();
                    if (backHandlers != null) {
                        for (ChannelHandler channelHandler : backHandlers) {
                            pipeline.addLast(channelHandler.getClass().getName(), channelHandler);
                        }
                    }
                }
                createConnection(channel);
            }

            /**
             * create connection operation<br>
             * <ul>
             * <li>If flag manageConnection be true, use {@link DefaultConnectionManager} to add a new connection, meanwhile bind it with the channel.</li>
             * <li>If flag manageConnection be false, just create a new connection and bind it with the channel.</li>
             * </ul>
             */
            private void createConnection(SocketChannel channel) {
                Url url = addressParser.parse(RemotingUtil.parseRemoteAddress(channel));
                if (option(BoltServerOption.SERVER_MANAGE_CONNECTION_SWITCH)) {
                    connectionManager.add(new Connection(channel, url), url.getUniqueKey());
                } else {
                    new Connection(channel, url);
                }
                channel.pipeline().fireUserEventTriggered(ConnectionEventType.CONNECT);
            }
        });
    }

    @Override
    protected boolean doStart() throws InterruptedException {
        return false;
    }

    @Override
    protected boolean doStop() {
        return false;
    }

    @Override
    public boolean isStarted() {
        return false;
    }

    @Override
    public void registerProcessor(byte protocolCode, CommandCode commandCode, RemotingProcessor<?> processor) {

    }

    @Override
    public void registerDefaultExecutor(byte protocolCode, ExecutorService executor) {

    }

    @Override
    public void registerUserProcessor(UserProcessor<?> processor) {

    }
}
