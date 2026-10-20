package pe.edu.upeu.pharmamobil.data.repository
import kotlinx.coroutines.flow.collect

import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

/**
 * Doble del inventario para las pruebas: sin delay y capaz de fallar a
 * voluntad. Sin el, probar el camino de error del caso de uso o del ViewModel
 * era imposible, porque el repositorio en memoria nunca falla.
 */
class FakeProductoRepository(
    private val productos: MutableList<Producto> = mutableListOf()
) : ProductoRepository {

    var fallaAlRegistrar: Throwable? = null
    var fallaAlListar: Throwable? = null

    private val cambios = kotlinx.coroutines.flow.MutableStateFlow(productos.filter { it.estado })
    override fun observar(): kotlinx.coroutines.flow.Flow<List<Producto>> = kotlinx.coroutines.flow.flow {
        fallaAlListar?.let { throw it }
        cambios.collect { emit(it) }
    }
    private var siguienteId = (productos.maxOfOrNull { it.id } ?: 0L) + 1L

    override suspend fun registrar(producto: Producto): Producto {

        fallaAlRegistrar?.let { throw it }

        val guardado = producto.copy(id = siguienteId++)
        productos.add(guardado)
        cambios.value = productos.filter { it.estado }
        return guardado
    }

    override suspend fun listar(): List<Producto> {

        fallaAlListar?.let { throw it }

        return productos.filter { it.estado }
    }
    override suspend fun obtener(id: Long) = productos.first { it.id == id }
    override suspend fun actualizar(producto: Producto): Producto { val i=productos.indexOfFirst { it.id==producto.id };check(i>=0);productos[i]=producto;cambios.value=productos.filter { it.estado };return producto }
    override suspend fun eliminar(id: Long) { val i=productos.indexOfFirst { it.id==id };check(i>=0);productos[i]=productos[i].copy(estado=false);cambios.value=productos.filter { it.estado } }
}
