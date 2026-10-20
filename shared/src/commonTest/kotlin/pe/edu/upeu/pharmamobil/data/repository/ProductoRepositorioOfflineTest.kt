package pe.edu.upeu.pharmamobil.data.repository
import app.cash.sqldelight.db.SqlDriver
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import pe.edu.upeu.pharmamobil.data.local.*
import pe.edu.upeu.pharmamobil.data.remote.*
import pe.edu.upeu.pharmamobil.db.PharmaDb
import pe.edu.upeu.pharmamobil.domain.model.*
import pe.edu.upeu.pharmamobil.domain.error.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class ProductoRepositorioOfflineTest {
 private val drivers = mutableListOf<SqlDriver>()
 private val clientes = mutableListOf<HttpClient>()
 private val sesion = Sesion("token", "admin", "Administrador", setOf("CATALOGO_ESCRIBIR"))
 private fun base() = driverEnMemoria().also { drivers.add(it) }.let { PharmaDb(it) }
 private fun cliente(engine: MockEngine) = crearHttpClient(engine,"http://localhost/api/v1/",esDepuracion=false).also { clientes.add(it) }
 private fun pagina(contenido: String="") = """{"contenido":[$contenido],"pagina":0,"tamanio":100,"totalElementos":1,"totalPaginas":1,"ultima":true}"""
 private fun producto(id:Long=11) = """{"id":$id,"nombre":"Nuevo offline","precio":4.5,"stock":20,"estado":true,"categoriaId":1}"""
 private fun MockRequestHandleScope.json(cuerpo:String, codigo:HttpStatusCode=HttpStatusCode.OK) = respond(cuerpo,codigo,headersOf(HttpHeaders.ContentType,"application/json"))
 @AfterTest fun cerrar() { clientes.forEach { it.close() };drivers.forEach { it.close() } }

 @Test fun sinRedGuardaUnIdNegativoPendienteYElFlowLoEmite() = runTest {
  val d=UnconfinedTestDispatcher(testScheduler);val local=ProductoLocal(base(),d)
  val repo=ProductoRepositorioOffline(local,ProductoApi(cliente(MockEngine { throw kotlinx.io.IOException("sin red") })),MutableStateFlow(sesion),d)
  val creado=repo.registrar(Producto(0,"Nuevo offline",4.5,20))
  assertTrue(creado.id < 0);assertTrue(creado.pendiente)
  val fila=repo.observar().first().single();assertEquals(creado,fila)
 }
 @Test fun reconectarSubeAntesDeBajarYReemplazaIdLocalSinDuplicar() = runTest {
  val d=UnconfinedTestDispatcher(testScheduler);val local=ProductoLocal(base(),d);var red=false;val metodos=mutableListOf<String>()
  val api=ProductoApi(cliente(MockEngine { req ->
   if(!red) throw kotlinx.io.IOException("sin red")
   metodos.add(req.method.value)
   if(req.method==HttpMethod.Post) json(producto(),HttpStatusCode.Created) else json(pagina(producto()))
  }))
  val repo=ProductoRepositorioOffline(local,api,MutableStateFlow(sesion),d)
  repo.registrar(Producto(0,"Nuevo offline",4.5,20));red=true;repo.sincronizar();repo.sincronizar()
  assertEquals(listOf("POST","GET","GET"),metodos)
  val p=repo.observar().first().single();assertEquals(11,p.id);assertFalse(p.pendiente);assertTrue(local.pendientes("admin").isEmpty())
 }
 @Test fun postConfirmadoNoSeRepiteSiFallaLaDescarga() = runTest {
  val d=UnconfinedTestDispatcher(testScheduler);val local=ProductoLocal(base(),d);var getsFallan=true;var posts=0
  local.guardarPendiente("admin",Producto(0,"Nuevo offline",4.5,20))
  val repo=ProductoRepositorioOffline(local,ProductoApi(cliente(MockEngine { req ->
   if(req.method==HttpMethod.Post) { posts++;json(producto(),HttpStatusCode.Created) }
   else if(getsFallan) throw kotlinx.io.IOException("falló GET") else json(pagina(producto()))
  })),MutableStateFlow(sesion),d)
  assertFailsWith<ErrorApiException> { repo.sincronizar() }
  assertTrue(local.pendientes("admin").isEmpty());assertEquals(11,local.listar("admin").single().id)
  getsFallan=false;repo.sincronizar();assertEquals(1,posts)
 }
 @Test fun unaSubidaFallidaImpideBajarYConservaTodosLosPendientes() = runTest {
  val d=UnconfinedTestDispatcher(testScheduler);val local=ProductoLocal(base(),d);var gets=0
  repeat(2) { local.guardarPendiente("admin",Producto(0,"Producto $it",4.5,20)) }
  val repo=ProductoRepositorioOffline(local,ProductoApi(cliente(MockEngine { req ->
   if(req.method==HttpMethod.Get) gets++
   json("""{"message":"sin permiso"}""",HttpStatusCode.Forbidden)
  })),MutableStateFlow(sesion),d)
  val fallo=assertFailsWith<ErrorApiException> { repo.sincronizar() }
  assertEquals(ErrorApi.SinPermiso,fallo.error);assertEquals(0,gets);assertEquals(2,local.pendientes("admin").size)
 }
 @Test fun bajarLaListaNoBorraUnaFilaPendiente() = runTest {
  val local=ProductoLocal(base(),UnconfinedTestDispatcher(testScheduler))
  val p=local.guardarPendiente("admin",Producto(0,"Pendiente",2.0,3))
  local.reemplazarSincronizados("admin",listOf(Producto(9,"Servidor",3.0,10)))
  assertEquals(2,local.listar("admin").size);assertEquals(p,local.pendientes("admin").single())
 }
 @Test fun losDatosYLosPendientesNoSeMezclanEntreUsuarios() = runTest {
  val d=UnconfinedTestDispatcher(testScheduler);val local=ProductoLocal(base(),d);val sesiones=MutableStateFlow<Sesion?>(sesion)
  val repo=ProductoRepositorioOffline(local,ProductoApi(cliente(MockEngine { json(pagina()) })),sesiones,d)
  local.guardarPendiente("admin",Producto(0,"Privado admin",2.0,3))
  assertEquals(1,repo.observar().first().size)
  sesiones.value=sesion.copy(usuario="almacen");assertTrue(repo.observar().first().isEmpty())
  repo.sincronizar();assertEquals(1,local.pendientes("admin").size)
 }
 @Test fun editarYEliminarUnPendienteNuncaEnviaUnIdNegativoALaApi() = runTest {
  val d=UnconfinedTestDispatcher(testScheduler);val local=ProductoLocal(base(),d);val rutas=mutableListOf<String>()
  val repo=ProductoRepositorioOffline(local,ProductoApi(cliente(MockEngine { req -> rutas.add(req.url.encodedPath);throw kotlinx.io.IOException("sin red") })),MutableStateFlow(sesion),d)
  val creado=repo.registrar(Producto(0,"Nuevo offline",4.5,20))
  repo.actualizar(creado.copy(precio=6.0));assertEquals(6.0,repo.observar().first().single().precio)
  repo.eliminar(creado.id);assertTrue(repo.observar().first().isEmpty());assertTrue(rutas.all { it.endsWith("/productos") })
 }
 @Test fun laBaseConservaDatosAlCrearOtroRepositorioSinRed() = runTest {
  val d=UnconfinedTestDispatcher(testScheduler);val b=base();val local=ProductoLocal(b,d)
  local.reemplazarSincronizados("admin",listOf(Producto(10,"Guardado",3.0,10)))
  val nuevo=ProductoRepositorioOffline(ProductoLocal(b,d),ProductoApi(cliente(MockEngine { throw kotlinx.io.IOException("apagado") })),MutableStateFlow(sesion),d)
  assertFailsWith<ErrorApiException> { nuevo.sincronizar() }
  assertEquals("Guardado",nuevo.observar().first().single().nombre)
 }
 @Test fun dosSincronizacionesSimultaneasNoDuplicanElPost() = runTest {
  val d=UnconfinedTestDispatcher(testScheduler);val local=ProductoLocal(base(),d);var posts=0
  local.guardarPendiente("admin",Producto(0,"Nuevo offline",4.5,20))
  val repo=ProductoRepositorioOffline(local,ProductoApi(cliente(MockEngine { req ->
   if(req.method==HttpMethod.Post) { posts++;delay(100);json(producto(),HttpStatusCode.Created) }
   else json(pagina(producto()))
  })),MutableStateFlow(sesion),d)
  coroutineScope { launch { repo.sincronizar() };launch { repo.sincronizar() } }
  assertEquals(1,posts);assertEquals(1,repo.observar().first().size)
 }
 @Test fun deleteConfirmadoNoResucitaLaFilaSiElGetResponde500() = runTest {
  val d=UnconfinedTestDispatcher(testScheduler);val local=ProductoLocal(base(),d)
  local.reemplazarSincronizados("admin",listOf(Producto(11,"Servidor",4.5,20)))
  val repo=ProductoRepositorioOffline(local,ProductoApi(cliente(MockEngine { req ->
   if(req.method==HttpMethod.Delete) respond("",HttpStatusCode.NoContent)
   else json("{}",HttpStatusCode.InternalServerError)
  })),MutableStateFlow(sesion),d)
  repo.eliminar(11)
  assertTrue(repo.observar().first().isEmpty());assertTrue(local.pendientes("admin").isEmpty())
 }
 @Test fun conservaLaUltimaSincronizacionSiLaSiguienteFalla() = runTest {
  val d=UnconfinedTestDispatcher(testScheduler);val local=ProductoLocal(base(),d);var red=true;var reloj=1000L
  val repo=ProductoRepositorioOffline(local,ProductoApi(cliente(MockEngine { if(red) json(pagina()) else throw kotlinx.io.IOException("sin red") })),MutableStateFlow(sesion),d,{ reloj })
  assertNull(repo.ultimaSincronizacion().first())
  repo.sincronizar();assertEquals(1000L,repo.ultimaSincronizacion().first())
  red=false;reloj=2000
  assertFailsWith<ErrorApiException> { repo.sincronizar() }
  assertEquals(1000L,repo.ultimaSincronizacion().first())
 }
 @Test fun migracionDeVersionUnoADosConservaProductos() = runTest {
  val driver=driverEnMemoria().also(drivers::add);val base=PharmaDb(driver)
  val local=ProductoLocal(base,UnconfinedTestDispatcher(testScheduler))
  local.reemplazarSincronizados("admin",listOf(Producto(12,"Conservado",2.0,10)))
  driver.execute(null,"DROP TABLE sincronizacion",0)
  assertEquals(2L,PharmaDb.Schema.version)
  PharmaDb.Schema.migrate(driver,1,2)
  base.sincronizacionQueries.guardar("admin",1000)
  assertEquals("Conservado",local.listar("admin").single().nombre)
  assertEquals(1000L,local.ultimaSincronizacion("admin").first())
 }

}
