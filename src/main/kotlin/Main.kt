import java.io.OutputStream
import java.net.Socket
import java.nio.charset.Charset
import java.util.*
import kotlin.concurrent.thread
import kotlin.system.exitProcess

fun main() {
    val client = Client("localhost", 1805)
    client.run()
}

class Client(address: String, port: Int) {
    private val connection: Socket = Socket(address, port)
    private var connected: Boolean = true
    private lateinit var readThread: Thread
    private lateinit var writeThread: Thread

    init {
        println("Connected to server at $address on port $port")
    }

    private val reader: Scanner = Scanner(connection.getInputStream())
    private val writer: OutputStream = connection.getOutputStream()

    fun run() {
        readThread = thread { read() }
        writeThread = thread {
            while (connected) {
                val input = readLine() ?: ""
                if ("exit" in input) {
                    connected = false
                    write(input)
                    shutdown()
                } else {
                    write(input)
                }
            }
        }

    }

    fun changeUsername(newUsername: String) {
        write("/nick $newUsername")
    }

    private fun write(message: String) {
        if (connected)
            writer.write((message + '\n').toByteArray(Charset.defaultCharset()))
    }

    fun shutdown() {
        connected = false
        reader.close()
        connection.close()
        readThread.stop()
        writeThread.stop()
    }

    private fun read() {
        try {
            while (connected)
                println(reader.nextLine())
        } catch (e: Exception) {
            print("Сервер неожиданно прервал существующее подключение.")
            exitProcess(0)
        }
    }
}