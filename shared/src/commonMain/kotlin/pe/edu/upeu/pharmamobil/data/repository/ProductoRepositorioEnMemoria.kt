package pe.edu.upeu.pharmamobil.data.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

/**
 * Almacenamiento en memoria de productos. El id lo asigna el repositorio,
 * no la pantalla, para evitar identificadores duplicados.
 *
 * El delay simula la latencia que traera el backend REST, de modo que el
 * estado de carga de la pantalla sea visible desde ahora.
 *
 * Koin lo registra como single, asi que es un objeto compartido y sus metodos
 * son suspend: nada garantiza que dos llamadas no se crucen. El [Mutex]
 * protege la lista y el contador de ids de esa carrera.
 */
class ProductoRepositorioEnMemoria : ProductoRepository {

    private val candado = Mutex()
    private val productos = mutableListOf<Producto>()
    private val cambios = kotlinx.coroutines.flow.MutableStateFlow(productos.filter { it.estado })
    override fun observar(): kotlinx.coroutines.flow.Flow<List<Producto>> = cambios
    private var siguienteId = 1L

    override suspend fun registrar(producto: Producto): Producto {
        delay(RETARDO_REGISTRO_MS)
        return candado.withLock {
            val guardado = producto.copy(id = siguienteId++)
            productos.add(guardado)
        cambios.value = productos.filter { it.estado }
            guardado
        }
    }

    override suspend fun listar(): List<Producto> {
        delay(RETARDO_LISTADO_MS)
        return candado.withLock {
            productos.filter { it.estado }
        }
    }

    override suspend fun obtener(id: Long): Producto = candado.withLock {
        productos.firstOrNull { it.id == id } ?: throw pe.edu.upeu.pharmamobil.domain.error.ErrorApiException(pe.edu.upeu.pharmamobil.domain.error.ErrorApi.NoEncontrado)
    }
    override suspend fun actualizar(producto: Producto): Producto = candado.withLock {
        val index = productos.indexOfFirst { it.id == producto.id }
        if (index < 0) throw pe.edu.upeu.pharmamobil.domain.error.ErrorApiException(pe.edu.upeu.pharmamobil.domain.error.ErrorApi.NoEncontrado)
        productos[index] = producto
        cambios.value = productos.filter { it.estado }
        producto
    }
    override suspend fun eliminar(id: Long) { candado.withLock {
        val index = productos.indexOfFirst { it.id == id }
        if (index < 0) throw pe.edu.upeu.pharmamobil.domain.error.ErrorApiException(pe.edu.upeu.pharmamobil.domain.error.ErrorApi.NoEncontrado)
        if (!productos[index].estado) throw pe.edu.upeu.pharmamobil.domain.error.ErrorApiException(pe.edu.upeu.pharmamobil.domain.error.ErrorApi.Conflicto("El producto ya está inactivo."))
        productos[index] = productos[index].copy(estado = false)
        cambios.value = productos.filter { it.estado }
    } }
    private companion object {
        const val RETARDO_REGISTRO_MS = 400L
        const val RETARDO_LISTADO_MS = 600L
    }
}
