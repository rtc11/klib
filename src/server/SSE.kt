package server

import java.io.*

data class Event(
    val data: Any? = "",
    val name: String? = null,
    val id: Any? = null,
) {

    internal fun sendTo(out: OutputStream) {
        id?.let { out.writeln("id: $it") }
        name?.let { out.writeln("event: $it") }
        out.write("data: ")
        out.write(data.toString().replace("\n", "\ndata: "))
        out.writeln("\n")
        out.flush()
    }
}

private fun OutputStream.write(s: String) = write(s.encodeToByteArray())
private fun OutputStream.writeln(s: String) = write("$s\n")
