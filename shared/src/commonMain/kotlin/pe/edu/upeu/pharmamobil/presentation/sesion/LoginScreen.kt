package pe.edu.upeu.pharmamobil.presentation.sesion

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(estado: SesionUiState, aviso: String?, usuario: (String) -> Unit, clave: (String) -> Unit, login: () -> Unit, restaurar: () -> Unit, verCatalogoLocal: () -> Unit = {}) {
    var visible by rememberSaveable { mutableStateOf(false) }
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(Modifier.fillMaxSize().safeDrawingPadding().imePadding(), contentAlignment = Alignment.Center) {
            Column(Modifier.widthIn(max = 440.dp).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 32.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.primary) {
                        Icon(Icons.Outlined.LocalPharmacy, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.padding(14.dp).size(26.dp))
                    }
                    Column {
                        Text("PharmaMobil", style = MaterialTheme.typography.headlineSmall)
                        Text("Gestión farmacéutica", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Iniciar sesión", style = MaterialTheme.typography.titleLarge)
                        Text("Ingresa con tu cuenta para continuar.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if(estado.iniciando) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) { CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp); Text("Comprobando sesión…") }
                        } else {
                            aviso?.let { AvisoLogin(it) }
                            OutlinedTextField(estado.usuario, usuario, label = { Text("Usuario") }, leadingIcon = { Icon(Icons.Outlined.Person, null) }, singleLine = true, enabled = !estado.cargando, shape = MaterialTheme.shapes.small, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next), modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(estado.clave, clave, label = { Text("Contraseña") }, leadingIcon = { Icon(Icons.Outlined.Lock, null) }, trailingIcon = {
                                IconButton(onClick = { visible = !visible }) { Icon(if(visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, contentDescription = if(visible) "Ocultar contraseña" else "Mostrar contraseña") }
                            }, singleLine = true, enabled = !estado.cargando, shape = MaterialTheme.shapes.small, visualTransformation = if(visible) VisualTransformation.None else PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done), keyboardActions = KeyboardActions(onDone = { if(!estado.cargando) login() }), modifier = Modifier.fillMaxWidth())
                            estado.error?.let { AvisoLogin(it) }
                            Button(onClick = login, enabled = !estado.cargando, shape = MaterialTheme.shapes.small, modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp)) {
                                if(estado.cargando) { CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary); Spacer(Modifier.width(10.dp)) }
                                Text(if(estado.cargando) "Ingresando…" else "Ingresar")
                            }
                            TextButton(onClick = verCatalogoLocal) { Text("Ver catálogo sin conexión") }
                            if(estado.error != null) TextButton(onClick = restaurar) { Text("Reintentar sesión guardada") }
                        }
                    }
                }
                Text("Tu farmacia, en un solo lugar.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }
}

@Composable
private fun AvisoLogin(texto: String) {
    Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer) {
        Text(texto, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.fillMaxWidth().padding(12.dp))
    }
}
