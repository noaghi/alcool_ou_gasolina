package com.example.alcoolougasolina

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alcoolougasolina.ui.theme.AlcoolOuGasolinaTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AlcoolOuGasolinaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val scrollState = rememberScrollState()
                    val context = LocalContext.current
                    val gas = context.getString(R.string.preco_da_gasolina)
                    val alc = context.getString(R.string.preco_do_alcool)
                    val posto = context.getString(R.string.nome_do_posto)

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .verticalScroll(scrollState)
                    ) {
                        Titulo()
                        CampoDeNumero(gas)
                        CampoDeNumero(alc)
                        CampoDeTexto(posto)
                        Switch75()
                        BotaoAddPosto()
                    }
                }
            }
        }
    }
}

@Composable
fun Titulo() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier
                .padding(16.dp),
            text = stringResource(id = R.string.titulo),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CampoDeNumero(nome: String, maxLength: Int = 4) {
    var num by rememberSaveable() { mutableStateOf("") }
    var decimalRegex = Regex("^\\d*[.,]?\\d*\$")

    OutlinedTextField(
        value = num,
        onValueChange = { novoNum ->
            if (novoNum.matches(decimalRegex) && novoNum.length <= maxLength) {
                num = novoNum.replace(',', '.')
            }
        },
        label = { Text(nome) },
        placeholder = { Text(nome) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}

@Composable
fun CampoDeTexto(nome: String) {
    var text by rememberSaveable { mutableStateOf("") }

    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        label = { Text(nome) },
        placeholder = { Text(nome) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}

@Composable
fun Switch75() {
    var checked by rememberSaveable { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(16.dp)
    ) {
        Text(modifier = Modifier
            .padding(end = 8.dp),
            text = "75%")
        Switch(
            checked = checked,
            onCheckedChange = {
                checked = it
            }
        )
    }
}

@Composable
fun BotaoAddPosto() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.CenterEnd
    ){
        Button(onClick = { /* fazer */ }) {
            Text("+")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Preview() {
    Column() {
        Titulo()
        CampoDeNumero(stringResource(R.string.preco_da_gasolina))
        CampoDeNumero(stringResource(R.string.preco_do_alcool))
        CampoDeTexto(stringResource(R.string.nome_do_posto))
        Switch75()
        BotaoAddPosto()
    }
}