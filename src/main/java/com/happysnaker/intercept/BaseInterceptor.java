package com.happysnaker.intercept;

import com.happysnaker.config.RobotConfig;
import com.happysnaker.utils.RobotUtil;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChain;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 前置拦截器，控制 Robot 的开关，检查 include 和 exclude 的事件
 * @author Happysnaker
 * @description
 * @date 2022/1/30
 * @email happysnaker@foxmail.com
 */
@intercept
public class BaseInterceptor implements Interceptor {


    /**
     * 返回真则拦截该事件
     * @param event
     * @return
     */
    @Override
    public boolean interceptBefore(MessageEvent event) {
        // 如果如何，放行开机命令
        if (RobotUtil.getContent(event).equals(RobotConfig.commandPrefix + "开机")) {
            return false;
        }
        System.out.println("RobotConfig.enableRobot = " + RobotConfig.enableRobot);
        if (!RobotConfig.enableRobot)               return true;
        if (!(event instanceof GroupMessageEvent))  return false;
        String gid = String.valueOf(((GroupMessageEvent) event).getGroup().getId());
        if (RobotConfig.include.isEmpty()) {
            return RobotConfig.exclude.contains(gid);
        }
        return !RobotConfig.include.contains(gid);
    }

    @Override
    public List<MessageChain> interceptAfter(MessageEvent e, List<MessageChain> mc) {
        return mc;
    }
}
