
package com.open.remoting;

import org.slf4j.Logger;

import java.net.InetSocketAddress;

/**
 * Server template for remoting.
 *
 * @author jiangping
 * @version $Id: AbstractRemotingServer.java, v 0.1 2015-9-5 PM7:37:48 tao Exp $
 */
public abstract class AbstractRemotingServer extends AbstractLifeCycle implements RemotingServer,
                                                                      ConfigurableInstance {

    private static final Logger   logger = BoltLoggerFactory.getLogger("CommonDefault");

    private String                ip;
    private int                   port;

    private final BoltOptions     options;

    public AbstractRemotingServer(int port) {
        this(new InetSocketAddress(port).getAddress().getHostAddress(), port);
    }

    public AbstractRemotingServer(String ip, int port) {
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException(String.format(
                "Illegal port value: %d, which should between 0 and 65535.", port));
        }
        this.ip = ip;
        this.port = port;

        this.options = new BoltOptions();
    }


    @Override
    public void startup() throws LifeCycleException {
        super.startup();
        try {
            doInit();

            logger.warn("Prepare to start server on port {} ", port);
            if (doStart()) {
                logger.warn("Server started on port {}", port);
            } else {
                logger.warn("Failed starting server on port {}", port);
                throw new LifeCycleException("Failed starting server on port: " + port);
            }
        } catch (Throwable t) {
            this.shutdown();// do stop to ensure close resources created during doInit()
            throw new IllegalStateException("ERROR: Failed to start the Server!", t);
        }
    }

    @Override
    public void shutdown() throws LifeCycleException {
        super.shutdown();
        if (!doStop()) {
            throw new LifeCycleException("doStop fail");
        }
    }

    @Override
    public String ip() {
        return ip;
    }

    @Override
    public int port() {
        return port;
    }

    /**
     * override the random port zero with the actual binding port value.
     * @param port local binding port
     */
    protected void setLocalBindingPort(int port) {
        if (port() == 0) {
            this.port = port;
        }
    }

    protected abstract void doInit();

    protected abstract boolean doStart() throws InterruptedException;

    protected abstract boolean doStop();

    @Override
    public <T> T option(BoltOption<T> option) {
        return options.option(option);
    }

    @Override
    public <T> Configuration option(BoltOption<T> option, T value) {
        options.option(option, value);
        return this;
    }


    @Override
    public void initWriteBufferWaterMark(int low, int high) {
        option(BoltServerOption.NETTY_BUFFER_LOW_WATER_MARK, low);
        option(BoltServerOption.NETTY_BUFFER_HIGH_WATER_MARK, high);
    }

    @Override
    public int netty_buffer_low_watermark() {
        return option(BoltServerOption.NETTY_BUFFER_LOW_WATER_MARK);
    }

    @Override
    public int netty_buffer_high_watermark() {
        return option(BoltServerOption.NETTY_BUFFER_HIGH_WATER_MARK);
    }
}
