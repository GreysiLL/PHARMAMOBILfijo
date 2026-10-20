package pe.edu.upeu.pharmamobil.data.repository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pe.edu.upeu.pharmamobil.data.local.ProductoLocal
import pe.edu.upeu.pharmamobil.data.remote.ProductoApi
import pe.edu.upeu.pharmamobil.data.mapper.*
import pe.edu.upeu.pharmamobil.domain.model.*
import pe.edu.upeu.pharmamobil.domain.error.*
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

@OptIn(ExperimentalCoroutinesApi::class)
class ProductoRepositorioOffline(
 private val local: ProductoLocal,
 private val remoto: ProductoApi,
 private val sesiones: StateFlow<Sesion?>,
 private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : ProductoRepository {
 private val mutex = Mutex()
 private fun usuario() = sesiones.value?.usuario ?: throw ErrorApiException(ErrorApi.SesionExpirada)
 override fun observar(): Flow<List<Producto>> = sesiones.map { it?.usuario }.distinctUntilChanged().flatMapLatest {
  if(it == null) flowOf(emptyList()) else local.observar(it)
 }
 override fun observarPendientes(): Flow<Int> = sesiones.map { it?.usuario }.distinctUntilChanged().flatMapLatest {
  if(it == null) flowOf(0) else local.observarPendientes(it)
 }
 override suspend fun listar() = withContext(dispatcher) { local.listar(usuario()) }
 override suspend fun obtener(id: Long) = withContext(dispatcher) { local.obtener(usuario(), id) }
 override suspend fun registrar(producto: Producto): Producto = withContext(dispatcher) { mutex.withLock {
  val usuario = usuario()
  val guardado = local.guardarPendiente(usuario, producto)
  try { sincronizarDentro(usuario) }
  catch(e: CancellationException) { throw e }
  catch(e: ErrorApiException) {
   if(e.error !in setOf(ErrorApi.SinConexion, ErrorApi.TiempoAgotado) && local.pendientes(usuario).any { it.id == guardado.id }) {
    // Una validación o un permiso rechazado se informa, sin acumular POST inválidos.
    local.borrar(usuario, guardado.id); throw e
   }
  }
  local.listar(usuario).firstOrNull { it.nombre == guardado.nombre && it.id == guardado.id } ?: guardado
 } }
 override suspend fun actualizar(producto: Producto): Producto = withContext(dispatcher) { mutex.withLock {
  val usuario = usuario(); val anterior = local.obtener(usuario, producto.id)
  val guardado = producto.copy(pendiente = true); local.modificar(usuario, guardado)
  try { sincronizarDentro(usuario) }
  catch(e: CancellationException) { throw e }
  catch(e: ErrorApiException) { if(e.error !in setOf(ErrorApi.SinConexion, ErrorApi.TiempoAgotado) && local.pendientes(usuario).any { it.id == anterior.id }) { local.restaurar(usuario, anterior); throw e } }
  guardado
 } }
 override suspend fun eliminar(id: Long) = withContext(dispatcher) { mutex.withLock {
  val usuario = usuario(); val anterior = local.obtener(usuario, id)
  if(id < 0) local.borrar(usuario, id) else {
   local.modificar(usuario, anterior.copy(estado = false))
   try { sincronizarDentro(usuario) }
   catch(e: CancellationException) { throw e }
   catch(e: ErrorApiException) { if(e.error !in setOf(ErrorApi.SinConexion, ErrorApi.TiempoAgotado) && local.pendientes(usuario).any { it.id == id }) { local.restaurar(usuario, anterior); throw e } }
  }
 }; Unit }
 override suspend fun sincronizar() = withContext(dispatcher) { mutex.withLock { sincronizarDentro(usuario()) } }
 private suspend fun sincronizarDentro(usuario: String) {
  // Siempre SUBIR antes de BAJAR. Mutex evita dos envíos simultáneos de la misma fila.
  for(p in local.pendientes(usuario)) {
   comprobarUsuario(usuario)
   if(!p.estado) {
    try { remoto.eliminar(p.id) } catch(e: ErrorApiException) { if(e.error != ErrorApi.NoEncontrado) throw e }
    local.borrar(usuario, p.id)
   } else {
    val confirmado = if(p.id < 0) remoto.crear(p.toRequest(1L)).toDomain()
                      else remoto.actualizar(p.id, p.toRequest(1L)).toDomain()
    local.confirmar(usuario, p.id, confirmado)
   }
  }
  val productos = mutableListOf<Producto>(); var numero = 0
  do {
   comprobarUsuario(usuario)
   val pagina = remoto.listar(numero, 100)
   productos.addAll(pagina.contenido.filter { it.estado }.map { it.toDomain() }); numero++
  } while(!pagina.ultima)
  comprobarUsuario(usuario)
  local.reemplazarSincronizados(usuario, productos)
 }
 private fun comprobarUsuario(esperado: String) { if(usuario() != esperado) throw ErrorApiException(ErrorApi.SesionExpirada) }
}
