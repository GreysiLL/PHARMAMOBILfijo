package pe.edu.upeu.pharmamobil.data.repository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.plugins.auth.AuthCircuitBreaker
import io.ktor.client.request.*
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import pe.edu.upeu.pharmamobil.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobil.data.remote.dto.*
import pe.edu.upeu.pharmamobil.domain.model.*
import pe.edu.upeu.pharmamobil.domain.repository.AuthRepository
class AuthRepositorioRest(private val cliente: HttpClient, private val almacen: AlmacenSeguro, private val estado: EstadoSesion, private val cache: pe.edu.upeu.pharmamobil.data.local.SesionLocal? = null) : AuthRepository {
    override suspend fun iniciarSesion(credenciales: Credenciales): Sesion = traducir {
        val dto = cliente.post("auth/login") { attributes.put(AuthCircuitBreaker, Unit); setBody(LoginRequestDto(credenciales.usuario, credenciales.clave)) }.body<LoginResponseDto>()
        val sesion = Sesion(dto.token, dto.username, dto.nombreCompleto, dto.permisos.toSet())
        try { almacen.guardarToken(dto.token) } catch(e: Exception) { throw ErrorSesionException(ErrorSesion.Almacenamiento) }
        cliente.authProvider<BearerAuthProvider>()?.clearToken()
        cache?.guardar(sesion)
        estado.activar(sesion)
        sesion
    }
    override suspend fun sesionGuardada(): Sesion? = traducir {
        val token = try { almacen.leerToken() } catch(e: Exception) { throw ErrorSesionException(ErrorSesion.Almacenamiento) } ?: return@traducir null
        cliente.authProvider<BearerAuthProvider>()?.clearToken()
        try {
            val usuario = cliente.get("auth/me").body<UsuarioSesionDto>()
            Sesion(token, usuario.username, usuario.empleadoNombre, usuario.permisos.toSet()).also { cache?.guardar(it); estado.activar(it) }
        } catch(e: ClientRequestException) {
            if(e.response.status.value == 401) null else throw e
        } catch(e: IOException) { recuperarSinRed(token, e) }
          catch(e: HttpRequestTimeoutException) { recuperarSinRed(token, e) }
    }
    private fun recuperarSinRed(token: String, fallo: Exception): Sesion {
        val sesion = cache?.recuperar(token) ?: throw fallo
        estado.activar(sesion)
        return sesion
    }
    override suspend fun cerrarSesion() {
        try { estado.limpiar() } finally { cliente.authProvider<BearerAuthProvider>()?.clearToken() }
    }
    private suspend fun <T> traducir(block: suspend () -> T): T = try { block() }
    catch(e: CancellationException) { throw e }
    catch(e: ClientRequestException) { throw ErrorSesionException(when(e.response.status.value) {401 -> ErrorSesion.CredencialesInvalidas;403 -> ErrorSesion.SinPermiso;else -> ErrorSesion.Servidor}) }
    catch(e: IOException) { throw ErrorSesionException(ErrorSesion.SinConexion) }
    catch(e: HttpRequestTimeoutException) { throw ErrorSesionException(ErrorSesion.SinConexion) }
    catch(e: ServerResponseException) { throw ErrorSesionException(ErrorSesion.Servidor) }
}
