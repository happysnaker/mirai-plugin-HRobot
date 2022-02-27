package com.happysnaker.command.impl;

import com.happysnaker.config.RobotConfig;
import com.happysnaker.exception.CanNotParseCommandException;
import com.happysnaker.exception.InsufficientPermissionsException;
import com.happysnaker.handler.handler;
import com.happysnaker.permission.Permission;
import com.happysnaker.utils.StringUtil;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.MessageSource;
import net.mamoe.mirai.message.data.QuoteReply;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Happysnaker
 * @description
 * @date 2022/2/22
 * @email happysnaker@foxmail.com
 */
@handler(priority = 1024)
public class AuthorityCommandMessageHandler extends DefaultCommandMessageHandlerManager {
    public static final String GRANT_ADMIN = "授予管理员权限";
    public static final String GRANT_GT_ADMIN = "授予坎公管理员权限";
    public static final String GRANT_GROUP_ADMIN = "授予群管理员权限";
    public static final String SEE_PERMISSION = "查看权限";
    public static final String REMOVE_ADMIN = "移除管理员权限";
    public static final String REMOVE_GT_ADMIN = "移除坎公管理员权限";
    public static final String REMOVE_GROUP_ADMIN = "移除群管理员权限";

    public static final Map<Integer, String> permissionMap = new HashMap<>();

    public AuthorityCommandMessageHandler() {
        keywords.add(GRANT_ADMIN);
        keywords.add(GRANT_GT_ADMIN);
        keywords.add(SEE_PERMISSION);
        keywords.add(REMOVE_GT_ADMIN);
        keywords.add(REMOVE_ADMIN);
        keywords.add(REMOVE_GROUP_ADMIN);
        keywords.add(GRANT_GROUP_ADMIN);

        permissionMap.put(Permission.SUPER_ADMINISTRATOR, "超级管理员");
        permissionMap.put(Permission.ADMINISTRATOR, "普通管理员");
        permissionMap.put(Permission.GT_ADMINISTRATOR, "坎公管理员");
        permissionMap.put(Permission.GROUP_ADMINISTRATOR, "群管理员");
    }

    @Override
    public List<MessageChain> parseCommand(MessageEvent event) throws CanNotParseCommandException, InsufficientPermissionsException {
        String content = getPlantContent(event);
        if (content.startsWith(GRANT_ADMIN)) {
            return grantAdmin(event);
        } else if (content.startsWith(GRANT_GT_ADMIN)) {
            return grantGtAdmin(event);
        } else if (content.startsWith(REMOVE_ADMIN)) {
            return removeAdmin(event);
        } else if (content.startsWith(REMOVE_GT_ADMIN)) {
            return removeGtAdmin(event);
        } else if (content.startsWith(SEE_PERMISSION)) {
            return seePermission(event);
        } else if (content.startsWith(GRANT_GROUP_ADMIN)) {
            return grantGroupAdmin(event);
        } else if (content.startsWith(REMOVE_GROUP_ADMIN)) {
            return removeGroupAdmin(event);
        }
        return null;
    }


    private List<MessageChain> seePermission(MessageEvent event) throws CanNotParseCommandException, InsufficientPermissionsException {
        String content = getPlantContent(event);
        String qq = content.replace(SEE_PERMISSION, "").trim();
        if (qq == null || qq.isEmpty()) {
            qq = getSenderId(event);
        } else if (!StringUtil.isNum(qq)) {
            throw new CanNotParseCommandException("不正确的 qq 格式");
        }
        StringBuilder sb = new StringBuilder();
        for (Integer p : Permission.getPermissionSet(qq)) {
            sb.append("- " + permissionMap.get(p) + "\n");
        }
        return sb.length() == 0 ?
                buildMessageChainAsList("用户 " + qq + " 无任何权限") :
                buildMessageChainAsList("用户 " + qq + " 的权限为:\n" + sb);
    }

