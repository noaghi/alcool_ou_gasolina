package com.example.alcoolougasolina

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alcoolougasolina.ui.theme.AlcoolOuGasolinaTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.location.Location
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.toMutableStateList
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.compose.material3.Surface

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        enableEdgeToEdge()
        setContent {
            AlcoolOuGasolinaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val scrollState = rememberScrollState()

                    val context = LocalContext.current

                    val prefs = remember { context.getSharedPreferences("PostoPrefs", MODE_PRIVATE) }
                    val gson = Gson()
                    val tipo = object : TypeToken<MutableList<Posto>>() {}.type

                    val listaDePostos = remember {
                        val json = prefs.getString("lista_postos", "[]")
                        val listaInicial = gson.fromJson<List<Posto>>(json, tipo) ?: listOf()
                        listaInicial.toMutableStateList()
                    }

                    var gasValue by rememberSaveable { mutableStateOf("") }
                    var alcValue by rememberSaveable { mutableStateOf("") }
                    var postoValue by rememberSaveable { mutableStateOf("") }
                    var is75Percent by rememberSaveable { mutableStateOf(prefs.getBoolean("usa_75", false)) }

                    val labels = remember {
                        mapOf(
                            "gas" to context.getString(R.string.preco_da_gasolina),
                            "alc" to context.getString(R.string.preco_do_alcool),
                            "posto" to context.getString(R.string.nome_do_posto),
                            "ptoc" to context.getString(R.string.preencha_todos_os_campos),
                            "pscs" to context.getString(R.string.posto_salvo_com_sucesso),
                            "hdp" to context.getString(R.string.historico_de_postos),
                            "editando" to context.getString(R.string.editando),
                            "removido" to context.getString(R.string.removido),
                            "baseadoEm" to context.getString(R.string.baseado_em),
                            "gasP" to context.getString(R.string.gasolinaP),
                            "alcP" to context.getString(R.string.alcoolP),
                            "acg" to context.getString(R.string.use_gasolina),
                            "acc" to context.getString(R.string.use_alcool),
                            "di" to context.getString(R.string.dados_insuficientes),
                            "editar" to context.getString(R.string.editar),
                            "excluir" to context.getString(R.string.excluir)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .verticalScroll(scrollState)
                    ) {
                        Titulo()
                        CampoDeNumero(labels["gas"] ?: "", gasValue) { gasValue = it }
                        CampoDeNumero(labels["alc"] ?: "", alcValue) { alcValue = it }
                        CampoDeTexto(labels["posto"] ?: "", postoValue) { postoValue = it }
                        Switch75(is75Percent) { novoValor ->
                            is75Percent = novoValor
                            prefs.edit { putBoolean("usa_75", novoValor) }
                        }
                        BotaoAddPosto {
                            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                                    val lat = location?.latitude ?: 0.0
                                    val lon = location?.longitude ?: 0.0

                                    val formatter = java.text.SimpleDateFormat("dd/MM/yy HH:mm", java.util.Locale.getDefault())
                                    val dataFormatada = formatter.format(java.util.Date())

                                    val novoPosto = Posto(
                                        nome = postoValue,
                                        gasolina = gasValue,
                                        alcool = alcValue,
                                        usa75 = is75Percent,
                                        dataIns = dataFormatada,
                                        lat = lat,
                                        lon = lon
                                    )

                                    if (postoValue.isNotBlank() && gasValue.isNotBlank() && alcValue.isNotBlank()) {
                                        listaDePostos.removeAll { it.nome == postoValue }
                                        listaDePostos.add(novoPosto)

                                        val novoJson = gson.toJson(listaDePostos.toList())
                                        prefs.edit {putString("lista_postos", novoJson) }

                                        Toast.makeText(context, labels["pscs"] ?: "", Toast.LENGTH_SHORT).show()

                                        postoValue = ""; gasValue = ""; alcValue = ""
                                    } else {
                                        Toast.makeText(context, labels["ptoc"] ?: "", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                (context as? Activity)?.let {
                                    ActivityCompat.requestPermissions(it, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
                                }
                            }
                        }
                        Text(
                            labels["hdp"] ?: "",
                            modifier = Modifier.padding(16.dp),
                            fontWeight = FontWeight.ExtraBold
                        )

                        listaDePostos.forEach { posto ->
                            PostoCard(
                                posto = posto,
                                labels = labels,
                                onEdit = {
                                    postoValue = posto.nome
                                    gasValue = posto.gasolina
                                    alcValue = posto.alcool
                                    is75Percent = posto.usa75

                                    Toast.makeText(context, (labels["editando"] ?: "") + posto.nome, Toast.LENGTH_SHORT).show()
                                },
                                onDelete = {
                                    listaDePostos.remove(posto)

                                    val novoJson = gson.toJson(listaDePostos.toList())
                                    prefs.edit { putString("lista_postos", novoJson) }

                                    Toast.makeText(context, posto.nome + (labels["removido"] ?: ""), Toast.LENGTH_SHORT).show()
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
    val maxLength = 4

    OutlinedTextField(
        value = valor,
        onValueChange = { novoNum ->
            val numTratado = novoNum.replace(',', '.')
            if (numTratado.matches(Regex("^\\d*\\.?\\d*$")) && numTratado.length <= maxLength) {
                onValueChange(numTratado)
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
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onCheckedChange(!checked) }
    ) {
        Text(
            modifier = Modifier.padding(end = 4.dp),
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
fun PostoCard(
    posto: Posto,
    labels: Map<String, String>,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val gasP = labels["gasP"] ?: ""
    val alcP = labels["alcP"] ?: ""
    val acc = labels["acc"] ?: ""
    val acg = labels["acg"] ?: ""
    val di = labels["di"] ?: ""
    val baseadoEm = labels["baseadoEm"] ?: ""
    val editar = labels["editar"] ?: ""
    val excluir = labels["excluir"] ?: ""

    val gasV = posto.gasolina.toDoubleOrNull() ?: 0.0
    val alcV = posto.alcool.toDoubleOrNull() ?: 0.0
    val resultado = if (gasV > 0) {
        val limite = if (posto.usa75) 0.75 else 0.70
        if ( alcV / gasV <= limite ) acc else acg
    } else {
        di
    }

    var expandido by remember {mutableStateOf(true)}

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .animateContentSize()
            .clickable { expandido = !expandido },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = posto.nome, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                Text(text = "$gasP${posto.gasolina} | $alcP${posto.alcool}")

                Text(
                    text = resultado,
                    color = if (resultado == acc) Color(0xFF2E7D32) else Color(0xFFC68728),
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(top = 4.dp)
                )

                if (posto.usa75) {
                    Text(text = baseadoEm, fontSize = 12.sp, color = Color.Gray)
                }
            }

            Column {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = editar,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = excluir,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        if (expandido) {
            Column(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    .fillMaxWidth()
            ) {
                HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
                Text("Latitude: ${posto.lat}", style = MaterialTheme.typography.bodyMedium)
                Text("Longitude: ${posto.lon}", style = MaterialTheme.typography.bodyMedium)
                Text("Data de cadastro: ${posto.dataIns}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Preview(
    name = "Modo Claro",
    showBackground = true,
    locale = "pt"
)
@Preview(
    name = "Modo Escuro",
    showBackground = true,
    locale = "pt",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PreviewTelaDoForm() {
    AlcoolOuGasolinaTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                Titulo()
                CampoDeNumero("Preço da gasolina (R$)", "5.5", onValueChange = {})
                CampoDeNumero("Preço do álcool (R$)", "4.4", onValueChange = {})
                CampoDeTexto("Nome do posto", "shell da Jovita", onValueChange = {})
                Switch75(true, onCheckedChange = {})
                BotaoAddPosto(onClick = {})
                Text(
                    "Histórico de Postos",
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.ExtraBold
                )
                val postoPreview = Posto(
                    "Shell da Zé Bastos",
                    "6.66",
                    "4.20",
                    true,
                    dataIns = "26/04/2026",
                    lat = 1.21,
                    lon = 2.23
                )
                val labelsFake = mapOf(
                    "gasP" to "Gasolina: R$",
                    "alcP" to "Álcool: R$",
                    "acc" to "Abasteça com ÁLCOOL",
                    "acg" to "Abasteça com GASOLINA",
                    "baseadoEm" to "Cálculo baseado no rendimento de 75%",
                )
                PostoCard(postoPreview, labels = labelsFake, onEdit = {}, onDelete = {})
            }
        }
    }
}