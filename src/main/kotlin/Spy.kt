package net.reincarnatey

import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.utils.info

object Spy : KotlinPlugin(
    JvmPluginDescription(
        id = "net.reincarnatey.spy",
        name = "谁是卧底",
        version = "1.0",
    ) {
        author("Mitr-yuzr")
        info("""谁是卧底小游戏插件""")
    }
) {
    override fun onEnable() {
        Config.reload()
        Data.reload()

        Listener.start(this)

        logger.info {
            if (Data.words.isEmpty()){
                "spy词条不足，请前往/data/net.reincarnatey.spy/SpyData.yml增加词条！"
            } else {
                "spy准备就绪！"
            }
        }
    }

    override fun onDisable() {
        logger.info { "TimerRequester已卸载!" }
    }
}