package de.simles.timetracker.storage

import de.simles.timetracker.models.Project
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.File
import kotlin.io.path.Path

@ExperimentalSerializationApi
object FileBasedProjectStorage : FileBasedStorage(), ProjectStorageHandler {
    private val file = File(basePath, "projects.json")
    private val mutex: Mutex = Mutex()

    override suspend fun getProjects(): Result<List<Project>> {
        mutex.withLock {
            return kotlin.runCatching {
                if (file.exists()) {
                    Json.decodeFromStream(file.inputStream())
                } else {
                    emptyList()
                }
            }
        }
    }

    override suspend fun setProjects(projects: List<Project>) {
        mutex.withLock {
            if (backupPath != null && file.exists()) {
                val now = Clock.System.now().epochSeconds
                file.copyTo(Path(basePath, backupPath, "projects-$now.json").toFile())
            }
            Json.encodeToStream(projects, file.outputStream())
        }
    }
}