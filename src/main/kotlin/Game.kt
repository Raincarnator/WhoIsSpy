package net.reincarnatey

import kotlinx.coroutines.*
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.message.data.content
import net.reincarnatey.Util.buildChooseRegex
import net.reincarnatey.Util.buildDescriptionList
import net.reincarnatey.Util.buildRegex
import net.reincarnatey.Util.buildVoteList

class Game(private val group: Group) : CompletableJob by SupervisorJob() {
    private var start = false
    private var mode = 1
    //1:标准 2:多卧底 3:启用白板 4:特殊
    private val jobNum = arrayOf(0, 0, 0)
    //普通、卧底、白板

    private val players = mutableListOf<Member>()
    private val playerDead = mutableMapOf<Long, Boolean>()
    private val playerJob = mutableMapOf<Long, Int>()
    //身份 1:普通 2:卧底 3:白板
    private var normalWord = ""
    private var spyWord = ""

    private lateinit var jobMessage: Message

    suspend fun create() {
        coroutineScope {
            val channel = globalEventChannel()
                .parentJob(this@Game)
                .filterIsInstance<GroupMessageEvent>()
                .filter { it.group.id == group.id }

            channel.subscribeGroupMessages(priority = EventPriority.HIGH) {
                (matching(buildRegex(Config.createTriggers)) and sentFrom(group)) reply {
                    this.intercept()
                    if (!start) Config.alreadyCreateMessage.replace("{num}", players.size.toString()) else Config.inGamingMessage
                }
                (matching(buildRegex(Config.stopTriggers)) and sentFrom(group)) {
                    group.sendMessage(Config.stopMessage)
                    start = false
                    this@Game.cancel()
                }
            }
        }

        group.sendMessage(Config.createMessage)
        joinGame()
    }

    private suspend fun joinGame() {
        group.sendMessage(Config.modeMessage)
        var finish = false
        val joinGameJob = Job(this)
        val scopedChannel = coroutineScope {
            globalEventChannel().parentJob(joinGameJob)
                .filterIsInstance<GroupMessageEvent>()
                .filter { it.group.id == group.id }
        }
        val job = scopedChannel.subscribeGroupMessages {
            (matching(buildRegex(Config.joinTriggers)) and sentFrom(group)) reply {
                if (sender.id in players.map { it.id }){
                    Config.alreadyJoinMessage.replace("{num}", players.size.toString())
                } else if (start) {
                    Config.inGamingMessage
                } else {
                    players.add(sender)
                    playerJob[sender.id] = 1
                    playerDead[sender.id] = false
                    Config.joinMessage.replace("{num}", players.size.toString())
                }
            }
            (matching(buildRegex(Config.showTriggers)) and sentFrom(group)) reply {
                Config.showMessage.replace("{player_list}", players.map { it.nick }.toString())
            }
            (matching(buildChooseRegex(Config.modeTriggers)) and sentFrom(group)) reply {
                when(message.content.split(' ')[1]){
                    "1" -> {
                        mode = 1
                        Config.switchModeMessage.replace("{mode}", "标准模式")
                    }
                    "2" -> {
                        mode = 2
                        Config.switchModeMessage.replace("{mode}", "多卧底模式")
                    }
                    "3" -> {
                        mode = 3
                        Config.switchModeMessage.replace("{mode}", "随机白板模式")
                    }
                    "114514" -> {
                        mode = 4
                        Config.specialModeMessage
                    }
                    else -> {
                        Config.noModeMessage
                    }
                }
            }
            (matching(buildRegex(Config.startTriggers)) and sentFrom(group)){
                if (players.size > 3){
                    finish = true
                    joinGameJob.cancel()
                }
            }
        }
        job.join()
        if (finish) init()
    }

    private suspend fun init(){
        start = true

        Data.words.run { get(indices.random()) }.let {
            if ((0..1).random() == 0){
                normalWord = it.first
                spyWord = it.second
            } else {
                normalWord = it.second
                spyWord = it.first
            }
        }

        if ((0..99).random() < Config.specialModeOdds){
            mode = 4
        }

        when(mode){
            1 -> {
                jobNum[1] = 1
                jobNum[0] = players.size-1
            }
            2 -> {
                jobNum[1] = players.size / 4
                jobNum[0] = players.size-jobNum[1]
            }
            3 -> {
                jobNum[1] = 1
                jobNum[2] = (1..players.size/4).random()
                jobNum[0] = players.size-jobNum[2]-1
            }
            else -> {
                jobNum[2] = players.size
            }
        }

        jobMessage = buildMessageChain {
            players.shuffled().forEachIndexed { index, player ->
                if (index < jobNum[0]){
                    add("\n普通玩家 ")
                    add(At(player))
                    playerJob[player.id] = 1
                    player.sendMessage(Config.wordMessage.replace("{word}", normalWord))
                } else if (index < jobNum[0]+jobNum[1]){
                    add("\n卧底 ")
                    add(At(player))
                    playerJob[player.id] = 2
                    player.sendMessage(Config.wordMessage.replace("{word}", spyWord))
                } else {
                    add("\n白板 ")
                    add(At(player))
                    playerJob[player.id] = 3
                    player.sendMessage(Config.whiteMessage)
                }
                delay(Config.delay)
            }
        }
        players.shuffle()

        running()
    }

