package net.reincarnatey

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value

object Data : AutoSavePluginData("SpyData") {
    var words by value(mutableListOf(Pair("词1", "词2")))
}