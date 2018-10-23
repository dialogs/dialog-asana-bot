package com.terorie.asanabot

import im.dlg.botsdk.Bot
import im.dlg.botsdk.domain.InteractiveEvent
import im.dlg.botsdk.domain.Message
import im.dlg.botsdk.domain.Peer
import im.dlg.botsdk.domain.User
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch

import java.util.HashMap

fun main() {
    Controller.run()
}

object Controller {
    private val convos = HashMap<String, Conversation>()
    val bot = Bot.start(BOT_TOKEN).get()

    fun run() {
        bot.messaging().onMessage { message ->
            // Spawn coroutine to handle message
            GlobalScope.launch {
                handleMessage(message)
            }
        }

        bot.interactiveApi().onEvent { event ->
            GlobalScope.launch {
                handleEvent(event)
            }
        }

        bot.await()
    }

    private suspend fun getUser(peer: Peer): User? {
        // Get user
        val userOpt = bot.users().get(peer).await()
        if (!userOpt.isPresent)
            return null
        return userOpt.get()
    }

    suspend fun handleMessage(message: Message) {
        val user = getUser(message.sender) ?: return

        // Load conversation context
        val conv = convos.getOrPut(user.nick)
            { Conversation(message.peer) }

        // Respond to message
        conv.message(bot, message)
    }

    suspend fun handleEvent(event: InteractiveEvent) {
        val user = getUser(event.peer) ?: return

        // Load conversation context
        val conv = convos.getOrPut(user.nick) { Conversation(event.peer) }

        // Respond to message
        conv.event(bot, event)
    }

}
