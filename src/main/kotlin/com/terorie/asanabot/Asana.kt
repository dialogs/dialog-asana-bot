package com.terorie.asanabot

import com.asana.Client
import com.asana.models.Project
import com.asana.models.User
import com.asana.requests.CollectionRequest
import java.lang.IllegalArgumentException

object Asana {

    val client: Client
    val members: List<User>
    val projects: List<Project>

    init {
        client = Client.accessToken(Config.ASANA_PERSONAL_TOKEN)
        projects = client.projects.findByTeam(Config.ASANA_TEAM_ID).execute()
            ?: throw IllegalArgumentException("no such team: ${Config.ASANA_TEAM_ID}")
        val path = "/teams/${Config.ASANA_TEAM_ID}/users"
        members = CollectionRequest(client.teams, User::class.java, path, "GET").execute()
    }

    fun createTask(projectID: String, userID: String, title: String, desc: String): String {
        val project = client.projects.findById(projectID).execute()
            ?: throw IllegalArgumentException("no such project: $projectID")
        val task = client.tasks.createInWorkspace(project.workspace.id)
            .data("name", title)
            .data("notes", desc)
            .data("projects", listOf(projectID))
            .data("assignee", userID)
            .execute()
        return "https://app.asana.com/0/${project.id}/${task.id}"
    }

}
