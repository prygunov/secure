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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Preview
@Composable
fun App() {
    val coroutineScope = rememberCoroutineScope()
    val pattern = remember { Regex("^\\d+\$") }
    var sourceText by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var iterations by remember { mutableStateOf(1000000000) }
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
        if (sourceText.isBlank())
            return

        var found = false
        message = "Вычисление $hash, ожидайте..."

        val beforeMs = System.currentTimeMillis()

        for (i in 1..iterations) {
            val assumedPassword = i.toString()
            val assumedHash = algorithm.calculateHash(assumedPassword.toByteArray())
            currentIteration = i
            if (assumedHash.contentEquals(hash)) {
                found = true
            }
            if (found)
                break
        }

        val afterMs = System.currentTimeMillis()
        val diff = afterMs - beforeMs
        message = "Вычислен $algorithm, данные: $currentIteration, заняло времени $diff мс."

        if (!found) {
            message = "Данные не найдены в заданном диапазоне"
        }
    }

    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
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
                        if (it.isEmpty() || it.matches(pattern)) {
                            calculateStringHash(it)
                            sourceText = it
                        }
                    },
                    label = { Text("Данные для хэширвоания") }
                )
            }

            for (algorithm in HashAlgorithm.values()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(3.dp),
                ) {
                    TextField(
                        maxLines = 10,
                        value = HashUtils.bytesToHex(hashes[algorithm] ?: emptyByteArr),
                        enabled = false,
                        onValueChange = {},
                        label = { Text("${algorithm.name} (${algorithm.bits} bits)") }
                    )
                    Button(
                        modifier = Modifier.requiredSize(width = 150.dp, height = 50.dp),
                        onClick = {
                            coroutineScope.launch {
                                decodeHash(hashes[algorithm] ?: emptyByteArr, algorithm)
                            }
                        }) {
                        Text("Вскрыть $algorithm")
                    }
                }
            }
            Text("Интервал перебора до: $iterations, текущая итерация: $currentIteration")
            Text(message)
        }
    }
}


fun main() = application {
    Window(title = "Lab 1", onCloseRequest = ::exitApplication) {
        App()
    }
}
