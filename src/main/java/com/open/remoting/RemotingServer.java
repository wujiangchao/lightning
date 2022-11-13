
package com.open.remoting;

import com.open.remoting.rpc.protocol.UserProcessor;

import java.util.concurrent.ExecutorService;

/**
 * @author chengyi (mark.lx@antfin.com) 2018-06-16 06:55
 */
public interface RemotingServer extends Configuration, LifeCycle {


    /**
     * Get the ip of the server.
     *
     * @return ip
     */
    String ip();

    /**
     * Get the port of the server.
     *
     * @return listened port
     */
    int port();

    /**
     * Register processor for command with the command code.
     *
     * @param protocolCode protocol code
     * @param commandCode command code
     * @param processor processor
     */
    void registerProcessor(byte protocolCode, CommandCode commandCode,
                           RemotingProcessor<?> processor);

    /**
     * Register default executor service for server.
     *
     * @param protocolCode protocol code
     * @param executor the executor service for the protocol code
     */
    void registerDefaultExecutor(byte protocolCode, ExecutorService executor);

    /**
     * Register user processor.
     *
     * @param processor user processor which can be a single-interest processor or a multi-interest processor
     */
    void registerUserProcessor(UserProcessor<?> processor);

}
