/*
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  simple-robot-core
 * File     CQCodeUtil.java
 *
 * You can contact the author through the following channels:
 * github https://github.com/ForteScarlet
 * gitee  https://gitee.com/ForteScarlet
 * email  ForteScarlet@163.com
 * QQ     1149159218
 *
 */

package com.forte.qqrobot.utils;

import com.forte.qqrobot.beans.cqcode.AppendList;
import com.forte.qqrobot.beans.cqcode.CQAppendList;
import com.forte.qqrobot.beans.cqcode.CQCode;
import com.forte.qqrobot.beans.messages.msgget.MsgGet;
import com.forte.qqrobot.beans.types.CQCodeTypes;
import com.forte.qqrobot.exception.CQParseException;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * CQ码生成器
 *
 *
 * 你可以试着换成cqCodeUtils模组：http://simple-robot-doc.forte.love/1644790
 * 而不是用这个内置的类
 *
 * @deprecated v1.16.0中默认集成了cqCodeUtils模组, 所以你可以直接使用KQCodeUtils类，并弃用原版工具类
 *
 * @see com.simplerobot.modules.utils.KQCodeUtils
 * @see <a href='http://simple-robot-doc.forte.love/1644790'>simple-robot-doc/1644790</a>
 *
 * @author ForteScarlet <[163邮箱地址]ForteScarlet@163.com>
 * @date Created in 2019/3/8 14:37
 * @since JDK1.8
 **/
@Deprecated
public class CQCodeUtil {

    /**
     * CQ码开头
     */
    private static final String CQ_CODE_HEAD = "[CQ:";
    /**
     * CQ码结尾
     */
    private static final String CQ_CODE_END = "]";
    /**
     * CQ码k-v分割符
     */
    private static final String CQ_CODE_SPLIT = ",";

    /**
     * 仅存在一个单例
     */
    private static final CQCodeUtil CQ_CODE_UTIL = new CQCodeUtil();

    /**
     * 获取CQCodeUtil实例对象
     */
    public static CQCodeUtil build() {
        return CQ_CODE_UTIL;
    }

    /**
     * 构造私有化
     */
    private CQCodeUtil() {
    }

    /**
     * 获取CQ码生成用StringJoiner
     *
     * @return
     */
    private static StringJoiner getCQCodeJoiner() {
        return new StringJoiner(CQ_CODE_SPLIT, CQ_CODE_HEAD, CQ_CODE_END);
    }

    /**
     * 字符串转义-不在CQ码内的消息
     * 对于不在CQ码内的消息（即文本消息），为了防止解析混淆，需要进行转义。
     * 转义规则如下：
     * & -> &amp;
     * [ -> &#91;
     * ] -> &#93;
     *
     * @return 转义后的字符串
     */
    public String escapeOutCQCode(String msgOutCQCode) {
        return msgOutCQCode == null ? null : msgOutCQCode
                .replace("&", "&amp;")
                .replace("[", "&#91;")
                .replace("]", "&#93;")
                ;
    }


    /**
     * 将一个没有CQ码的字符串进行解码
     * nullable
     *
     * @param noCQCodeMsg
     * @return
     */
    public String escapeOutCQCodeDecode(String noCQCodeMsg) {
        return noCQCodeMsg == null ? null : noCQCodeMsg
                .replace("&mp;", "&")
                .replace("&91;", "[")
                .replace("&93;", "]")
                ;
    }


    /**
     * 对于CQ码中的value（参数值），为了防止解析混淆，需要进行转义。
     * 此方法大概仅需要内部使用
     * 转义规则如下：
     * & -> &amp;
     * [ -> &#91;
     * ] -> &#93;
     * , -> &#44;
     *
     * @param value 参数值的字符串
     * @return 转义后的字符串
     */
    public String escapeValue(String value) {
        return value == null ? null : value
                .replace("&", "&amp;")
                .replace("[", "&#91;")
                .replace("]", "&#93;")
                .replace(",", "&#44;")
                ;
    }

    /**
     * 对参数进行解码
     *
     * @param value 参数值的字符串
     * @return 节码后的字符串
     */
    public String escapeValueDecode(String value) {
        return value == null ? null : value
                .replace("&amp;", "&")
                .replace("&#91;", "[")
                .replace("&#93;", "]")
                .replace("&#44;", ",")
                ;
    }


