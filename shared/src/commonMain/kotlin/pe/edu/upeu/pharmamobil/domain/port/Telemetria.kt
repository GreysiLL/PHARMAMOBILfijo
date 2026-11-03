package pe.edu.upeu.pharmamobil.domain.port

fun interface Telemetria {
    fun registrarFalloDeSincronizacion(codigoHttp: Int?)
}
