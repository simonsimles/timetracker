package de.simles.timetracker.storage

import de.simles.timetracker.models.Comment
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
object FileBasedCommentStorage : FileBasedStorage(), CommentStorageHandler {
    private val mutex: Mutex = Mutex()

    private fun getMonthFile(year: Int, month: Int, forBackup: Boolean = false): File {
        if (forBackup && backupPath != null) {
            val now = Clock.System.now().epochSeconds
            return Path(basePath, backupPath, "comment", "$year", "$month-$now.json").toFile()
        }
        return Path(basePath, "comment", "$year", "$month.json").toFile()
    }

    override suspend fun getMonth(year: Int, month: Int): Result<List<Comment>> {
        return kotlin.runCatching {
            mutex.withLock {
                val file = getMonthFile(year, month)
                if (file.exists()) {
                    Json.decodeFromStream(
                        file.inputStream()
                    )
                } else {
                    emptyList()
                }
            }
        }
    }

    override suspend fun setMonth(year: Int, month: Int, comments: List<Comment>) {
        mutex.withLock {
            val file = getMonthFile(year, month)
            if (backupPath != null && file.exists()) {
                file.copyTo(getMonthFile(year, month, forBackup = true))
            }
            if (!file.parentFile.exists()) {
                file.parentFile.mkdirs()
            }
            Json.encodeToStream(comments, file.outputStream())
        }
    }
}