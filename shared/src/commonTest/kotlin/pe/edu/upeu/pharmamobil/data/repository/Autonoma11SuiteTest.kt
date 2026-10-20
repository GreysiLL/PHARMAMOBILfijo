package pe.edu.upeu.pharmamobil.data.repository
import app.cash.sqldelight.db.SqlDriver
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import pe.edu.upeu.pharmamobil.data.local.*
import pe.edu.upeu.pharmamobil.data.remote.*
import pe.edu.upeu.pharmamobil.data.mapper.toDomain
import pe.edu.upeu.pharmamobil.db.PharmaDb
import pe.edu.upeu.pharmamobil.domain.model.*
import kotlin.test.*

/** Seis casos de la guía. Cada fixture crea su propio driver; MockEngine no abre la red. */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class Autonoma11SuiteTest {
 private val drivers=mutableListOf<SqlDriver>();private val clientes=mutableListOf<HttpClient>()
 private val sesion=MutableStateFlow<Sesion?>(Sesion("prueba","admin","Admin",setOf("CATALOGO_LEER","CATALOGO_ESCRIBIR")))
 private fun producto(id:Long=10,nombre:String="Servidor")="""{"id":$id,"nombre":"$nombre","precio":2.5,"stock":12,"estado":true,"categoriaId":1}"""
 private fun pagina(filas:String,ultima:Boolean=true)="""{"contenido":[$filas],"pagina":0,"tamanio":100,"totalElementos":2,"totalPaginas":2,"ultima":$ultima}"""
 private fun MockRequestHandleScope.json(cuerpo:String,status:HttpStatusCode=HttpStatusCode.OK)=respond(cuerpo,status,headersOf(HttpHeaders.ContentType,"application/json"))
 private fun fixture(scope:TestScope,engine:MockEngine):Triple<ProductoRepositorioOffline,ProductoLocal,ProductoApi> {
  val driver=driverEnMemoria().also(drivers::add);val local=ProductoLocal(PharmaDb(driver),UnconfinedTestDispatcher(scope.testScheduler))
  val cliente=crearHttpClient(engine,"http://localhost/api/v1/",esDepuracion=false).also(clientes::add)
  val api=ProductoApi(cliente)
  return Triple(ProductoRepositorioOffline(local,api,sesion,UnconfinedTestDispatcher(scope.testScheduler)),local,api)
 }
 @AfterTest fun cerrar() { clientes.forEach { it.close() };drivers.forEach { it.close() } }

 @Test fun T02_registroSinRedQuedaPendienteYEmiteElFlow()=runTest {
  val (repo,local)=fixture(this,MockEngine { throw kotlinx.io.IOException("sin red") })
  val p=repo.registrar(Producto(0,"Producto offline",3.0,9))
  assertTrue(p.id<0);assertTrue(p.pendiente)
  assertEquals(p,local.pendientes("admin").single());assertEquals(listOf(p),repo.observar().first())
 }
 @Test fun T04_laBajadaConservaLoPendiente()=runTest {
  val (repo,local,api)=fixture(this,MockEngine { json(pagina(producto())) })
  val pendiente=local.guardarPendiente("admin",Producto(0,"No perder",3.0,9))
  // Aísla la fase de descarga: sincronizar() no baja si antes falla una subida.
  val descargados=api.listar().contenido.map { it.toDomain() }
  local.reemplazarSincronizados("admin",descargados)
  val visibles=repo.observar().first()
  assertEquals(2,visibles.size);assertEquals(pendiente,visibles.single { it.id<0 })
  assertTrue(local.pendientes("admin").single().pendiente)
 }
 @Test fun T01_sincronizacionConRedEmiteTodasLasPaginas()=runTest {
  val (repo,local)=fixture(this,MockEngine { req ->
   if(req.url.parameters["pagina"]=="0") json(pagina(producto(10),false))
   else json(pagina(producto(11,"Segunda página")))
  })
  repo.sincronizar()
  assertEquals(setOf(10L,11L),repo.observar().first().map { it.id }.toSet())
  assertEquals(2,local.listar("admin").size);assertTrue(local.pendientes("admin").isEmpty())
 }
 @Test fun T03_reconexionEnviaYPoneLaMarcaEnCero()=runTest {
  var red=false;val metodos=mutableListOf<HttpMethod>()
  val (repo,local)=fixture(this,MockEngine { req ->
   if(!red) throw kotlinx.io.IOException("sin red")
   metodos.add(req.method)
   if(req.method==HttpMethod.Post) json(producto(70,"Nuevo offline"),HttpStatusCode.Created)
   else json(pagina(producto(70,"Nuevo offline")))
  })
  repo.registrar(Producto(0,"Nuevo offline",2.5,12));red=true;repo.sincronizar()
  assertEquals(listOf(HttpMethod.Post,HttpMethod.Get),metodos)
  assertTrue(local.pendientes("admin").isEmpty())
  assertEquals(70L,repo.observar().first().single().id);assertFalse(repo.observar().first().single().pendiente)
 }
 @Test fun T05_identificadoresLocalesYRemotosNoColisionan()=runTest {
  val (repo,local)=fixture(this,MockEngine { json(pagina(producto(1))) })
  local.reemplazarSincronizados("admin",listOf(Producto(1,"Servidor",2.5,12)))
  val nuevo=local.guardarPendiente("admin",Producto(0,"Local",3.0,9))
  assertEquals(-1L,nuevo.id)
  assertEquals(setOf(-1L,1L),repo.observar().first().map { it.id }.toSet())
  assertEquals("Servidor",local.obtener("admin",1).nombre)
 }
 @Test fun T06_sincronizarDosVecesNoDuplicaFilasNiPost()=runTest {
  var posts=0
  val (repo,local)=fixture(this,MockEngine { req ->
   if(req.method==HttpMethod.Post) { posts++;json(producto(22),HttpStatusCode.Created) }
   else json(pagina(producto(22)))
  })
  local.guardarPendiente("admin",Producto(0,"Servidor",2.5,12))
  repo.sincronizar();val primera=repo.observar().first()
  repo.sincronizar();val segunda=repo.observar().first()
  assertEquals(primera,segunda);assertEquals(1,segunda.size);assertEquals(1,posts)
 }
}
