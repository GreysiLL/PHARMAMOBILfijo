package pe.edu.upeu.pharmamobil.data.local
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import pe.edu.upeu.pharmamobil.db.PharmaDb
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.error.*

class ProductoLocal(base: PharmaDb, private val dispatcher: CoroutineDispatcher = Dispatchers.Default) {
 private val q = base.productoQueries
 fun observar(usuario: String) = q.listarTodos(usuario).asFlow().mapToList(dispatcher).map { filas -> filas.map { it.aDominio() } }
 fun observarPendientes(usuario: String) = q.listarPendientes(usuario).asFlow().mapToList(dispatcher).map { it.size }
 fun listar(usuario: String) = q.listarTodos(usuario).executeAsList().map { it.aDominio() }
 fun pendientes(usuario: String) = q.listarPendientes(usuario).executeAsList().map { it.aDominio() }
 fun obtener(usuario: String, id: Long) = q.obtener(usuario, id).executeAsOneOrNull()?.aDominio()
     ?: throw ErrorApiException(ErrorApi.NoEncontrado)
 fun guardarPendiente(usuario: String, producto: Producto): Producto {
  var guardado = producto
  q.transaction {
   val menor = q.siguienteIdLocal(usuario).executeAsOne()
   guardado = producto.copy(id = minOf(menor, 0L) - 1L, pendiente = true)
   insertar(usuario, guardado)
  }
  return guardado
 }
 fun modificar(usuario: String, producto: Producto) { insertar(usuario, producto.copy(pendiente = true)) }
 fun restaurar(usuario: String, producto: Producto) { insertar(usuario, producto) }
 fun borrar(usuario: String, id: Long) = q.borrar(usuario, id)
 // Confirmar cada subida inmediatamente evita repetir POST si después falla la descarga.
 fun confirmar(usuario: String, idLocal: Long, remoto: Producto) { q.transaction {
  q.borrar(usuario, idLocal); insertar(usuario, remoto.copy(pendiente = false))
 } }
 fun reemplazarSincronizados(usuario: String, productos: List<Producto>) { q.transaction {
  q.borrarSincronizados(usuario)
  productos.forEach { producto ->
   if(q.obtener(usuario, producto.id).executeAsOneOrNull()?.pendiente != 1L)
       insertar(usuario, producto.copy(pendiente = false))
  }
 } }
 private fun insertar(usuario: String, p: Producto) = q.insertar(p.id, usuario, p.nombre, p.precio, p.stock.toLong(), if(p.pendiente) 1L else 0L, if(p.estado) 1L else 0L, p.categoriaId)
 private fun pe.edu.upeu.pharmamobil.db.Producto.aDominio() = Producto(id, nombre, precio, stock.toInt(), estado == 1L, categoriaId, pendiente == 1L)
}