    private suspend fun running(){
        while (start){
            description()
            vote()
            if (jobNum[1]*2>=jobNum[0]+jobNum[2]){
                group.sendMessage(Config.spyWinMessage)
                end()
                return
            }
            if (jobNum[1]<=0){
                group.sendMessage(Config.spyLostMessage)
                end()
                return
            }
            if (jobNum.sum()<=3){
                if (mode == 4){
                    group.sendMessage(Config.specialModeEndMessage)
                } else {
                    group.sendMessage(Config.normalEndMessage.replace("{win_list}", buildDescriptionList(players, playerDead)))
                }
                start = false
                this@Game.cancel()
                return
            }
        }
    }

    private suspend fun description(){
        val done = mutableMapOf<Long, Boolean>()
        done.putAll(playerDead)
        group.sendMessage(Config.descriptionMessage.replace("{nextTriggers}", Config.nextTriggers[0]).replace("{player_list}", buildDescriptionList(players, playerDead)))
        val descriptionJob = Job(this)
        val scopedChannel = coroutineScope {
            globalEventChannel().parentJob(descriptionJob)
                .filterIsInstance<GroupMessageEvent>()
                .filter { it.group.id == group.id }
        }
        val job = scopedChannel.subscribeGroupMessages {
            (sentFrom(group)) {
                if (message.content.matches(buildRegex(Config.nextTriggers))){
                    done.filter { !it.value }.let {
                        if (it.isNotEmpty()){
                            group.sendMessage(
                                buildMessageChain {
                                    add("还有玩家未进行描述: ")
                                    done.forEach { (player, _) ->
                                        add(At(player))
                                    }
                                }
                            )
                        } else {
                            descriptionJob.cancel()
                        }
                    }
                } else {
                    if ((message.content.contains(normalWord)&&playerJob[sender.id]==1) || (message.content.contains(spyWord)&&playerJob[sender.id]==2)) {
                        group.sendMessage(
                            buildMessageChain {
                                add("描述中禁止包含自己的词语！")
                                add(At(sender))
                                add(" 犯规了，游戏结束！")
                            }
                        )
                        start = false
                        this@Game.cancel()
                    } else {
                        done[sender.id] = true
                    }
                }
            }
        }
        job.join()
    }

    private suspend fun vote(){
        val done = mutableMapOf<Long, Boolean>()
        done.putAll(playerDead)
        val vote = mutableMapOf<Long, Int>()
        done.forEach { (player, dead) ->
            if (!dead) {
                vote[player] = 0
            }
        }
        group.sendMessage(Config.voteMessage.replace("{player_list}", buildDescriptionList(players, playerDead)))
        val voteJob = Job(this)
        val scopedChannel = coroutineScope {
            globalEventChannel().parentJob(voteJob)
                .filterIsInstance<GroupMessageEvent>()
                .filter { it.group.id == group.id }
        }
        val job = scopedChannel.subscribeGroupMessages {
            (matching(buildChooseRegex(Config.voteTriggers)) and sentFrom(group)) {
                val choose = message.content.split(' ')[1].toInt()-1
                if (playerDead[sender.id] == true){
                    group.sendMessage("已经出局的玩家不可以投票！")
                } else if (choose == -1){
                    done[sender.id] = true
                    group.sendMessage(At(sender) + " 弃票！")
                    done.filter { !it.value }.let {
                        if (it.isEmpty()){
                            vote.toList().sortedByDescending { p -> p.second }.let { playerVote ->
                                if (playerVote[0].second == playerVote[1].second){
                                    group.sendMessage("平票！本回合无人出局！")
                                } else {
                                    playerVote[0].let { (player, vote) ->
                                        jobNum[playerJob[player]!!-1]--
                                        playerDead[player] = true
                                        group.sendMessage(
                                            buildMessageChain {
                                                add("本回合出局的是")
                                                add(At(player))
                                                add("，共 $vote 票！")
                                            }
                                        )
                                    }
                                }
                            }
                            voteJob.cancel()
                        }
                    }
                } else if (playerDead[players[choose].id] == true){
                    group.sendMessage("无法给已经出局的玩家投票！")
                } else if (sender !in players) {
                    group.sendMessage("非游戏玩家禁止投票！")
                } else if (done[sender.id] == true) {
                    group.sendMessage("你已经投过票了！")
                } else if (choose !in players.indices){
                    group.sendMessage("该玩家不存在！")
                } else {
                    done[sender.id] = true
                    vote[players[choose].id] = vote[players[choose].id]!! + 1
                    done.filter { !it.value }.let {
                        if (it.isNotEmpty()){
                            group.sendMessage("当前票数:\n${buildVoteList(players, playerDead, vote)}")
                        } else {
                            vote.toList().sortedByDescending { p -> p.second }.let { playerVote ->
                                if (playerVote[0].second == playerVote[1].second){
                                    group.sendMessage("平票！本回合无人出局！")
                                } else {
                                    playerVote[0].let { (player, vote) ->
                                        jobNum[playerJob[player]!!-1]--
                                        playerDead[player] = true
                                        group.sendMessage(
                                            buildMessageChain {
                                                add("本回合出局的是")
                                                add(At(player))
                                                add("，共 $vote 票！")
                                            }
                                        )
                                    }
                                }
                            }
                            voteJob.cancel()
                        }
                    }
                }
            }
        }
        job.join()
    }

    private suspend fun end(){
        group.sendMessage(
            buildMessageChain {
                add("本局游戏普通词条为【${normalWord}】，卧底词为【${spyWord}】！")
                add(jobMessage)
            }
        )
        start = false
        this@Game.cancel()
    }
}