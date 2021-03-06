/*
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  simple-robot-core
 * File     AbstractMsgGet.java
 *
 * You can contact the author through the following channels:
 * github https://github.com/ForteScarlet
 * gitee  https://gitee.com/ForteScarlet
 * email  ForteScarlet@163.com
 * QQ     1149159218
 *
 */

package com.forte.qqrobot.beans.messages.msgget;

import java.util.StringJoiner;

/**
 * 接口{@link MsgGet}对应的抽象类
 * @author ForteScarlet <[email]ForteScarlet@163.com>
 * @since JDK1.8
 **/
public abstract class AbstractMsgGet implements MsgGet {

    /**
     * 原始数据字符串
     */
    private String originalData;
    /**
     * 收到消息的机器人账号
     */
    private String thisCode;
    /** 消息ID */
    private String id;
    /** 消息文本 */
    private String msg;
    /** 大概是字体ID */
    private String font;
    /** 发消息的时间戳，一般来讲是秒值 */
    private Long time;

    @Override
    public String getOriginalData() {
        return originalData;
    }

    public void setOriginalData(String originalData) {
        this.originalData = originalData;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getMsg() {
        return msg;
    }

    @Override
    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String getFont() {
        return font;
    }

    public void setFont(String font) {
        this.font = font;
    }

    @Override
    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    @Override
    public void setThisCode(String code){
        this.thisCode = code;
    }

    @Override
    public String getThisCode(){
        return this.thisCode;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", "MsgGet[", "]")
                .add("id='" + getId() + "'")
                .add("msg='" + getMsg() + "'")
                .add("font='" + getFont() + "'")
                .add("time=" + getTime())
                .add("originalData='" + getOriginalData() + "'")
                .toString();
    }
}
