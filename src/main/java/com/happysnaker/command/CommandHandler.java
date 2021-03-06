package com.happysnaker.command;

import com.happysnaker.exception.CanNotParseCommandException;
import com.happysnaker.exception.InsufficientPermissionsException;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChain;

import java.util.List;

/**
 * 机器人命令处理接口
 * @author Happysnaker
 * @description
 * @date 2022/2/23
 * @email happysnaker@foxmail.com
 */
public interface CommandHandler {
    /**
     * 解析命令
     * @param event 命令事件
     * @return 解析完成后返回的消息
     * @throws CanNotParseCommandException 无法解析时请抛出此异常
     * @throws InsufficientPermissionsException 没有足够权限时请抛出此异常
     */
    List<MessageChain> parseCommand(MessageEvent event) throws CanNotParseCommandException, InsufficientPermissionsException;
}
