package com.open.remoting.config;

/**
 * @Description TODO
 * @Date 2022/11/13 16:24
 * @Author jack wu
 */
public class ConfigManager {

    public static boolean netty_epoll() {
        return getBool(Configs.NETTY_EPOLL_SWITCH, Configs.NETTY_EPOLL_SWITCH_DEFAULT);
    }
}
