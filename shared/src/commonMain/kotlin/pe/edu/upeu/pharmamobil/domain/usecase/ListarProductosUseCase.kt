package pe.edu.upeu.pharmamobil.domain.usecase

import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

class ListarProductosUseCase(
    private val productoRepository: ProductoRepository
) {

    fun ultimaSincronizacion() = productoRepository.ultimaSincronizacion()
    fun observar() = productoRepository.observar()
    fun observarPendientes() = productoRepository.observarPendientes()
    fun observarFallidas() = productoRepository.observarFallidas()
    suspend fun sincronizar() = resultadoDe { productoRepository.sincronizar() }
    suspend operator fun invoke(): Result<List<Producto>> = resultadoDe {
        productoRepository.listar()
    }
}
