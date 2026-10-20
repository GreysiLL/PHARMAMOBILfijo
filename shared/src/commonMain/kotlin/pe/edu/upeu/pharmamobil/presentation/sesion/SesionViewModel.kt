package pe.edu.upeu.pharmamobil.presentation.sesion
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobil.domain.repository.ObservadorSesion
import pe.edu.upeu.pharmamobil.domain.repository.AuthRepository
import pe.edu.upeu.pharmamobil.domain.usecase.*
data class SesionUiState(val usuario: String = "", val clave: String = "", val cargando: Boolean = false, val iniciando: Boolean = true, val error: String? = null, val catalogoVacio: Boolean = false)
class SesionViewModel(private val repo: AuthRepository, private val iniciar: IniciarSesionUseCase, private val cerrar: CerrarSesionUseCase, estado: ObservadorSesion) : ViewModel() {
    val sesion = estado.sesion
    val aviso = estado.aviso
    private val actual = MutableStateFlow(SesionUiState())
    val uiState = actual.asStateFlow()
    init { restaurar() }
    fun restaurar() { viewModelScope.launch {
        actual.update { it.copy(iniciando = true, error = null) }
        try { repo.sesionGuardada() } catch(e: CancellationException) { throw e }
        catch(e: Exception) { actual.update { it.copy(error = e.message ?: "No pudimos recuperar tu sesión.") } }
        finally { actual.update { it.copy(iniciando = false) } }
    } }
    fun verCatalogoVacio() { actual.update { it.copy(catalogoVacio = true) } }
    fun volverLogin() { actual.update { it.copy(catalogoVacio = false) } }
    fun usuario(v: String) { if(!actual.value.cargando) actual.update { it.copy(usuario = v, error = null) } }
    fun clave(v: String) { if(!actual.value.cargando) actual.update { it.copy(clave = v, error = null) } }
    fun login() {
        if(actual.value.cargando) return
        val datos = actual.value
        if(datos.usuario.isBlank() || datos.clave.isBlank()) { actual.update { it.copy(error = "Ingresa tu usuario y contraseña.") }; return }
        actual.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            try { iniciar(datos.usuario, datos.clave); actual.value = SesionUiState(iniciando = false) }
            catch(e: CancellationException) { throw e }
            catch(e: Exception) { actual.update { it.copy(error = e.message ?: "No pudimos iniciar sesión.", clave = "") } }
            finally { actual.update { it.copy(cargando = false) } }
        }
    }
    fun logout() { viewModelScope.launch {
        try { cerrar(); actual.value = SesionUiState(iniciando = false) }
        catch(e: Exception) { actual.update { it.copy(error = "No pudimos borrar la sesión del almacén seguro.") } }
    } }
}
