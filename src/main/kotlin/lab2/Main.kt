package lab2

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.singleWindowApplication
import java.io.*
import java.math.BigInteger
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec


@Throws(IOException::class)
private fun encryption(input: InputStream, output: OutputStream, encrypt: Cipher) {
    var output: OutputStream? = output
    output = CipherOutputStream(output, encrypt)
    //calling the writeBytes() method to write the encrypted bytes to the file
    writeBytes(input, output)
}

//method for decryption
@Throws(IOException::class)
private fun decryption(input: InputStream, output: OutputStream, decrypt: Cipher) {
    var input: InputStream? = input
    input = CipherInputStream(input, decrypt)
    //calling the writeBytes() method to write the decrypted bytes to the file
    writeBytes(input, output)
}

//method for writting bytes to the files
@Throws(IOException::class)
private fun writeBytes(input: InputStream, output: OutputStream) {
    val writeBuffer = ByteArray(512)
    var readBytes = 0
    while (input.read(writeBuffer).also { readBytes = it } >= 0) {
        output.write(writeBuffer, 0, readBytes)
    }
    //closing the output stream
    output.close()
    //closing the input stream
    input.close()
}

@Composable
fun DropdownList(itemList: List<String>, selectedIndex: Int, modifier: Modifier, onItemClick: (Int) -> Unit) {

    var showDropdown by rememberSaveable { mutableStateOf(true) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        // button
        Box(
            modifier = modifier
                .background(Color.Red)
                .clickable { showDropdown = true },
//            .clickable { showDropdown = !showDropdown },
            contentAlignment = Alignment.Center
        ) {
            Text(text = itemList[selectedIndex], modifier = Modifier.padding(3.dp))
        }

        // dropdown list
        Box() {
            if (showDropdown) {
                Popup(
                    alignment = Alignment.TopCenter,

                    // to dismiss on click outside
                    onDismissRequest = { showDropdown = false }
                ) {

                    Column(
                        modifier = modifier
                            .heightIn(max = 90.dp)
                            .verticalScroll(state = scrollState)
                            .border(width = 1.dp, color = Color.Gray),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {

                        itemList.onEachIndexed { index, item ->
                            if (index != 0) {
                                Divider(thickness = 1.dp, color = Color.LightGray)
                            }
                            Box(
                                modifier = Modifier
                                    .background(Color.Green)
                                    .fillMaxWidth()
                                    .clickable {
                                        onItemClick(index)
                                        showDropdown = !showDropdown
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = item)
                            }
                        }

                    }
                }
            }
        }
    }

}

fun performDesEncryption(initialization_vector: String, text: String, keyMap: Map<Int, String>, mode: String): String {
    // Примерная реализация, в реальности здесь должно быть шифрование
    val keyBinaryString = keyMap.toList().sortedBy { it.first }.joinToString("") { it.second }
    var b = BigInteger(keyBinaryString, 2).toByteArray().asList().toMutableList()
    // Предполагается, что вы переведете keyBinaryString в ключ формата DES и выполните шифрование
    for (i in 0 until 8) {
        if (i >= b.size) {
            b.add(i, 0.toByte())
        }
    }

    val scrtkey = object : SecretKey {
        override fun getAlgorithm(): String = "DES"

        override fun getFormat(): String = "RAW"

        override fun getEncoded(): ByteArray = b.toByteArray()
    }

    val aps: AlgorithmParameterSpec = IvParameterSpec(initialization_vector.toByteArray())
    val encrypt = Cipher.getInstance("DES/$mode/PKCS5Padding")
    encrypt.init(Cipher.ENCRYPT_MODE, scrtkey, aps)
    val a = ByteArrayOutputStream()

    encryption(ByteArrayInputStream(text.toByteArray()), a, encrypt)

    return "Encrypted text: $text with key: $keyBinaryString looks like $a"
}

fun main() = singleWindowApplication {
    // Состояние для ввода ключа и текста
    val keyState = remember { mutableStateOf(mutableMapOf<Int, String>()) }
    val textState = remember { mutableStateOf("text") }
    val initializationVectorState = remember { mutableStateOf("20202020") }
    val encryptedTextState = remember { mutableStateOf("") }
    val itemList = listOf("CBC", "CFB", "OFB")
    var selectedIndex by rememberSaveable { mutableStateOf(0) }
    var buttonModifier = Modifier.width(100.dp)


    for (i in 0..63) {
        keyState.value = keyState.value.toMutableMap().apply { this[i] = "0" }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Грид для ввода ключа (64 бита для DES)
        val gridSize = 8
        for (row in 0 until gridSize) {
            Row(modifier = Modifier.padding(bottom = 4.dp)) {
                for (column in 0 until gridSize) {
                    val index = row * gridSize + column

                    BasicTextField(
                        value = keyState.value[index].orEmpty(),
                        onValueChange = {
                            if (it.length <= 1 && it.all { char -> char == '0' || char == '1' }) {
                                keyState.value = keyState.value.toMutableMap().apply { this[index] = it }
                            }
                        },
                        modifier = Modifier
                            .size(30.dp)
                            .padding(2.dp)
                            .align(Alignment.Top)
                            .border(1.dp, Color.Gray),
                        //keyboardType = KeyboardType.Number,
                        // imeAction = if (index == 63) ImeAction.Done else ImeAction.Next
                    )
                }
            }
        }
        Text("Source text")
        // Поле ввода для текста, который будет зашифрован
        BasicTextField(
            value = textState.value,
            onValueChange = {
                textState.value = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .border(1.dp, androidx.compose.ui.graphics.Color.Gray),
            //imeAction = ImeAction.Done,
            //keyboardType = KeyboardType.Text
        )

        Text("Initialization vector")
        BasicTextField(
            value = initializationVectorState.value,
            onValueChange = {
                initializationVectorState.value = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .border(1.dp, androidx.compose.ui.graphics.Color.Gray),
            //imeAction = ImeAction.Done,
            //keyboardType = KeyboardType.Text
        )

        // Кнопка для шифрования текста
        Row(verticalAlignment = Alignment.Top) {
            Text("Mode")
            DropdownList(
                itemList = itemList,
                selectedIndex = selectedIndex,
                modifier = buttonModifier,
                onItemClick = { selectedIndex = it })
            Button(
                onClick = {
                    // Здесь должна быть функция шифрования
                    encryptedTextState.value = performDesEncryption(
                        initializationVectorState.value,
                        textState.value,
                        keyState.value,
                        itemList[selectedIndex]
                    )
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Encrypt")
            }
        }


        // Поля для отображения зашифрованного текста
        Text(
            text = encryptedTextState.value,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

