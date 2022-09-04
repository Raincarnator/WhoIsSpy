package net.reincarnatey

import kotlinx.coroutines.launch
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.At

object Listener {
    fun start(plugin: KotlinPlugin){
        plugin.globalEventChannel().subscribeGroupMessages {
            matching(Util.buildRegex(Config.createTriggers)) {
                if (Data.words.isEmpty()){
                    group.sendMessage("无法创建游戏，请先添加词条！")
                } else {
                    plugin.launch { Game(group).create() }
                }
            }

            matching(Util.buildRegex(Config.ruleTriggers)) reply {
                At(sender) + " " + Config.rule
            }
        }
    }
}