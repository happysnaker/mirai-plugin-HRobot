package com.happysnaker.handler.impl;

import com.happysnaker.api.QingYunKeApi;
import com.happysnaker.config.RobotConfig;
import com.happysnaker.exception.FileUploadException;
import com.happysnaker.exception.InsufficientPermissionsException;
import com.happysnaker.handler.handler;
import net.mamoe.mirai.contact.ContactList;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.MessageSource;
import net.mamoe.mirai.message.data.PlainText;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Happysnaker
 * @description
 * @date 2022/1/14
 * @email happysnaker@foxmail.com
 */
@handler(priority = 0)
public class GroupMessageHandler extends AbstractMessageHandler {

    /**
     * 获取群号
     * @param event
     * @return
     */
    protected String getGroupId(MessageEvent event) {
        GroupMessageEvent group = (GroupMessageEvent) event;
        return String.valueOf(group.getGroup().getId());
    }

    /**
     * 是否是群聊事件
     * @param event
     * @return
     */
    protected boolean isGroupMessageEvent(MessageEvent event) {
        return event != null && event instanceof GroupMessageEvent;
    }


    /**
     * 如果是一条 atall 消息，则去除 atall 信息
     * @see #getPlantContent
     * @param content 源消息(MIRAI编码)
     * @return 如果被 at all 所标记 将返回去除 at all 信息后的消息，否则什么也不做
     */
    protected String handlerContentIfAtAll(String content) {
        String ans;
        return (ans = handlerContentIfMatches(content, atAllRegex)) == null ? content : ans;
    }

    /**
     * 如果机器人被 at，则去除该 at 信息，该函数不会响应 at all
     * @see #getPlantContent
     * @param content (MIRAI编码)
     * @return 如果被 at 将返回去除 at 信息后的消息，否则什么也不做
     */
    protected String handlerContentIfBotBeAt(String content) {
        if (qqs == null || qqs.isEmpty()) initBotQQ();
        for (String qq : qqs) {
            if (content.indexOf(at.replace("qq", qq)) != -1) {
                return handlerContentIfMatches(content, atRegex);
            }
        }
        return content;
    }


    /**
     * 机器人是否被 at
     * @param content (MIRAI编码)
     * @return
     */
    protected boolean isAtBot(String content) {
        if (qqs == null || qqs.isEmpty()) initBotQQ();
        for (String qq : qqs) {
            if (content.indexOf(at.replace("qq", qq)) != -1) {
                return true;
            }
        }
        return false;
    }


    /**
     * 机器人是否被 at
     * @return
     */
    protected boolean isAtBot(MessageEvent event) {
        return isAtBot(getContent(event));
    }

