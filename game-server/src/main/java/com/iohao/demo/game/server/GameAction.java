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

import com.iohao.demo.game.common.MyCmd;
import com.iohao.demo.game.common.PlayerInfo;
import com.iohao.demo.game.common.PlayerMove;
import com.iohao.game.action.skeleton.annotation.ActionController;
import com.iohao.game.action.skeleton.annotation.ActionMethod;
import com.iohao.game.action.skeleton.core.CmdInfo;
import com.iohao.game.action.skeleton.core.exception.ActionErrorEnum;
import com.iohao.game.action.skeleton.core.flow.FlowContext;
import com.iohao.game.action.skeleton.core.flow.interal.StatActionInOut;
//import com.iohao.game.action.skeleton.core.flow.interal.ThreadMonitorInOut;
import com.iohao.game.action.skeleton.protocol.wrapper.WrapperKit;
import com.iohao.game.bolt.broker.client.kit.UserIdSettingKit;
import com.iohao.game.bolt.broker.core.client.BrokerClientHelper;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.jctools.maps.NonBlockingHashMap;
import org.jctools.maps.NonBlockingHashSet;

import java.util.*;

/**
 * @author 渔民小镇
 * @date 2023-11-23
 */
@Slf4j
@ActionController(MyCmd.cmd)
public class GameAction {
    public static StatActionInOut.StatActionRegion statActionRegion;
//    public static ThreadMonitorInOut.ThreadMonitorRegion threadMonitorRegion;
    static Faker faker = new Faker(Locale.CHINA);
    Map<Long, PlayerInfo> playerInfoMap = new NonBlockingHashMap<>();
    Set<Long> playerIdSet = new NonBlockingHashSet<>();

    @ActionMethod(MyCmd.login)
    public PlayerInfo login(long playerId, FlowContext flowContext) {
        UserIdSettingKit.settingUserId(flowContext, playerId);

        PlayerInfo playerInfo = new PlayerInfo();
        playerInfo.id = playerId;
        playerInfo.name = faker.name().fullName();

        return playerInfo;
    }

    @ActionMethod(MyCmd.joinRoom)
    public void joinRoom(PlayerInfo playerInfo, FlowContext flowContext) {
        ActionErrorEnum.dataNotExist.assertNonNull(playerInfo.name);

        long userId = flowContext.getUserId();
        this.playerInfoMap.put(userId, playerInfo);
        this.playerIdSet.add(userId);

        // 新玩家加入房间，给房间内的其他玩家广播
        List<PlayerInfo> list = this.playerInfoMap.values().stream().toList();

        BrokerClientHelper.getBroadcastContext().broadcast(
                CmdInfo.of(MyCmd.cmd, MyCmd.joinRoom),
                WrapperKit.ofListByteValue(list),
                this.playerIdSet
        );
    }

    @ActionMethod(MyCmd.quitRoom)
    public void quitRoom(FlowContext flowContext) {
        long userId = flowContext.getUserId();

        var playerInfo = this.playerInfoMap.remove(userId);
        this.playerIdSet.remove(userId);

        // 新玩家加入房间，给房间内的其他玩家广播
        BrokerClientHelper.getBroadcastContext().broadcast(
                CmdInfo.of(MyCmd.cmd, MyCmd.quitRoom),
                playerInfo,
                this.playerIdSet
        );
    }

    @ActionMethod(MyCmd.move)
    public void move(PlayerMove playerMove, FlowContext flowContext) {
        long userId = flowContext.getUserId();

        PlayerInfo playerInfo = this.playerInfoMap.get(userId);
        playerInfo.x = playerMove.x;
        playerInfo.y = playerMove.y;

        playerMove.playerId = userId;

        CmdInfo cmdInfo = CmdInfo.of(MyCmd.cmd, MyCmd.move);
        BrokerClientHelper.getBroadcastContext().broadcast(cmdInfo, playerMove, this.playerIdSet);
    }

    @ActionMethod(MyCmd.print)
    public void printRegion() {
        statActionRegion.forEach((cmdInfo, statAction) -> log.info("{}", statAction));

//        log.info("\n{}", threadMonitorRegion);
    }


}