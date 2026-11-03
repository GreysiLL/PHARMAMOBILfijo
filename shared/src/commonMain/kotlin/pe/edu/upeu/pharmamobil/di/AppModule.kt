package pe.edu.upeu.pharmamobil.di

import io.ktor.client.HttpClient
import org.koin.core.qualifier.named
import pe.edu.upeu.pharmamobil.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobil.data.remote.ProductoApi
import pe.edu.upeu.pharmamobil.data.repository.ProductoRepositorioRest
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.koin.dsl.onClose
import pe.edu.upeu.pharmamobil.data.repository.ClienteRepositorioEnMemoria
import pe.edu.upeu.pharmamobil.data.repository.ProductoRepositorioEnMemoria
import pe.edu.upeu.pharmamobil.domain.repository.ClienteRepository
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobil.domain.usecase.ListarClientesUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ListarProductosUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarClienteUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarProductoUseCase
import pe.edu.upeu.pharmamobil.presentation.cliente.ClienteViewModel
import pe.edu.upeu.pharmamobil.presentation.producto.ProductoViewModel


val dataModule = module {
    single<app.cash.sqldelight.db.SqlDriver> { get<pe.edu.upeu.pharmamobil.data.local.CreadorDeDriver>().crear() } onClose { it?.close() }
    single { pe.edu.upeu.pharmamobil.db.PharmaDb(get()) }
    single { pe.edu.upeu.pharmamobil.data.local.ProductoLocal(get()) }
    single { pe.edu.upeu.pharmamobil.data.local.SesionLocal(get()) }
    single { pe.edu.upeu.pharmamobil.data.repository.EstadoSesion(get()) }
    single<pe.edu.upeu.pharmamobil.domain.repository.ObservadorSesion> { get<pe.edu.upeu.pharmamobil.data.repository.EstadoSesion>() }
    single<pe.edu.upeu.pharmamobil.domain.repository.AuthRepository> { pe.edu.upeu.pharmamobil.data.repository.AuthRepositorioRest(get(), get(), get(), get()) }
    single<HttpClient> { crearHttpClient(get(), get(named("urlBase")), almacen = get(), estadoSesion = get(), esDepuracion = get(named("depuracion"))) } onClose { it?.close() }
    single { ProductoApi(get()) }
    single<pe.edu.upeu.pharmamobil.domain.repository.PruebasErrores> { pe.edu.upeu.pharmamobil.data.remote.PruebasErroresRest(get(), get(named("urlBase"))) }
    single<pe.edu.upeu.pharmamobil.domain.repository.DiagnosticoConexion> { pe.edu.upeu.pharmamobil.data.remote.DiagnosticoConexionRest(get(), get(named("urlBase"))) }
    single { ProductoRepositorioEnMemoria() }
    single<ProductoRepository> { pe.edu.upeu.pharmamobil.data.repository.ProductoRepositorioOffline(get(), get(), get<pe.edu.upeu.pharmamobil.data.repository.EstadoSesion>().sesion).also { repo -> getOrNull<pe.edu.upeu.pharmamobil.domain.port.Telemetria>()?.let { repo.telemetria = it } } }
    single<ClienteRepository> { pe.edu.upeu.pharmamobil.data.repository.ClienteRepositorioRest(get()) }
}

val domainModule = module {
    factory { pe.edu.upeu.pharmamobil.domain.usecase.IniciarSesionUseCase(get()) }
    factory { pe.edu.upeu.pharmamobil.domain.usecase.CerrarSesionUseCase(get()) }
    factory { RegistrarProductoUseCase(get()) }
    factory { ListarProductosUseCase(get()) }
    factory { pe.edu.upeu.pharmamobil.domain.usecase.ObtenerProductoUseCase(get()) }
    factory { pe.edu.upeu.pharmamobil.domain.usecase.ActualizarProductoUseCase(get()) }
    factory { pe.edu.upeu.pharmamobil.domain.usecase.EliminarProductoUseCase(get()) }
    factory { RegistrarClienteUseCase(get()) }
    factory { ListarClientesUseCase(get()) }
}

val presentationModule = module {
    viewModel { pe.edu.upeu.pharmamobil.presentation.sesion.SesionViewModel(get(), get(), get(), get()) }
    viewModel { pe.edu.upeu.pharmamobil.presentation.detalle.DetalleProductoViewModel(get(), get()) }
    viewModel { ProductoViewModel(get(), get(), get(), get(), get()) }
    viewModel { ClienteViewModel(get(), get()) }
}


expect val platformModule: Module

fun initKoin(configuracionAdicional: KoinApplication.() -> Unit = {}) {
    startKoin {
        configuracionAdicional()
        modules(
            dataModule,
            domainModule,
            presentationModule,
            platformModule
        )
    }
}
