package de.simles.timetracker.storage

import de.simles.timetracker.models.Work
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
object FileBasedWorkStorage : FileBasedStorage(), WorkStorageHandler {
    private val mutex = Mutex()

    private fun getYearFile(year: Int): File {
        return File(basePath, "$year")
    }

    private fun getWeekFile(year: Int, week: Int, forBackup: Boolean = false): File {
        if (forBackup && backupPath != null) {
            val now = Clock.System.now().epochSeconds
            return Path(basePath, backupPath, "$year", "$week-$now.json").toFile()
        }
        return Path(basePath, "$year", "$week.json").toFile()
    }

    private fun getNumberFromName(name: String, prefix: String, suffix: String): Int =
        """$prefix(\d+)$suffix""".toRegex().matchEntire(name)!!.groups[1]!!.value.toInt()

    override suspend fun getYears(): List<Int> {
        return File(basePath).listFiles()?.filter {
            it.isDirectory and it.name.matches("""(\d+)""".toRegex())
        }?.map { getNumberFromName(it.name, "", "") } ?: emptyList()
    }

    override suspend fun getWeeks(year: Int): List<Int> {
        return getYearFile(year).listFiles()?.filter {
            it.isFile and it.name.matches("""(\d+)\.json""".toRegex())
        }?.map { getNumberFromName(it.name, "", "\\.json") } ?: emptyList()
    }

    override suspend fun getWork(year: Int, week: Int): Result<List<Work>> {
        return kotlin.runCatching {
            mutex.withLock {
                val file = getWeekFile(year, week)
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

    override suspend fun setWork(year: Int, week: Int, work: List<Work>) {
        mutex.withLock {
            val weekFile = getWeekFile(year, week)
            if (backupPath != null && weekFile.exists()) {
                weekFile.copyTo(getWeekFile(year, week, forBackup = true))
            }
            if (!weekFile.parentFile.exists()) {
                weekFile.parentFile.mkdirs()
            }
            Json.encodeToStream(work, weekFile.outputStream())
        }
    }
}