    /**
     * CQCode字符串生成
     *
     * @param type   CQCode类型
     * @param params 参数，将会根据现有的参数按照对应的索引进行赋值，
     *               如果对应索引为null则视为忽略参数
     * @return CQCode字符串
     */
    private String getCQCodeString(CQCodeTypes type, String... params) {
        Objects.requireNonNull(params);
        //获取此CQ码的类型
        String function = type.getFunction();
        //获取joiner
        StringJoiner joiner = getCQCodeJoiner();
        joiner.add(function);
        //获取参数列表
        String[] keys = type.getKeys();
        //理论上，参数的数量应该不会小于keys的数量，如果长度不足，以null补位

        //遍历参数列表
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            //获取value值并转义，如果参数不存在则标记为null
            //如果参数的索引不足类型的索引，以null补位
            String value = Optional.ofNullable(params.length >= (i + 1) ? params[i] : null).map(this::escapeValue).orElse(null);
            //如果参数不为null则说明不需要忽略
            if (value != null) {
                joiner.add(key + "=" + value);
            }
        }
        return joiner.toString();
    }

    public CQCode getCQCode(CQCodeTypes type, String... params) {
        Objects.requireNonNull(params);
        //获取参数列表
        String[] keys = type.getKeys();
        //理论上，参数的数量应该不会小于keys的数量，如果长度不足，以null补位
        String[] realParams = new String[params.length];
        //遍历参数列表
        for (int i = 0; i < realParams.length; i++) {
            String key;
            if(keys.length > i){
                key = keys[i];
            }else{
                key = Integer.toString(i);
            }
            //获取value值并转义，如果参数不存在则标记为null
            //如果参数的索引不足类型的索引，以null补位
            String value = Optional.ofNullable(params.length >= (i + 1) ? params[i] : null).map(this::escapeValue).orElse(null);
            //如果参数不为null则说明不需要忽略
            if (value != null) {
                realParams[i] = key + "=" + value;
            } else {
                realParams[i] = key;
            }
        }
        return CQCode.of(type, realParams);
    }



    /* ———————————————————— CQ码获取方法 ———————————————————— */

    /**
     * 获取face字符表情
     *
     * @param id moji字符的unicode编号
     * @return emoji的CQ码
     * @see #getCQCode_Face(String)
     * @deprecated
     */
    public String getCQCode_face(String id) {
        return getCQCodeString(CQCodeTypes.face, id);
    }

    /**
     * 获取face字符表情
     *
     * @param id moji字符的unicode编号
     * @return emoji的CQ码
     */
    public CQCode getCQCode_Face(String id) {
        return getCQCode(CQCodeTypes.face, id);
    }

    /**
     * 获取原创表情
     *
     * @param id 原创表情的ID
     * @return 原创表情CQ
     * @see #getCQCode_Bface(String)
     * @deprecated
     */
    public String getCQCode_bface(String id) {
        return getCQCodeString(CQCodeTypes.bface, id);
    }

    /**
     * 获取原创表情
     *
     * @param id 原创表情的ID
     * @return 原创表情CQ
     */
    public CQCode getCQCode_Bface(String id) {
        return getCQCode(CQCodeTypes.bface, id);
    }

    /**
     * 小表情
     *
     * @param id 小表情的ID
     * @return 小表情CQ
     * @see #getCQCode_Sface(String)
     * @deprecated
     */
    public String getCQCode_sface(String id) {
        return getCQCodeString(CQCodeTypes.sface, id);
    }

    /**
     * 小表情
     *
     * @param id 小表情的ID
     * @return 小表情CQ
     */
    public CQCode getCQCode_Sface(String id) {
        return getCQCode(CQCodeTypes.sface, id);
    }

    /**
     * 发送自定义图片
     *
     * @param file 图片文件名称
     * @return 自定义图片CQ
     * @see #getCQCode_Image(String)
     * @deprecated
     */
    public String getCQCode_image(String file) {
        return getCQCodeString(CQCodeTypes.image, file);
    }

    /**
     * 发送自定义图片
     *
     * @param file 图片文件名称
     * @return 自定义图片CQ
     */
    public CQCode getCQCode_Image(String file) {
        return getCQCode(CQCodeTypes.image, file);
    }


    /**
     * 发送语音
     *
     * @param file  音频文件名称
     * @param magic 是否为变声
     * @return 发送语音CQ
     * @see #getCQCode_Record(String, Boolean)
     * @deprecated
     */
    public String getCQCode_record(String file, Boolean magic) {
        return getCQCodeString(CQCodeTypes.record, file, Optional.ofNullable(magic).map(Object::toString).orElse(null));
    }

    /**
     * 发送语音
     *
     * @param file  音频文件名称
     * @param magic 是否为变声
     * @return 发送语音CQ
     */
    public CQCode getCQCode_Record(String file, Boolean magic) {
        return getCQCode(CQCodeTypes.record, file, Optional.ofNullable(magic).map(Object::toString).orElse(null));
    }

    /**
     * 发送语音
     *
     * @param file 音频文件名称
     * @return 发送语音CQ
     * @see #getCQCode_Record(String)
     * @deprecated
     */
    public String getCQCode_record(String file) {
        return getCQCodeString(CQCodeTypes.record, file, null);
    }

    /**
     * 发送语音
     *
     * @param file 音频文件名称
     * @return 发送语音CQ
     */
    public CQCode getCQCode_Record(String file) {
        return getCQCode(CQCodeTypes.record, file, null);
    }


    /**
     * at某人
     *
     * @param qq qq号
     * @return at CQcode
     * @see #getCQCode_At(String)
     * @deprecated
     */
    public String getCQCode_at(String qq) {
        return getCQCodeString(CQCodeTypes.at, qq);
    }

    /**
     * at某人
     *
     * @param qq qq号
     * @return at CQcode
     */
    public CQCode getCQCode_At(String qq) {
        return getCQCode(CQCodeTypes.at, qq);
    }

    /**
     * at全体
     *
     * @return at CQcode
     * @see #getCQCode_At(String)
     * @deprecated
     */
    public String getCQCode_atAll() {
        return getCQCodeString(CQCodeTypes.at, "all");
    }

    /**
     * at全体
     *
     * @return at CQcode
     */
    public CQCode getCQCode_AtAll() {
        return getCQCode(CQCodeTypes.at, "all");
    }


    /**
     * 发送猜拳魔法表情
     *
     * @param type 为猜拳结果的类型，暂不支持发送时自定义。该参数可被忽略。
     * @return 猜拳魔法表情CQCode
     * @see #getCQCode_Rps(String)
     * @deprecated
     */
    public String getCQCode_rps(String type) {
        return getCQCodeString(CQCodeTypes.rps, type);
    }

    /**
     * 发送猜拳魔法表情
     *
     * @param type 为猜拳结果的类型，暂不支持发送时自定义。该参数可被忽略。
     * @return 猜拳魔法表情CQCode
     */
    public CQCode getCQCode_Rps(String type) {
        return getCQCode(CQCodeTypes.rps, type);
    }

    /**
     * 掷骰子魔法表情
     *
     * @param type 对应掷出的点数，暂不支持发送时自定义。该参数可被忽略。
     * @return 掷骰子魔法表情CQCode
     * @see #getCQCode_Dice(String)
     * @deprecated
     */
    public String getCQCode_dice(String type) {
        return getCQCodeString(CQCodeTypes.dice, type);
    }

    /**
     * 掷骰子魔法表情
     *
     * @param type 对应掷出的点数，暂不支持发送时自定义。该参数可被忽略。
     * @return 掷骰子魔法表情CQCode
     */
    public CQCode getCQCode_Dice(String type) {
        return getCQCode(CQCodeTypes.dice, type);
    }

    /**
     * 戳一戳 仅支持好友消息使用
     *
     * @return 戳一戳CQCode
     * @see #getCQCode_Shake()
     * @deprecated
     */
    public String getCQCode_shake() {
        return getCQCodeString(CQCodeTypes.shake);
    }

    /**
     * 戳一戳 仅支持好友消息使用
     *
     * @return 戳一戳CQCode
     */
    public CQCode getCQCode_Shake() {
        return getCQCode(CQCodeTypes.shake);
    }

    /**
     * 匿名发消息（仅支持群消息使用）
     * 本CQ码需加在消息的开头。
     *
     * @param ignore 当{1}为true时，代表不强制使用匿名，如果匿名失败将转为普通消息发送。
     *               当{1}为false或ignore参数被忽略时，代表强制使用匿名，如果匿名失败将取消该消息的发送。<br>
     * @return 匿名发消息
     * @see #getCQCode_Anonymous(Boolean)
     * @deprecated
     */
    public String getCQCode_anonymous(Boolean ignore) {
        return getCQCodeString(CQCodeTypes.anonymous, Optional.ofNullable(ignore).map(Object::toString).orElse(null));
    }

    /**
     * 匿名发消息（仅支持群消息使用）
     * 本CQ码需加在消息的开头。
     *
     * @param ignore 当{1}为true时，代表不强制使用匿名，如果匿名失败将转为普通消息发送。
     *               当{1}为false或ignore参数被忽略时，代表强制使用匿名，如果匿名失败将取消该消息的发送。<br>
     * @return 匿名发消息
     */
    public CQCode getCQCode_Anonymous(Boolean ignore) {
        return getCQCode(CQCodeTypes.anonymous, Optional.ofNullable(ignore).map(Object::toString).orElse(null));
    }

    /**
     * 匿名发消息（仅支持群消息使用）
     * 本CQ码需加在消息的开头。
     * 参数被忽略，代表强制使用匿名，如果匿名失败将取消该消息的发送。
     *
     * @return 匿名发消息
     * @see #getCQCode_Anonymous()
     * @deprecated
     */
    public String getCQCode_anonymous() {
        return getCQCodeString(CQCodeTypes.anonymous, (String) null);
    }

    /**
     * 匿名发消息（仅支持群消息使用）
     * 本CQ码需加在消息的开头。
     * 参数被忽略，代表强制使用匿名，如果匿名失败将取消该消息的发送。
     *
     * @return 匿名发消息
     */
    public CQCode getCQCode_Anonymous() {
        return getCQCode(CQCodeTypes.anonymous, (String) null);
    }


    /**
     * 发送音乐
     * 注意：音乐只能作为单独的一条消息发送
     *
     * @param type 为音乐平台类型，目前支持qq、163、xiami
     * @param id   为对应音乐平台的数字音乐id
     * @return 发送音乐
     * @see #getCQCode_Music(String, String)
     * @deprecated
     */
    public String getCQCode_music(String type, String id) {
        return getCQCodeString(CQCodeTypes.music, type, id);
    }

    /**
     * 发送音乐
     * 注意：音乐只能作为单独的一条消息发送
     *
     * @param type 为音乐平台类型，目前支持qq、163、xiami
     * @param id   为对应音乐平台的数字音乐id
     * @return 发送音乐
     */
    public CQCode getCQCode_Music(String type, String id) {
        return getCQCode(CQCodeTypes.music, type, id);
    }

    /**
     * 发送音乐自定义分享
     *
     * @param url     为分享链接，即点击分享后进入的音乐页面（如歌曲介绍页）。
     * @param audio   为音频链接（如mp3链接）。
     * @param title   为音乐的标题，建议12字以内。
     * @param content 为音乐的简介，建议30字以内。该参数可被忽略。
     * @param image   为音乐的封面图片链接。若参数为空或被忽略，则显示默认图片。
     * @return 音乐自定义分享CQCode
     * @see #getCQCode_Music_Custom(String, String, String, String, String)
     * @deprecated
     */
    public String getCQCode_music_custom(String url, String audio, String title, String content, String image) {
        return getCQCodeString(CQCodeTypes.music_custom, "custom", url, audio, title, content, image);
    }

    /**
     * 发送音乐自定义分享
     *
     * @param url     为分享链接，即点击分享后进入的音乐页面（如歌曲介绍页）。
     * @param audio   为音频链接（如mp3链接）。
     * @param title   为音乐的标题，建议12字以内。
     * @param content 为音乐的简介，建议30字以内。该参数可被忽略。
     * @param image   为音乐的封面图片链接。该参数可被忽略。若参数为空或被忽略，则显示默认图片。
     * @return 音乐自定义分享CQCode
     */
    public CQCode getCQCode_Music_Custom(String url, String audio, String title, String content, String image) {
        return getCQCode(CQCodeTypes.music_custom, "custom", url, audio, title, content, image);
    }

    /**
     * 发送链接分享
     * 注意：链接分享只能作为单独的一条消息发送
     *
     * @param url     为分享链接。
     * @param title   为分享的标题，建议12字以内。
     * @param content 为分享的简介，建议30字以内。该参数可被忽略。
     * @param image   为分享的图片链接。若参数为空或被忽略，则显示默认图片。
     * @return 链接分享CQCode
     * @see #getCQCode_Share(String, String, String, String)
     * @deprecated
     */
    public String getCQCode_share(String url, String title, String content, String image) {
        return getCQCodeString(CQCodeTypes.share, url, title, content, image);
    }

    /**
     * 发送链接分享
     * 注意：链接分享只能作为单独的一条消息发送
     *
     * @param url     为分享链接。
     * @param title   为分享的标题，建议12字以内。
     * @param content 为分享的简介，建议30字以内。该参数可被忽略。
     * @param image   为分享的图片链接。若参数为空或被忽略，则显示默认图片。
     * @return 链接分享CQCode
     */
    public CQCode getCQCode_Share(String url, String title, String content, String image) {
        return getCQCode(CQCodeTypes.share, url, title, content, image);
    }

    /**
     * 生成emoji
     *
     * @param id emoji的id
     * @return emoji的CQCode
     * @see #getCQCode_Emoji(String)
     * @deprecated
     */
    public String getCQCode_emoji(String id) {
        return getCQCodeString(CQCodeTypes.emoji, id);
    }

    /**
     * 生成emoji
     *
     * @param id emoji的id
     * @return emoji的CQCode
     */
    public CQCode getCQCode_Emoji(String id) {
        return getCQCode(CQCodeTypes.emoji, id);
    }


    //**************** CQ码辅助方法 ****************//


    /**
     * 用于从字符串中提取CQCode码字符串的正则表达式
     */
    private static final String CQCODE_EXTRACT_REGEX = CQCodeTypes.getCqcodeExtractRegex();


    /**
     * 从信息字符串中提取出CQCode码的字符串
     *
     * @param msg 信息字符串
     * @return 提取出CQCode码的字符串
     */
    public List<String> getCQCodeStrFromMsg(String msg) {
        return substringCQCodeByHead(msg, "[CQ:");
    }

    /**
     * 从信息字符串中提取出指定类型的CQCode码的字符串
     * @param msg   消息字符串
     * @param types CQ码类型
     * @return
     */
    public List<String> getCQCodeStrFromMsgByType(String msg, CQCodeTypes types) {
        String functionHead;
        try {
            functionHead = types.getFunctionHead();
        }catch (NoSuchMethodError e){
            functionHead = "[CQ:" + types.getFunction();
        }
        return substringCQCodeByHead(msg, functionHead);
    }

    /**
     * 从信息字符串中提取出指定类型的CQCode码的字符串
     * @param msg   消息字符串
     * @return
     */
    public List<String> getCQCodeStrFromMsgByType(String msg, String typeFunction) {
        String functionHead = "[CQ:" + typeFunction;
        return substringCQCodeByHead(msg, functionHead);
    }

    /**
     * 根据CQ码的开头截取存在的字符串
     * @param msg           消息字符串
     * @param functionHead  CQ码开头
     */
    private List<String> substringCQCodeByHead(String msg, String functionHead){
        final String functionEnd = "]";
        List<String> cqList = new ArrayList<>();
        int start = -1;
        int end = -1;

        do {
            if (start >= 0) {
                cqList.add(msg.substring(start, end + 1));
            }
            start = msg.indexOf(functionHead, start + 1);
            end = msg.indexOf(functionEnd, start + 1);
        } while (start >= 0 && end >= 0);

        return cqList;
    }


    /**
     * 从信息字符串中移除CQCode字符串
     *
     * @param msg 字符串
     * @return 移除后的字符串
     */
    public String removeCQCodeFromMsg(String msg) {
        return removeCQCodeFromMsg(msg, null);
    }

    /**
     * 从信息字符串中移除某种类型的CQCode字符串
     *
     * @param msg         字符串
     * @param cqCodeTypes CQ码类型
     * @return 移除后的字符串
     */
    public String removeCQCodeFromMsg(String msg, CQCodeTypes cqCodeTypes) {
        return msg.replaceAll(cqCodeTypes == null ? CQCODE_EXTRACT_REGEX : cqCodeTypes.getMatchRegex(), "");
    }

    /**
     * 从信息字符串中提取出CQCode码对象
     *
     * @param msg 信息字符串 如果为空则返回空字符串
     * @return 提取出CQCode码对象
     */
    public List<CQCode> getCQCodeFromMsg(String msg) {
        if (msg == null || msg.trim().length() <= 0) {
            return Collections.emptyList();
        }
        //CQ码list集合
        List<String> cqStrList = getCQCodeStrFromMsg(msg);
        return cqStrList.stream()
                //移除[CQ:和],在以逗号分隔
                .map(CQCode::of).collect(Collectors.toList());
    }

    /**
     * 从信息字符串中提取出指定类型的CQCode码对象
     *
     * @param msg   字符串
     * @param types 类型
     * @return 指定类型的CQ码类型
     */
    public List<CQCode> getCQCodeFromMsgByType(String msg, CQCodeTypes types) {
        if (msg == null || msg.trim().length() <= 0) {
            return Collections.emptyList();
        }
        //CQ码list集合
        List<String> cqStrList = getCQCodeStrFromMsgByType(msg, types);
        return cqStrList.stream().map(CQCode::of).collect(Collectors.toList());
    }

    /**
     * 从信息字符串中提取出指定类型的CQCode码对象
     *
     * @param msg   字符串
     * @param typeStr 类型字符串
     * @return 指定类型的CQ码类型
     */
    public List<CQCode> getCQCodeFromMsgByType(String msg, String typeStr) {
        if (msg == null || msg.trim().length() <= 0) {
            return Collections.emptyList();
        }
        //CQ码list集合
        List<String> cqStrList = getCQCodeStrFromMsgByType(msg, typeStr);
        return cqStrList.stream().map(CQCode::of).collect(Collectors.toList());
    }


    public boolean isAt(MsgGet msgget){
        return isAt(msgget.getMsg(), msgget.getThisCode());
    }

    /**
     * 判断是否存在at某个qq
     *
     * @return 是否at了某个qq
     */
    public boolean isAt(String msg, String qq) {
        if (msg == null) {
            return false;
        }
        //如果存在at的CQ码并且参数‘qq’是某个qq
        return msg.contains(getCQCode_at(qq));
    }

