package com.terorie.asanabot

import java.io.FileInputStream
import java.lang.IllegalArgumentException
import java.util.*

object Config {
    val BOT_TOKEN: String
    val ASANA_PERSONAL_TOKEN: String
    val ASANA_TEAM_ID: String

    init {
        val props = Properties()
        FileInputStream("config.properties").use {
            props.load(it)
        }
        BOT_TOKEN = props.getProperty("bot_token")
            ?: throw IllegalArgumentException("bot_token not set")
        ASANA_PERSONAL_TOKEN = props.getProperty("asana_personal_token")
            ?: throw IllegalArgumentException("asana_personal_token not set")
        ASANA_TEAM_ID = props.getProperty("asana_team_id")
            ?: throw IllegalArgumentException("asana_team_id not set")
    }
}
