package pe.edu.upeu.pharmamobil.domain.repository
import kotlinx.coroutines.flow.collect
import pe.edu.upeu.pharmamobil.domain.model.Producto
interface ProductoRepository {
 fun observar(): kotlinx.coroutines.flow.Flow<List<Producto>> = kotlinx.coroutines.flow.flow { emit(listar()) }
 suspend fun sincronizar() {}
 fun observarPendientes(): kotlinx.coroutines.flow.Flow<Int> = kotlinx.coroutines.flow.flow { observar().collect { emit(it.count { p -> p.pendiente }) } }
 // Se conserva para los casos de uso anteriores; la pantalla utiliza observar().
 suspend fun listar(): List<Producto>
 suspend fun obtener(id: Long): Producto
 suspend fun registrar(producto: Producto): Producto
 suspend fun actualizar(producto: Producto): Producto
 suspend fun eliminar(id: Long)
}
