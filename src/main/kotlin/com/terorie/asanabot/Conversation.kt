package com.terorie.asanabot

import im.dlg.botsdk.Bot
import im.dlg.botsdk.domain.InteractiveEvent
import im.dlg.botsdk.domain.Message
import im.dlg.botsdk.domain.Peer
import im.dlg.botsdk.domain.interactive.*
import kotlinx.coroutines.future.await
import java.lang.Exception

class Conversation(val peer: Peer) {

    private var state = 0
    lateinit var taskName: String
    lateinit var taskDescription: String
    lateinit var currentAction: String
    lateinit var projectID: String
    lateinit var assigneeID: String

    init { init() }

    private fun init() {
        taskName = ""
        taskDescription = ""
        currentAction = ""
        projectID = ""
        assigneeID = ""
    }

    suspend fun message(bot: Bot, message: Message) {
        when (state) {
            // Initial encounter
            0 -> {
                state = 1
                bot.messaging().send(peer,
                    "Hi! Write me the task, and I will create it in Asana."
                ).await()
            }
            // Ask for board
            1 -> {
                state = 2
                taskName = message.text
                currentAction = "action_select_project"

                val selectOptions = Asana.projects.map { project ->
                    InteractiveSelectOption(project.id, project.name)
                }

                val select = InteractiveSelect(selectOptions)
                val action = InteractiveAction(currentAction, select)
                val group = InteractiveGroup(listOf(action))
                bot.messaging().send(peer,
                    "Great! In which project should I add the task?"
                ).await()
                bot.interactiveApi().send(peer, group).await()
            }
            // Description sent, submit task to Asana
            4 -> {
                state = 0
                taskDescription = message.text
                submit(bot)
                init()
            }
        }
    }

    suspend fun event(bot: Bot, event: InteractiveEvent) {
        if (event.id != currentAction)
            return
        when (state) {
            // Load board, ask for assignee
            2 -> {
                state = 3
                projectID = event.value
                currentAction = "action_select_assignee"

                val selectOptions = Asana.members.map { user ->
                    InteractiveSelectOption(user.id, user.name)
                }

                val select = InteractiveSelect(selectOptions)
                val action = InteractiveAction(currentAction, select)
                val group = InteractiveGroup(listOf(action))
                bot.messaging().send(peer,
                    "Ok. To whom should I assign the task?"
                ).await()
                bot.interactiveApi().send(peer, group).await()
            }
            // Load assignee, ask for description and create button
            3 -> {
                state = 4
                assigneeID = event.value

                val button = InteractiveButton("Create", "Create")
                val action = InteractiveAction(currentAction, button)
                val group = InteractiveGroup(listOf(action))

                bot.messaging().send(peer,
                    "Got it! Do you whant to add a description to the task or should I immediately publish the task? Write a comment with the text or confirm the request."
                ).await()
                bot.interactiveApi().send(peer, group)
            }
            // Button clicked, submit task to Asana
            4 -> {
                state = 0
                submit(bot)
                init()
            }
        }
    }

    suspend fun submit(bot: Bot) {
        try {
            val url = Asana.createTask(projectID, assigneeID, taskName, taskDescription)
            bot.messaging().send(peer,
                "Great, the task is created. Link to the task: $url"
            ).await()
        } catch (e: Exception) {
            bot.messaging().send(peer,
                "Failed creating the task :("
            ).await()
            e.printStackTrace()
        }
    }

}
