
package com.open.remoting;

/**
 * basic info for biz
 */
public interface BizContext {
    /**
     * get remote address
     *
     * @return remote address
     */
    String getRemoteAddress();

    /**
     * get remote host ip
     *
     * @return remote host
     */
    String getRemoteHost();

    /**
     * get remote port
     *
     * @return remote port
     */
    int getRemotePort();

    /**
     * get the connection of this request
     *
     * @return connection
     */
    Connection getConnection();

    /**
     * check whether request already timeout
     *
     * @return true if already timeout, you can log some useful info and then discard this request.
     */
    boolean isRequestTimeout();

    /**
     * get the timeout value from rpc client.
     *
     * @return client timeout
     */
    int getClientTimeout();

    /**
     * get the arrive time stamp
     *
     * @return the arrive time stamp
     */
    long getArriveTimestamp();

    /**
     * put a key and value
     */
    void put(String key, String value);

    /**
     * get value
     *
     * @param key target key
     * @return value
     */
    String get(String key);

    /**
     * get invoke context.
     *
     * @return InvokeContext
     */
    InvokeContext getInvokeContext();
}