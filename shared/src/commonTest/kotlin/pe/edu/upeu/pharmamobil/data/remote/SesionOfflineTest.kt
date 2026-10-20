package pe.edu.upeu.pharmamobil.data.remote

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobil.data.local.*
import pe.edu.upeu.pharmamobil.data.repository.*
import pe.edu.upeu.pharmamobil.db.PharmaDb
import pe.edu.upeu.pharmamobil.domain.model.*
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.time.Clock
import kotlin.test.*

@OptIn(ExperimentalEncodingApi::class)
class SesionOfflineTest {
 private class Almacen(var valor: String?) : AlmacenSeguro {
  override fun leerToken() = valor
  override fun guardarToken(token: String) { valor = token }
  override fun borrarToken() { valor = null }
 }
 private fun token(exp: Long): String {
  val payload = """{"sub":"admin","exp":$exp}""".encodeToByteArray()
  return "cabecera.${Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT).encode(payload)}.firma"
 }
 @Test fun restauraUnaSesionPreviamenteValidadaAlAbrirSinRed() = runTest {
  val driver = driverEnMemoria()
  val cache = SesionLocal(PharmaDb(driver))
  val jwt = token(Clock.System.now().epochSeconds + 3600)
  val original = Sesion(jwt,"admin","Administrador",setOf("CATALOGO_LEER"))
  cache.guardar(original)
  val store = Almacen(jwt);val estado = EstadoSesion(store)
  val client = crearHttpClient(MockEngine { throw kotlinx.io.IOException("backend apagado") },"http://localhost/api/v1/",almacen=store,estadoSesion=estado,esDepuracion=false)
  try {
   assertEquals(original,AuthRepositorioRest(client,store,estado,cache).sesionGuardada())
   assertEquals(original,estado.sesion.value)
  } finally { client.close();driver.close() }
 }
 @Test fun noRecuperaUnTokenVencidoNiUnaSesionQueNuncaFueValidada() {
  val driver=driverEnMemoria()
  try {
   val cache=SesionLocal(PharmaDb(driver))
   assertNull(cache.recuperar(token(Clock.System.now().epochSeconds+3600)))
   val vencido=token(1)
   cache.guardar(Sesion(vencido,"admin","Admin",setOf("CATALOGO_LEER")))
   assertNull(cache.recuperar(vencido))
   assertNull(cache.recuperar("no-es-jwt"))
  } finally { driver.close() }
 }
 @Test fun un401DelServidorNuncaSeOcultaConLaSesionLocal() = runTest {
  val driver=driverEnMemoria();val cache=SesionLocal(PharmaDb(driver))
  val jwt=token(Clock.System.now().epochSeconds+3600)
  cache.guardar(Sesion(jwt,"admin","Admin",setOf("CATALOGO_LEER")))
  val store=Almacen(jwt);val estado=EstadoSesion(store)
  val client=crearHttpClient(MockEngine { respond("{}",HttpStatusCode.Unauthorized,headersOf(HttpHeaders.ContentType,"application/json")) },"http://localhost/api/v1/",almacen=store,estadoSesion=estado,esDepuracion=false)
  try {
   assertNull(AuthRepositorioRest(client,store,estado,cache).sesionGuardada())
   assertNull(estado.sesion.value);assertNull(store.valor)
  } finally { client.close();driver.close() }
 }
 @Test fun tokenVencidoSinRedAbreSoloLecturaYNoPermiteEscritura() = runTest {
  val driver=driverEnMemoria();val cache=SesionLocal(PharmaDb(driver));val jwt=token(1)
  cache.guardar(Sesion(jwt,"admin","Admin",setOf("CATALOGO_LEER","CATALOGO_ESCRIBIR","CLIENTES_LEER")))
  val store=Almacen(jwt);val estado=EstadoSesion(store)
  val client=crearHttpClient(MockEngine { throw kotlinx.io.IOException("sin red") },"http://localhost/api/v1/",almacen=store,estadoSesion=estado,esDepuracion=false)
  try {
   val sesion=assertNotNull(AuthRepositorioRest(client,store,estado,cache).sesionGuardada())
   assertTrue(sesion.soloLecturaLocal);assertTrue(sesion.puede("CATALOGO_LEER"))
   assertFalse(sesion.puede("CATALOGO_ESCRIBIR"));assertFalse(sesion.puede("CLIENTES_LEER"))
  } finally { client.close();driver.close() }
 }

}
