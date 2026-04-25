package com.example.alcoolougasolina

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alcoolougasolina.ui.theme.AlcoolOuGasolinaTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.filled.Add

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AlcoolOuGasolinaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val scrollState = rememberScrollState()

                    val context = LocalContext.current

                    val prefs = remember { context.getSharedPreferences("PostoPrefs", Context.MODE_PRIVATE) }
                    val gson = Gson()
                    val tipo = object : TypeToken<MutableList<Posto>>() {}.type

                    var gasValue by rememberSaveable { mutableStateOf("") }
                    var alcValue by rememberSaveable { mutableStateOf("") }
                    var postoValue by rememberSaveable { mutableStateOf("") }
                    var is75Percent by rememberSaveable { mutableStateOf(prefs.getBoolean("usa_75", false)) }

                    val gas = context.getString(R.string.preco_da_gasolina)
                    val alc = context.getString(R.string.preco_do_alcool)
                    val posto = context.getString(R.string.nome_do_posto)
                    val ptoc = context.getString(R.string.preencha_todos_os_campos)
                    val pscs = context.getString(R.string.posto_salvo_com_sucesso)
                    val hdp = context.getString(R.string.historico_de_postos)
                    val editando = context.getString(R.string.editando)
                    val removido = context.getString(R.string.removido)

                    var listaDePostos by remember {
                        val json = prefs.getString("lista_postos", "[]")
                        mutableStateOf(Gson().fromJson<MutableList<Posto>>(json, tipo) ?: mutableListOf() )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .verticalScroll(scrollState)
                    ) {
                        Titulo()
                        CampoDeNumero(gas, gasValue) { gasValue = it }
                        CampoDeNumero(alc, alcValue) { alcValue = it }
                        CampoDeTexto(posto, postoValue) { postoValue = it }
                        Switch75(is75Percent) { novoValor ->
                            is75Percent = novoValor
                            prefs.edit().putBoolean("usa_75", novoValor).apply()
                        }
                        BotaoAddPosto {
                            val jsonExistente = prefs.getString("lista_postos", "[]")

                            val listaAtual: MutableList<Posto> = gson.fromJson(jsonExistente, tipo) ?: mutableListOf()

                            val novoPosto = Posto(
                                nome = postoValue,
                                gasolina = gasValue,
                                alcool = alcValue,
                                usa75 = is75Percent
                            )

                            if (postoValue.isNotBlank() && gasValue.isNotBlank() && alcValue.isNotBlank()) {

                            listaAtual.removeAll { it.nome == postoValue }
                            listaAtual.add(novoPosto)
                            val novoJson = gson.toJson(listaAtual)
                            prefs.edit().putString("lista_postos", novoJson).apply()

                            Toast.makeText(context, pscs, Toast.LENGTH_SHORT).show()

                            postoValue = ""; gasValue = ""; alcValue = ""

                            listaDePostos = listaAtual
                            } else {
                                Toast.makeText(context, ptoc, Toast.LENGTH_SHORT).show()
                            }
                        }
                        Text(
                            hdp,
                            modifier = Modifier.padding(16.dp),
                            fontWeight = FontWeight.ExtraBold
                        )

                        listaDePostos.forEach { posto ->
                            PostoCard(
                                posto = posto,
                                onClick = {
                                    postoValue = posto.nome
                                    gasValue = posto.gasolina
                                    alcValue = posto.alcool
                                    is75Percent = posto.usa75

                                    Toast.makeText(context, editando + posto.nome, Toast.LENGTH_SHORT).show()
                                },
                                onDelete = {
                                    val novaLista = listaDePostos.toMutableList()
                                    novaLista.remove(posto)

                                    listaDePostos = novaLista

                                    val novoJson = gson.toJson(novaLista)
                                    prefs.edit().putString("lista_postos", novoJson).apply()

                                    Toast.makeText(context, posto.nome + removido, Toast.LENGTH_SHORT).show()
                                }
                            )
                            }
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
fun CampoDeNumero(nome: String, valor: String, onValueChange: (String) -> Unit) {
    val decimalRegex = Regex("^\\d*[.,]?\\d*\$")
    val maxLength = 4

    OutlinedTextField(
        value = valor,
        onValueChange = { novoNum ->
            if (novoNum.matches(decimalRegex) && novoNum.length <= maxLength) {
                onValueChange(novoNum.replace(',', '.'))
            }
        },
        label = { Text(nome) },
        placeholder = { Text(nome) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Next,
            ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}

@Composable
fun CampoDeTexto(nome: String, valor: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValueChange,
        label = { Text(nome) },
        placeholder = { Text(nome) },
        singleLine = true,
        keyboardOptions = KeyboardOptions (imeAction = ImeAction.Done),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}

@Composable
fun Switch75(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(16.dp)
    ) {
        Text(
            modifier = Modifier.padding(end = 8.dp),
            text = if (checked) "75%" else "70%"
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun BotaoAddPosto( onClick: () -> Unit ) {
    val context = LocalContext.current
    val add = context.getString(R.string.adicionar)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier.height(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = add,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(add)
        }
    }
}

@Composable
fun PostoCard(posto: Posto, onClick: () -> Unit, onDelete: () -> Unit) {
    val context = LocalContext.current
    val baseadoEm = context.getString(R.string.baseado_em)
    val gasP = context.getString(R.string.gasolinaP)
    val alcP = context.getString(R.string.alcoolP)
    val acg = context.getString(R.string.use_gasolina)
    val acc = context.getString(R.string.use_alcool)
    val di = context.getString(R.string.dados_insuficientes)
    val excluir = context.getString(R.string.excluir)

    val gasV = posto.gasolina.toDoubleOrNull() ?: 0.0
    val alcV = posto.alcool.toDoubleOrNull() ?: 0.0
    val resultado = if (gasV > 0) {
        val limite = if (posto.usa75) 0.75 else 0.70
        if ( alcV / gasV <= limite ) acc else acg
    } else {
        di
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = posto.nome, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = "$gasP${posto.gasolina} | $alcP${posto.alcool}")

                Text(
                    text = resultado,
                    color = if (resultado == acc) Color(0xFF2E7D32) else Color(0xFFC62828),
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(top = 4.dp)
                )

                if (posto.usa75) {
                    Text(text = baseadoEm, fontSize = 12.sp, color = Color.Gray)
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = excluir,
                    tint = Color.Red
                )
            }
        }
    }
}

@Preview(showBackground = true, locale = "pt-rBR")
@Composable
fun PreviewTelaDoForm() {
    Column() {
        Titulo()
        CampoDeNumero("Preço da gasolina (R$)", "5.5", onValueChange = {})
        CampoDeNumero("Preço do álcool (R$)", "4.4", onValueChange = {})
        CampoDeTexto("Nome do posto", "shell da Jovita", onValueChange = {})
        Switch75(true, onCheckedChange = {})
        BotaoAddPosto(onClick = {})
        val postoPreview = Posto (
            "Shell do Feira Center",
            "6.54",
            "4.54",
            true
        )
        PostoCard(postoPreview, onClick = {}, onDelete = {})
    }
}