    private List<MessageChain> removeGroupAdmin(MessageEvent event) throws CanNotParseCommandException, InsufficientPermissionsException {
        String content = getPlantContent(event);
        String qq = content.replace(REMOVE_GROUP_ADMIN, "").trim();
        assertPermission(getSenderId(event), qq, event);
        if (!RobotConfig.groupAdministrator.contains(qq)) {
            return buildMessageChainAsList("qq: " + qq + " 不是群管理员，此操作无任何动作");
        }
        if (RobotConfig.groupAdministrator.remove(qq)) {
            return buildMessageChainAsList("移除成功");
        }
        throw new CanNotParseCommandException("未知错误");
    }

    private List<MessageChain> removeGtAdmin(MessageEvent event) throws CanNotParseCommandException, InsufficientPermissionsException {
        String content = getPlantContent(event);
        String qq = content.replace(REMOVE_GT_ADMIN, "").trim();
        assertPermission(getSenderId(event), qq, event);
        if (!RobotConfig.gtAdministrator.contains(qq)) {
            return buildMessageChainAsList("qq: " + qq + " 不是坎公管理员，此操作无任何动作");
        }
        if (RobotConfig.gtAdministrator.remove(qq)) {
            return buildMessageChainAsList("移除成功");
        }
        throw new CanNotParseCommandException("未知错误");
    }

    private List<MessageChain> removeAdmin(MessageEvent event) throws CanNotParseCommandException, InsufficientPermissionsException {
        String content = getPlantContent(event);
        String qq = content.replace(REMOVE_ADMIN, "").trim();
        assertPermission(getSenderId(event), qq, event);
        if (!RobotConfig.administrator.contains(qq)) {
            return buildMessageChainAsList("qq: " + qq + " 不是管理员，此操作无任何动作");
        }
        if (RobotConfig.administrator.remove(qq)) {
            return buildMessageChainAsList("移除成功");
        }
        throw new CanNotParseCommandException("未知错误");
    }

    private List<MessageChain> grantGtAdmin(MessageEvent event) throws CanNotParseCommandException, InsufficientPermissionsException {
        String content = getPlantContent(event);
        String qq = content.replace(GRANT_GT_ADMIN, "").trim();
        assertPermission(getSenderId(event), qq, event);
        if (RobotConfig.gtAdministrator.contains(qq)) {
            return buildMessageChainAsList(qq + " 已是坎公管理员");
        }
        if (RobotConfig.gtAdministrator.add(qq)) {
            return buildMessageChainAsList("配置成功");
        }
        throw new CanNotParseCommandException("未知错误");
    }

    private List<MessageChain> grantGroupAdmin(MessageEvent event) throws CanNotParseCommandException, InsufficientPermissionsException {
        String content = getPlantContent(event);
        String qq = content.replace(GRANT_GROUP_ADMIN, "").trim();
        assertPermission(getSenderId(event), qq, event);
        if (RobotConfig.groupAdministrator.contains(qq)) {
            return buildMessageChainAsList(qq + " 已是坎公管理员");
        }
        if (RobotConfig.groupAdministrator.add(qq)) {
            return buildMessageChainAsList("配置成功");
        }
        throw new CanNotParseCommandException("未知错误");
    }

    private List<MessageChain> grantAdmin(MessageEvent event) throws CanNotParseCommandException, InsufficientPermissionsException {
        String content = getPlantContent(event);
        String qq = content.replace(GRANT_ADMIN, "").trim();
        assertPermission(getSenderId(event), qq, event);
        if (RobotConfig.administrator.contains(qq)) {
            return buildMessageChainAsList(qq + " 已是管理员");
        }
        if (RobotConfig.administrator.add(qq)) {
            return buildMessageChainAsList("配置成功");
        }
        throw new CanNotParseCommandException("未知错误");
    }

    private void assertPermission(String senderQQ, String qq, MessageEvent event) throws CanNotParseCommandException, InsufficientPermissionsException {
        if (!StringUtil.isNum(qq)) {
            throw new CanNotParseCommandException("不正确的 qq 格式");
        }
        if (Permission.compare(senderQQ, qq) <= 0) {
            throw new InsufficientPermissionsException();
        }

        // 如果是群管理员的话，设置的对象必须得在群内
        if (Permission.hasGroupAdmin(senderQQ) && !Permission.hasAdmin(senderQQ)) {
            if (!getMembersIds(event).contains(Long.valueOf(qq))) {
                throw new InsufficientPermissionsException("群管理员无法设置不在群内的对象");
            }
        }
    }
}
