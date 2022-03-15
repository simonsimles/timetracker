package de.simles.timetracker.storage

import de.simles.timetracker.models.Project

interface ProjectStorageHandler {
    suspend fun getProjects(): Result<List<Project>>
    suspend fun setProjects(projects: List<Project>)
}