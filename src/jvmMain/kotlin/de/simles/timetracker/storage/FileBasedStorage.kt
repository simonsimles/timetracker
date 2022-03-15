package de.simles.timetracker.storage

import java.util.*

open class FileBasedStorage {
    val basePath = retrieveBasePath()
    val backupPath: String? = retrieveBackupPath()

    private fun retrieveBasePath(): String {
        val path = System.getenv("TIME_TRACKER_BASE_PATH")
        return path ?: javaClass.classLoader.getResourceAsStream("timetracker.properties").use {
            Properties().apply { load(it) }.get("basePath")?.toString()
        } ?: "."
    }

    private fun retrieveBackupPath(): String? {
        val path = System.getenv("TIME_TRACKER_BACKUP_PATH")
        return path ?: javaClass.classLoader.getResourceAsStream("timetracker.properties").use {
            Properties().apply { load(it) }.get("backupPath")?.toString()
        }
    }
}