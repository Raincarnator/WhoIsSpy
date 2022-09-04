package net.reincarnatey

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object Config : AutoSavePluginConfig("SpyConfig") {
    @ValueDescription("创建触发器")
    var createTriggers by value(mutableListOf("创建谁是卧底"))

    @ValueDescription("加入触发器")
    var joinTriggers by value(mutableListOf("加入", "加入谁是卧底"))

    @ValueDescription("当前玩家触发器")
    var showTriggers by value(mutableListOf("当前玩家", "玩家列表"))

    @ValueDescription("切换模式触发器")
    var modeTriggers by value(mutableListOf("模式", "切换模式"))

    @ValueDescription("开始触发器")
    var startTriggers by value(mutableListOf("开", "开始", "go"))

    @ValueDescription("描述结束触发器")
    var nextTriggers by value(mutableListOf("结束"))

    @ValueDescription("投票触发器")
    var voteTriggers by value(mutableListOf("投票", "vote", "投"))

    @ValueDescription("强制结束触发器")
    var stopTriggers by value(mutableListOf("结束谁是卧底"))

    @ValueDescription("规则触发器")
    var ruleTriggers by value(mutableListOf("谁是卧底规则"))

    @ValueDescription("创建消息")
    var createMessage by value("创建成功~\n请输入\"加入\"加入游戏！\n人数大于三人后可输入\"开始\"开始游戏！")

    @ValueDescription("重复创建消息")
    var alreadyCreateMessage by value("游戏已存在！当前玩家{num}人，可\n请输入\"加入\"加入游戏！输入\"谁是卧底规则\"查看规则，\n人数大于三人后可输入\"开始\"开始游戏！")

    @ValueDescription("加入消息")
    var joinMessage by value("加入成功~\n当前玩家{num}人")

    @ValueDescription("重复加入消息")
    var alreadyJoinMessage by value("你已经加入过了！\n当前玩家{num}人")

    @ValueDescription("游戏进行中消息")
    var inGamingMessage by value("游戏正在进行中！")

    @ValueDescription("当前玩家消息")
    var showMessage by value("当前玩家: {player_list}")

    @ValueDescription("词条消息")
    var wordMessage by value("你的词条: {word}")

    @ValueDescription("白板消息")
    var whiteMessage by value("你的词条——欸？！你没有词条呢，多观察一下他人的发言并模仿吧！")

    @ValueDescription("强制结束消息")
    var stopMessage by value("游戏已结束！")

    @ValueDescription("模式提醒消息")
    var modeMessage by value("默认为标准模式，可通过\"模式 1/2/3\"切换至其他模式\n1.标准模式: 仅有1个卧底\n2.多卧底模式: 适用于人较多的情况，四分之一玩家为卧底\n3.随机白板模式: 将随机一部分玩家为白板(不超过四分之一)")

    @ValueDescription("切换模式消息")
    var switchModeMessage by value("已切换至{mode}！")

    @ValueDescription("模式不存在消息")
    var noModeMessage by value("该模式不存在！")

    @ValueDescription("特殊模式消息")
    var specialModeMessage by value("游戏模式好像变得奇怪了！")

    @ValueDescription("卧底胜利消息")
    var spyWinMessage by value("游戏结束啦！卧底获得胜利！")

    @ValueDescription("卧底失败消息")
    var spyLostMessage by value("游戏结束啦！卧底输了！")

    @ValueDescription("特殊模式结束消息")
    var specialModeEndMessage by value("游戏结束啦！本轮没有玩家获胜，因为本轮为【特殊模式】，全部玩家都是白板！")

    @ValueDescription("不知道谁赢消息")
    var normalEndMessage by value("游戏结束啦！以下玩家获得胜利:\n{win_list}")

    @ValueDescription("描述阶段消息")
    var descriptionMessage by value("描述阶段！请按照以下顺序轮流用一句话描述自己的词语（但不可以包含自己的词语），所有人均描述完毕后发送\"{nextTriggers}\"进入下一阶段。\n{player_list}")

    @ValueDescription("投票消息")
    var voteMessage by value("投票阶段！请输入\"投票 编号\"进行投票，所有人均描述完毕后自动进入下一回合。\n0. 弃票\n{player_list}")

    @ValueDescription("规则")
    var rule by value("标准模式下，每个玩家会拿到自己的词条，其中卧底的词条与其他人的不一样！每个玩家需要轮流描述自己的词，描述不能重复，若出现自己的词则直接判输。之后进行投票选出卧底。票数最多的玩家将会被淘汰。一直重复下去，直到卧底被淘汰或卧底的人数占剩余玩家总数大于等于三分之一。")

    @ValueDescription("强制进入特殊模式(不通知)的概率，可填0-100，对应0%-100%的概率，为0时就是禁用特殊模式 。特殊模式: 所有玩家均为白板，只剩下3名玩家时结束。该模式也可通过\"切换模式 114514\"进入。")
    var specialModeOdds by value(5)
}