//    /**
//     * 判断是否存在at当前code。
//     * 如果其中的thisCode为null，则永远返回false
//     *
//     * @return 是否at了某个qq
//     */
//    public boolean isAt(MsgGet msg) {
//        if (msg == null || msg.getThisCode() == null) {
//            return false;
//        }
//        //如果存在at的CQ码并且参数‘qq’是某个qq
//        final String at = getCQCode_at(msg.getThisCode());
//        return msg.getMsg().contains(at);
//    }

    /**
     * 判断是否存在at某个qq
     *
     * @return 是否at了某个qq
     */
    public boolean isAt(String msg, long qq) {
        return isAt(msg, String.valueOf(qq));
    }

    /**
     * 判断某个字符串中是否存在某类型的CQ码
     *
     * @param types CQ码类型
     * @param text  字符串
     * @return 是否包含
     */
    public boolean isContains(CQCodeTypes types, String text) {
        return types.contains(text);
    }

    /**
     * 将CQ码字符串转化为CQCode类型, 会去除空格
     *
     * @param cq cq字符串
     * @return CQCode对象
     * @throws com.forte.qqrobot.exception.CQParseException 当字符串无法转化为cq码对象的时候将会抛出此异常
     */
    public CQCode toCQCode(String cq) {
        return CQCode.of(cq.trim());
    }


    /**
     * 根据消息内容对普通消息与CQ码相关消息进行分隔。
     * 消息需要是转义前的消息，否则会出现问题。
     *
     * @param msg 消息正文
     * @return 切割结果
     */
    public AppendList splitToList(String msg) {
        // 是否处于CQ码读取状态，当读取到'['字符的时候开启。
        AtomicBoolean onCQ = new AtomicBoolean(false);

        StringBuilder sb = new StringBuilder();

        // 构建一个无拼接间隙的CQAppendList
        AppendList list = new CQAppendList("");

        // 索引记录
        AtomicInteger index = new AtomicInteger(0);

        // 遍历字符
        msg.chars().forEach(c -> {
            // 判断是不是CQ的开头
            if (c == '[') {
                // 如果在CQ码读取期间又碰到了一个开头，则抛出异常
                if (onCQ.get()) {
                    throw new CQParseException("redundant", '[', index);
                } else {
                    // 开启CQ码读取并输出之前的读取
                    onCQ.set(true);
                    if (sb.length() > 0) {
                        // 如果原本有内容，输出并清空
                        String lastMsg = sb.toString();
                        sb.delete(0, sb.length());
                        list.append(lastMsg);
                    }
                    sb.append((char) c);
                }
            } else if (c == ']') {
                // 如果是结尾, 但是之前并没有处于CQ读取状态，则抛出异常
                if (!onCQ.get()) {
                    throw new CQParseException("redundant", ']', index);
                } else {
                    // 结束CQ码的读取并输出之前的读取
                    onCQ.set(false);
                    // 记录当前
                    sb.append((char) c);
                    if (sb.length() > 0) {
                        // 如果原本有内容，输出并清空
                        String lastMsg = sb.toString();
                        sb.delete(0, sb.length());
                        list.append(lastMsg);
                    }
                }
            } else {
                // 其他情况就当成是普通的字符，直接读取
                sb.append((char) c);
            }

            // 索引 + 1
            index.addAndGet(1);
        });

        // 结束后收尾
        if (sb.length() > 0) {
            list.append(sb.toString());
        }

        return list;
    }


}
