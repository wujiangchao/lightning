
package com.open.remoting;

/**
 * @author jack.wu
 */
public interface LifeCycle {

    void startup() throws LifeCycleException;

    void shutdown() throws LifeCycleException;

    boolean isStarted();
}
