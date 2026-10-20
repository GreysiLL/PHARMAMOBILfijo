package pe.edu.upeu.pharmamobil.presentation.producto

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upeu.pharmamobil.presentation.components.*

@Composable
fun ProductoScreen(viewModel: ProductoViewModel, modifier: Modifier = Modifier, onAbrirDetalle: (Long) -> Unit = {}, puedeEscribir: Boolean = true) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var porEliminar by remember { mutableStateOf<ProductoUi?>(null) }
    var mostrarFormulario by rememberSaveable { mutableStateOf(false) }
    porEliminar?.let { producto ->
        AlertDialog(onDismissRequest = { porEliminar = null }, title = { Text("Eliminar producto") }, text = { Text("¿Eliminar ${producto.nombre} del inventario?") }, confirmButton = { TextButton(onClick = { porEliminar = null; viewModel.eliminar(producto.id) }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) } }, dismissButton = { TextButton(onClick = { porEliminar = null }) { Text("Cancelar") } })
    }
    DisposableEffect(viewModel) { onDispose { viewModel.cancelarOperaciones() } }
    LaunchedEffect(uiState.formulario.idEdicion) { if(uiState.formulario.idEdicion != null) mostrarFormulario = true }
    LaunchedEffect(uiState.mensajeExito) { if(uiState.mensajeExito != null) mostrarFormulario = false }

    Column(modifier.fillMaxSize().imePadding().padding(horizontal = 20.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Inventario", style = MaterialTheme.typography.headlineSmall)
                val fase = uiState.fase
                Text(if(fase is ProductoUiState.Fase.ConProductos) "${fase.productos.size} productos" else "Catálogo de medicamentos", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if(puedeEscribir) {
                if(mostrarFormulario) TextButton(enabled = !uiState.registrando, onClick = { viewModel.cancelarEdicion(); mostrarFormulario = false }) { Text("Cerrar formulario") }
                else Button(enabled = !uiState.registrando, onClick = { mostrarFormulario = true }, shape = MaterialTheme.shapes.small) { Icon(Icons.Outlined.Add, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Nuevo producto") }
            }
        }
        if(puedeEscribir && mostrarFormulario) FormularioProductoCard(uiState.formulario, uiState.registrando, viewModel::onNombreChange, viewModel::onPrecioChange, viewModel::onStockChange, viewModel::registrar) { viewModel.cancelarEdicion(); mostrarFormulario = false }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(if(uiState.pendientes > 0) "${uiState.pendientes} pendiente(s) de envío" else "Datos guardados en el dispositivo", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = viewModel::sincronizar, enabled = !uiState.sincronizando && !uiState.registrando) {
                Icon(Icons.Outlined.Sync, null, Modifier.size(18.dp)); Spacer(Modifier.width(5.dp))
                Text(if(uiState.sincronizando) "Sincronizando…" else "Sincronizar")
            }
        }
        uiState.avisoRed?.let { aviso ->
            Surface(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.secondaryContainer) {
                Text(aviso, Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
        }
        if(uiState.sincronizando) LinearProgressIndicator(Modifier.fillMaxWidth())
        uiState.mensajeExito?.let { MensajeExito(it) }
        (uiState.operacion as? ProductoUiState.Operacion.Fallida)?.let { error ->
            Surface(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.errorContainer) { Column(Modifier.padding(12.dp)) { Text(error.mensaje, color = MaterialTheme.colorScheme.onErrorContainer); TextButton(onClick = viewModel::cargarProductos) { Text("Actualizar listado") } } }
        }
        if(uiState.operacion is ProductoUiState.Operacion.EnCurso) LinearProgressIndicator(Modifier.fillMaxWidth())
        Box(Modifier.fillMaxWidth().weight(1f)) {
            when(val fase = uiState.fase) {
                ProductoUiState.Fase.Cargando -> Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) { CircularProgressIndicator(); Text("Cargando inventario…", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                ProductoUiState.Fase.SinProductos -> EstadoVacio(icono = Icons.Outlined.Inventory2, titulo = "Todavía no hay productos", descripcion = "Registra el primer medicamento para comenzar.", modifier = Modifier.align(Alignment.Center))
                is ProductoUiState.Fase.ConProductos -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 16.dp)) { items(fase.productos, key = { it.id }) { producto -> ProductoItem(producto, { onAbrirDetalle(producto.id) }, uiState.registrando, { viewModel.editar(producto.id) }, { porEliminar = producto }, puedeEscribir) } }
                is ProductoUiState.Fase.Error -> EstadoVacio(icono = Icons.Outlined.CloudOff, titulo = "No pudimos cargar el inventario", descripcion = fase.mensaje, colorIcono = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center), accion = { FilledTonalButton(onClick = viewModel::cargarProductos) { Text("Reintentar") } })
            }
        }
    }
}

@Composable
private fun FormularioProductoCard(formulario: FormularioProducto, registrando: Boolean, onNombreChange: (String) -> Unit, onPrecioChange: (String) -> Unit, onStockChange: (String) -> Unit, onRegistrar: () -> Unit, onCancelar: () -> Unit) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(if(formulario.idEdicion == null) "Registrar producto" else "Editar producto #${formulario.idEdicion}", style = MaterialTheme.typography.titleMedium)
            ValidatedTextField(formulario.nombre, onNombreChange, "Nombre", formulario.nombreError, leadingIcon = Icons.Outlined.Medication)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ValidatedTextField(formulario.precio, onPrecioChange, "Precio", formulario.precioError, ayuda = "En soles", keyboardType = KeyboardType.Decimal, modifier = Modifier.weight(1f))
                ValidatedTextField(formulario.stock, onStockChange, "Stock", formulario.stockError, ayuda = "Unidades", keyboardType = KeyboardType.Number, modifier = Modifier.weight(1f))
            }
            Button(onClick = onRegistrar, enabled = !registrando, shape = MaterialTheme.shapes.small, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(if(registrando) "Guardando…" else if(formulario.idEdicion != null) "Guardar cambios" else "Registrar") }
            if(formulario.idEdicion != null) TextButton(enabled = !registrando, onClick = onCancelar) { Text("Cancelar edición") }
        }
    }
}

@Composable
private fun ProductoItem(producto: ProductoUi, onAbrir: () -> Unit, ocupado: Boolean, onEditar: () -> Unit, onEliminar: () -> Unit, puedeEscribir: Boolean) {
    Card(onClick = onAbrir, modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.primary) { Icon(Icons.Outlined.Medication, null, Modifier.padding(10.dp).size(22.dp)) }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) { Text(producto.nombre, style = MaterialTheme.typography.titleMedium); Text(producto.precio, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary) }
                Icon(Icons.Outlined.ChevronRight, "Ver detalle", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            }
            if(producto.pendiente) Text("Pendiente de sincronización", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = MaterialTheme.shapes.extraSmall, color = if(producto.requiereReposicion) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer, contentColor = if(producto.requiereReposicion) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSecondaryContainer) { Text(if(producto.requiereReposicion) "${producto.stock} · Reponer" else "Stock: ${producto.stock}", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) }
                Spacer(Modifier.weight(1f))
                if(puedeEscribir) {
                    IconButton(onClick = onEditar, enabled = !ocupado) { Icon(Icons.Outlined.Edit, "Editar ${producto.nombre}", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp)) }
                    IconButton(onClick = onEliminar, enabled = !ocupado) { Icon(Icons.Outlined.DeleteOutline, "Eliminar ${producto.nombre}", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp)) }
                }
            }
        }
    }
}