    /**
     * 将消息转换为群聊消息
     * @param event
     * @return 若转换失败，则返回 null
     */
    protected GroupMessageEvent getGroupMessageEvent(MessageEvent event) {
        try {
            return (GroupMessageEvent) event;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取群成员列表，不包含机器人自己
     * @param event
     * @return 若获取失败则返回 null
     */
    protected ContactList<NormalMember> getMembers(MessageEvent event) {
        GroupMessageEvent group= getGroupMessageEvent(event);
        if (group != null) {
            return group.getGroup().getMembers();
        }
        return null;
    }

    /**
     * 以 List 的形式返回群内所有群成员的 群内名称，如果群内名称为空，则使用用户的QQ昵称替代
     * @param event
     * @return
     */
    protected List<String> getMembersGroupName(MessageEvent event) {
        ContactList<NormalMember> members = null;
        List<String> ans = new ArrayList<>();
        if ((members = getMembers(event)) != null) {
            for (NormalMember member : members) {
                String s = member.getNameCard();
                if (s == null || s.isEmpty()) {
                    s = member.queryProfile().getNickname();
                }
                ans.add(s);
            }
        }
        return ans;
    }

    /**
     * 获取所有群成员的 QQ，不包含机器人自己
     * @param event
     * @return
     */
    protected List<Long> getMembersIds(MessageEvent event) {
        ContactList<NormalMember> members = null;
        List<Long> ans = new ArrayList<>();
        if ((members = getMembers(event)) != null) {
            for (NormalMember member : members) {
                member.getNameCard();
                ans.add(member.getId());
            }
        }
        return ans;
    }

    /**
     * 获取群成员的 qq 号
     * @param event
     * @param groupName 群成员的群名片，如果为空，则为群成员的昵称
     * @return 未搜索到返回 -1
     */
    protected Long getMemberId(MessageEvent event, String groupName) {
        ContactList<NormalMember> members;
        long ans = -1;
        if ((members = getMembers(event)) != null) {
            for (NormalMember member : members) {
                String s = member.getNameCard();
                if (s == null || s.isEmpty())
                    s = member.queryProfile().getNickname();
                if (s.equals(groupName))
                    return member.getId();
            }
        }
        return ans;
    }


    /**
     * 该消息是否为 atall 消息
     *
     * @param content 源消息(MIRAI编码)
     * @return
     */
    protected boolean isAtAll(String content) {
        return content == null ? false : content.indexOf(atAllRegex) != -1;
    }

    /**
     * 去除 mirai 编码消息内的 at、at all 信息
     * @see #getPlantContent
     * @param content
     * @return 返回处理后的消息，如果为 null，则不会处理该消息
     */
    protected String removeAtInfo(String content) {
        content = handlerContentIfBotBeAt(content);
        content = handlerContentIfAtAll(content);
        return content;
    }

    /**
     * 将要回复的消息，默认使用青云客 API 消息回复，允许子类进行扩展，如果此消息返回 null，则不会尝试回复该消息
     *
     * @param event 经过 proxyContent 处理后的消息
     * @return 允许发送多条消息，因此需要返回一个消息列表
     */
    @Override
    protected List<MessageChain> getReplyMessage(MessageEvent event) {
        String content = getPlantContent(event);
        String help1 = "帮助", help2 = "help";
        if (help1.equals(content) || help2.equals(content)) {
            try {
                return List.of(new MessageChainBuilder()
                        .append(new PlainText(RobotConfig.menu))
                        .append(uploadImage(event, new URL("https://tse4-mm.cn.bing.net/th/id/OIP-C.rHuc8SKa0wLVwCqqA27uIwHaEt?pid=ImgDet&rs=1")))
                        .build());
            } catch (FileUploadException | MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return List.of(replaceFaceFromContent(QingYunKeApi.getMessage(content)));
    }

    /**
     * 获取纯文本消息，不包含任何 mirai 编码内容，不包含任何 at 消息
     * @param event
     * @return 返回处理后的消息，消息将去除首尾空格
     *  @see AbstractMessageHandler#getPlantContent
     */
    @Override
    protected String getPlantContent(MessageEvent event) {
        return removeAtInfo(super.getPlantContent(event)).trim();
    }

    /**
     * 获取发送者权限
     * @param event
     * @return 0 是成员、1 是管理员，2 是群主
     */
    protected int getSenderPermission (MessageEvent event) {
        MemberPermission permission = getGroupMessageEvent(event).getPermission();
        return permission.getLevel();
    }

    /**
     * 只有被 at 后并且是群聊消息事件才回复该消息，无视 at all
     * @param event
     * @return
     */
    @Override
    public boolean shouldHandle(MessageEvent event) {
        return isGroupMessageEvent(event) && isAtBot(event);
    }

    /**
     * 撤回一条消息，如果撤回失败请考虑是否是权限不够
     * @param source
     * @return 成功返回 true，否则返回 false
     */
    protected boolean cancelMessage(MessageSource source) {
        try {
            MessageSource.recall(source);
            return true;
        } catch (Exception e) {
            e.getMessage();
            return false;
        }
    }


    /**
     * 撤回一条消息，如果撤回失败请考虑是否是权限不够
     * @param event
     * @return 成功返回 true，否则返回 false
     * @exception InsufficientPermissionsException 如果权限不够则抛出此异常
     */
    protected boolean cancelMessage(MessageEvent event) throws InsufficientPermissionsException {
        if (((GroupMessageEvent) event).getPermission().getLevel() > 0) {
            throw new InsufficientPermissionsException("权限不足");
        }
        return cancelMessage(event.getSource());
    }

    /**
     * 检查此事件是否是群事件，并检查事件消息是否以关键词开头
     * @param event
     * @param keywords
     * @return 如果都为真返回 true
     */
    @Override
    protected boolean startWithKeywords(MessageEvent event, Collection<String> keywords) {
        return isGroupMessageEvent(event) && super.startWithKeywords(event, keywords);
    }
}
