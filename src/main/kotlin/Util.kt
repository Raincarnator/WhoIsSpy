package net.reincarnatey

import net.mamoe.mirai.contact.Member

object Util {
    fun buildRegex(list: List<String>): Regex {
        val sb = StringBuilder()
        sb.append('(')
        list.forEachIndexed { index, s ->
            sb.append(s)
            if (index < list.size - 1) {
                sb.append('|')
            }
        }
        sb.append(')')
        return Regex(sb.toString())
    }

    fun buildChooseRegex(list: List<String>): Regex {
        val sb = StringBuilder()
        sb.append("^(")
        list.forEachIndexed { index, s ->
            sb.append(s)
            if (index < list.size - 1) {
                sb.append('|')
            }
        }
        sb.append(") [0-9]+$")
        return Regex(sb.toString())
    }

    fun buildDescriptionList(players: MutableList<Member>, playerDead: MutableMap<Long, Boolean>): String {
        val sb = StringBuilder()
        players.forEachIndexed { index, player ->
            if (playerDead[player.id] != true){
                sb.append("${index+1}. ${player.nick}")
                if (index < players.size-1){
                    sb.append("\n")
                }
            }
        }
        return sb.toString()
    }

    fun buildVoteList(players: MutableList<Member>, playerDead: MutableMap<Long, Boolean>, vote: MutableMap<Long, Int>): String {
        val sb = StringBuilder()
        players.forEachIndexed { index, player ->
            if (playerDead[player.id] != true){
                sb.append("${index+1}. ${player.nick} ${vote[player.id]?:0}票")
                if (index < players.size-1){
                    sb.append("\n")
                }
            }
        }
        return sb.toString()
    }
}