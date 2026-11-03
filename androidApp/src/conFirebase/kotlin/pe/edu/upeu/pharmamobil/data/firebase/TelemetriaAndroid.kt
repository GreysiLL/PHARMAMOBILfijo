package pe.edu.upeu.pharmamobil.data.firebase

import pe.edu.upeu.pharmamobil.domain.port.Telemetria
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.crashlytics.crashlytics

internal fun crearTelemetria(): Telemetria = Telemetria { codigo ->
    val crashlytics = Firebase.crashlytics
    crashlytics.setCustomKey("sync_http", (codigo ?: 0).toString())
    crashlytics.recordException(IllegalStateException("Fallo de sincronización de productos"))
}
