package pe.edu.upeu.pharmamobil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import org.koin.core.context.GlobalContext
import pe.edu.upeu.pharmamobil.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobil.data.repository.EstadoSesion
import pe.edu.upeu.pharmamobil.db.PharmaDb
import kotlin.time.Clock

/** Fixtures explícitas y exclusivas de la instalación aislada .aa11; nunca en release. */
class PruebasAutonoma11Activity : ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  if(!packageName.endsWith(".aa11")) { finish(); return }
  val koin=GlobalContext.get()
  setContent { MaterialTheme {
   var mensaje by remember { mutableStateOf("Preparación de escenarios controlados. No modifica la app habitual.") }
   Column(Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),verticalArrangement=Arrangement.spacedBy(20.dp)) {
    Text("Escenarios de prueba AA11",style=MaterialTheme.typography.headlineSmall)
    Text("Instalación aislada .aa11")
    Button(onClick={
     val usuario=koin.get<EstadoSesion>().sesion.value?.usuario ?: "admin"
     koin.get<PharmaDb>().sincronizacionQueries.guardar(usuario,Clock.System.now().toEpochMilliseconds()-2*86_400_000L)
     mensaje="Fixture aplicada: última sincronización de hace dos días."
    }) { Text("Simular antigüedad de dos días") }
    Button(onClick={
     val archivo=java.io.File(filesDir,"token-vencido-fixture.txt")
     if(archivo.exists()) {
      koin.get<AlmacenSeguro>().guardarToken(archivo.readText().trim())
      koin.get<HttpClient>().authProvider<BearerAuthProvider>()?.clearToken()
      archivo.delete()
      mensaje="Token vencido de prueba cargado. Cierra y reabre sin red."
     } else mensaje="Falta el archivo privado de la fixture."
    }) { Text("Cargar token vencido de prueba") }
    Text(mensaje)
    TextButton(onClick={finish()}) { Text("Volver a PharmaMobil") }
   }
  } }
 }
}
