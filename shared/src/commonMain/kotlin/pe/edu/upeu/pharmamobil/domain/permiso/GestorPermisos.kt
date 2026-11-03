package pe.edu.upeu.pharmamobil.domain.permiso

enum class EstadoPermiso { Concedido, Denegado, DenegadoParaSiempre }

interface GestorPermisos {
    fun estado(): EstadoPermiso
    suspend fun solicitar(): EstadoPermiso
    fun abrirAjustes()
}
