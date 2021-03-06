/*
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  simple-robot-core
 * File     LimitIntercept.java
 *
 * You can contact the author through the following channels:
 * github https://github.com/ForteScarlet
 * gitee  https://gitee.com/ForteScarlet
 * email  ForteScarlet@163.com
 * QQ     1149159218
 *
 */

package com.forte.qqrobot.system.limit;

import com.forte.qqrobot.beans.messages.GroupCodeAble;
import com.forte.qqrobot.beans.messages.QQCodeAble;
import com.forte.qqrobot.beans.messages.msgget.MsgGet;
import com.forte.qqrobot.constant.PriorityConstant;
import com.forte.qqrobot.listener.ListenIntercept;
import com.forte.qqrobot.listener.invoker.ListenInterceptContext;
import com.forte.qqrobot.listener.invoker.ListenerMethod;
import com.forte.qqrobot.utils.AnnotationUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * limit intercept
 * 即限流拦截器。
 *
 *
 * @author <a href="https://github.com/ForteScarlet"> ForteScarlet </a>
 */
public class LimitIntercept implements ListenIntercept {

    /**
     * limit map
     */
    private Map<String, ListenLimit> limitMap = new ConcurrentHashMap<>();

    /**
     * 优先级为+3
     */
    @Override
    public int sort() {
        return PriorityConstant.THIRD;
    }

    /**
     * 拦截执行函数
     *
     * @param context 上下文对象
     * @return 是否放行
     */
    @Override
    public boolean intercept(ListenInterceptContext context) {
        // 啥？为啥下面的东西都不写注释？懒。这次是真的懒了。
        final ListenerMethod listenerMethod = context.getValue();
        final Method method = listenerMethod.getMethod();
        final Limit limit = AnnotationUtils.getAnnotation(method, Limit.class);
        if(limit == null){
            return true;
        }else{
            final MsgGet msgGet = context.getMsgGet();
            final long time = limit.timeUnit().toMillis(limit.value());
            final boolean isGroup = limit.group() && msgGet instanceof GroupCodeAble;
            final boolean isCode = limit.code() && msgGet instanceof QQCodeAble;
            final boolean isBot = limit.bot();

            final StringBuilder keyStringBuilder = new StringBuilder(estimatedLength(method, isGroup, isCode, isBot));
            keyStringBuilder.append(limit.toString()).append(method.toString());
            if(isGroup){
                keyStringBuilder.append(((GroupCodeAble) msgGet).getGroupCode());
            }
            if(isCode){
                keyStringBuilder.append(((QQCodeAble) msgGet).getCode());
            }
            if(isBot){
                keyStringBuilder.append(msgGet.getThisCode());
            }
            final String key = keyStringBuilder.toString();
            final ListenLimit listenLimit = limitMap.computeIfAbsent(key, h -> new ListenLimit(time));
            return listenLimit.expired();
        }
    }

    /**
     * 预估一个StringBuilder的长度
     */
    private static int estimatedLength(Method method, boolean group, boolean code, boolean bot){
        int len = 100 + method.toString().length();
        if(group){
            len += 10;
        }
        if(code){
            len += 10;
        }
        if(bot){
            len += 10;
        }
        return len;
    }

}
