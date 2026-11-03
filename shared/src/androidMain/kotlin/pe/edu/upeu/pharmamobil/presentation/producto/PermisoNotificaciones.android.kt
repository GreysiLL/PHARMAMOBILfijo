package pe.edu.upeu.pharmamobil.presentation.producto

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobil.domain.permiso.EstadoPermiso
import pe.edu.upeu.pharmamobil.domain.permiso.GestorPermisos

private tailrec fun Context.actividad(): Activity? = when(this) {
    is Activity -> this
    is ContextWrapper -> baseContext.actividad()
    else -> null
}

private class GestorAndroid(private val actividad: Activity) : GestorPermisos {
    private val prefs = actividad.getSharedPreferences("permisos", Context.MODE_PRIVATE)
    private val permiso = Manifest.permission.POST_NOTIFICATIONS
    private var respuesta: CompletableDeferred<Boolean>? = null
    lateinit var lanzar: (String) -> Unit

    override fun estado(): EstadoPermiso = when {
        Build.VERSION.SDK_INT < 33 -> EstadoPermiso.Concedido
        actividad.checkSelfPermission(permiso) == PackageManager.PERMISSION_GRANTED -> EstadoPermiso.Concedido
        !prefs.getBoolean("notificacionesSolicitadas", false) ||
            actividad.shouldShowRequestPermissionRationale(permiso) -> EstadoPermiso.Denegado
        else -> EstadoPermiso.DenegadoParaSiempre
    }
    fun recibir(concedido: Boolean) { respuesta?.complete(concedido) }
    override suspend fun solicitar(): EstadoPermiso {
        if (estado() != EstadoPermiso.Denegado || respuesta != null) return estado()
        val espera = CompletableDeferred<Boolean>()
        respuesta = espera
        return try {
            lanzar(permiso)
            espera.await()
            prefs.edit().putBoolean("notificacionesSolicitadas", true).apply()
            estado()
        } finally { respuesta = null }
    }
    override fun abrirAjustes() {
        actividad.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", actividad.packageName, null)))
    }
}

@Composable
actual fun PermisoNotificaciones() {
    if (Build.VERSION.SDK_INT < 33) return
    val actividad = LocalContext.current.actividad() ?: return
    val gestor = remember(actividad) { GestorAndroid(actividad) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission(), gestor::recibir)
    SideEffect { gestor.lanzar = { launcher.launch(it) } }
    var estado by remember(gestor) { mutableStateOf(gestor.estado()) }
    LifecycleResumeEffect(gestor) {
        estado = gestor.estado()
        onPauseOrDispose { }
    }
    val scope = rememberCoroutineScope()
    when (estado) {
        EstadoPermiso.Concedido -> Text("Permiso de notificaciones concedido")
        EstadoPermiso.Denegado -> {
            Text("Permite las notificaciones para que PharmaMobil pueda enviarte avisos.")
            TextButton(onClick = { scope.launch { estado = gestor.solicitar() } }) { Text("Permitir notificaciones") }
        }
        EstadoPermiso.DenegadoParaSiempre -> {
            Text("Las notificaciones están bloqueadas. Puedes habilitarlas en Ajustes.")
            TextButton(onClick = gestor::abrirAjustes) { Text("Abrir Ajustes") }
        }
    }
}
