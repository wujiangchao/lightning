package com.open.remoting.rpc.protocol;

import com.open.remoting.BizContext;
import com.open.remoting.LifeCycle;

/**
 * @Description 业务逻辑用户处理器顶层接口
 * @Date 2022/11/13 15:53
 * @Author jack wu
 */
public interface UserProcessor<T> extends LifeCycle {
    /**
     * Pre handle request, to avoid expose {@link RemotingContext} directly to biz handle request logic.
     *
     * @param remotingCtx remoting context
     * @param request     request
     * @return BizContext
     */
    BizContext preHandleRequest(RemotingContext remotingCtx, T request);

    /**
     * Handle request with {@link AsyncContext}.
     *
     * @param bizCtx   biz context
     * @param asyncCtx async context
     * @param request  request
     */
    void handleRequest(BizContext bizCtx, AsyncContext asyncCtx, T request);

    /**
     * Handle request in sync way.
     *
     * @param bizCtx  biz context
     * @param request request
     */
    Object handleRequest(BizContext bizCtx, T request) throws Exception;

    /**
     * The class name of user request.
     * Use String type to avoid classloader problem.
     * 指定感兴趣的请求数据类型，该 UserProcessor 只对感兴趣的请求类型的数据进行处理；
     * 假设 除了需要处理 MyRequest 类型的数据，还要处理 java.lang.String 类型，有两种方式：
     * 1、再提供一个 UserProcessor 实现类，其 interest() 返回 java.lang.String.class.getName()
     * 2、使用 MultiInterestUserProcessor 实现类，可以为一个 UserProcessor 指定 List<String> multiInterest()
     *
     * @return interested request's class name
     */
    String interest();


}
