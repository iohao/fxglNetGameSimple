/*
 * ioGame
 * Copyright (C) 2021 - 2023  渔民小镇 （262610965@qq.com、luoyizhu@gmail.com） . All Rights Reserved.
 * # iohao.com . 渔民小镇
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.iohao.demo.game.server;

import com.iohao.game.action.skeleton.core.BarSkeleton;
import com.iohao.game.action.skeleton.core.BarSkeletonBuilder;
import com.iohao.game.action.skeleton.core.BarSkeletonBuilderParamConfig;
import com.iohao.game.action.skeleton.core.flow.interal.DebugInOut;
import com.iohao.game.action.skeleton.core.flow.interal.StatActionInOut;
//import com.iohao.game.action.skeleton.core.flow.interal.ThreadMonitorInOut;
import com.iohao.game.bolt.broker.client.AbstractBrokerClientStartup;
import com.iohao.game.bolt.broker.core.client.BrokerClient;
import com.iohao.game.bolt.broker.core.client.BrokerClientBuilder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author 渔民小镇
 * @date 2023-11-23
 */
@Slf4j
public class MyLogicServer extends AbstractBrokerClientStartup {
    @Override
    public BarSkeleton createBarSkeleton() {
        // 业务框架构建器 配置
        var config = new BarSkeletonBuilderParamConfig()
                // 扫描 action 类所在包
                .scanActionPackage(GameAction.class);

        // 业务框架构建器
        var builder = config.createBuilder();

        inOut(builder);

        return builder.build();
    }

    private static void inOut(BarSkeletonBuilder builder) {
        // 添加控制台输出插件
//        builder.addInOut(new DebugInOut());

        StatActionInOut statActionInOut = new StatActionInOut();
        builder.addInOut(statActionInOut);
        GameAction.statActionRegion = statActionInOut.getRegion();

//        ThreadMonitorInOut threadMonitorInOut = new ThreadMonitorInOut();
//        builder.addInOut(threadMonitorInOut);
//        GameAction.threadMonitorRegion = threadMonitorInOut.getRegion();
    }

    @Override
    public BrokerClientBuilder createBrokerClientBuilder() {
        BrokerClientBuilder builder = BrokerClient.newBuilder();
        builder.appName("ioGame 网络游戏逻辑服");
        return builder;
    }
}