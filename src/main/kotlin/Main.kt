import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Composable
@Preview
fun App() {
    var sourceText by remember { mutableStateOf("password to hash") }
    var iterations by remember { mutableStateOf(10000000) }
    var currentIteration by remember { mutableStateOf(1) }
    val emptyByteArr = "".toByteArray()

    val hashes = mutableStateMapOf(
        HashAlgorithm.MD5 to emptyByteArr,
        HashAlgorithm.SHA1 to emptyByteArr,
        HashAlgorithm.SHA256 to emptyByteArr,
        HashAlgorithm.SHA512 to emptyByteArr
    )

    fun calculateStringHash(text: String) {
        for (algorithm in HashAlgorithm.values()) {
            val hash = algorithm.calculateHash(text.toByteArray())
            hashes[algorithm] = hash
        }
    }

    fun decodeHash(hash: ByteArray, algorithm: HashAlgorithm) {
        //GlobalScope().launch {  }
        var found = false
        for (i in 1..iterations) {
            val assumedPassword = i.toString()
            val assumedHash = algorithm.calculateHash(assumedPassword.toByteArray())
            currentIteration = i
            if (assumedHash.contentEquals(hash)) {
                println("Password is $i")
                found = true
            }
        }
        if (!found) {
            println("Password not found")
        }
    }

    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = sourceText,
                    onValueChange = {
                        calculateStringHash(it)
                        sourceText = it
                                    },
                    label = { Text("Данные для хэширвоания") }
                )
            }

            for (algorithm in HashAlgorithm.values()) {
                Row (
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    TextField(
                        value = HashUtils.bytesToHex(hashes[algorithm] ?: emptyByteArr),
                        enabled = false,
                        onValueChange = {},
                        label = { Text("${algorithm.name} (${algorithm.bits} bits)") }
                    )
                    Button(onClick = {
                        decodeHash(hashes[algorithm]?: emptyByteArr, algorithm)
                    }) {
                        Text("Вскрыть $algorithm")
                    }
                }
            }
            Text("Итераций: $iterations, текущая: $currentIteration()")
        }
    }
}


fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
