package blora.command

import blora.BloraPlugin
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Appends every private message to plugins/blora/logs/messages/yyyy-MM-dd.log
 */
object PrivateMessageLogger {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    private val lock = ReentrantLock()

    private var currentDate: LocalDate? = null
    private var writer: BufferedWriter? = null

    private val logDirectory: File
        get() = File(BloraPlugin.dataDirectory.toFile(), "logs${File.separator}messages")

    fun log(sender: String, receiver: String, message: String) {
        val now = LocalDateTime.now()
        val line = "[${now.toLocalTime().format(timeFormatter)}] $sender → $receiver: $message"
        try {
            appendLine(now.toLocalDate(), line)
        } catch (ex: Exception) {
            BloraPlugin.log.warn("Failed to write private message log: ${ex.message}")
        }
    }

    fun close() {
        lock.withLock {
            closeWriterUnlocked()
        }
    }

    private fun appendLine(date: LocalDate, line: String) {
        lock.withLock {
            ensureWriter(date)
            val out = writer ?: return
            out.write(line)
            out.newLine()
            out.flush()
        }
    }

    private fun ensureWriter(date: LocalDate) {
        if (writer != null && currentDate == date) {
            return
        }
        closeWriterUnlocked()
        val dir = logDirectory
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val file = File(dir, "${date.format(dateFormatter)}.log")
        if (!file.exists()) {
            file.createNewFile()
        }
        writer = BufferedWriter(
            OutputStreamWriter(FileOutputStream(file, true), StandardCharsets.UTF_8)
        )
        currentDate = date
    }

    private fun closeWriterUnlocked() {
        try {
            writer?.close()
        } catch (_: Exception) {
        }
        writer = null
        currentDate = null
    }

}
