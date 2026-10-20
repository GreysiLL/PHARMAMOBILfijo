package pe.edu.upeu.pharmamobil.data.local
import pe.edu.upeu.pharmamobil.db.PharmaDb
import pe.edu.upeu.pharmamobil.domain.model.Sesion
import kotlinx.serialization.json.*
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.time.Clock

class SesionLocal(base: PharmaDb) {
 private val q = base.sesionGuardadaQueries
 fun guardar(s: Sesion) = q.guardar(s.usuario, s.nombreCompleto, JsonArray(s.permisos.map(::JsonPrimitive)).toString())
 // Solo restaura metadatos de una sesión validada previamente. El permiso explícito
 // para una sesión vencida habilita lectura local; no valida la firma ni renueva el JWT.
 @OptIn(ExperimentalEncodingApi::class)
 fun recuperar(token: String, permitirVencidaSoloLectura: Boolean = false): Sesion? = try {
  val partes = token.split('.')
  require(partes.size == 3)
  val contenido = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT_OPTIONAL).decode(partes[1]).decodeToString()
  val claims = Json.parseToJsonElement(contenido).jsonObject
  val usuario = claims.getValue("sub").jsonPrimitive.content
  val vence = claims.getValue("exp").jsonPrimitive.long
  val vencida = vence <= Clock.System.now().epochSeconds
  if(vencida && !permitirVencidaSoloLectura) null else q.obtener(usuario).executeAsOneOrNull()?.let {
   Sesion(token, it.usuario, it.nombre, Json.parseToJsonElement(it.permisos).jsonArray.map { p -> p.jsonPrimitive.content }.toSet(), soloLecturaLocal = vencida)
  }
 } catch(_: Exception) { null }
}
