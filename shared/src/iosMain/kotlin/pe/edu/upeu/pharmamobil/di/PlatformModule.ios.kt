package pe.edu.upeu.pharmamobil.di
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.qualifier.named
import org.koin.core.module.Module
import org.koin.dsl.module
import pe.edu.upeu.pharmamobil.domain.platform.Compartidor
import pe.edu.upeu.pharmamobil.platform.CompartidorIos
actual val platformModule: Module = module {
    single<pe.edu.upeu.pharmamobil.data.local.CreadorDeDriver> { pe.edu.upeu.pharmamobil.data.local.CreadorDeDriverIos() }
    single<pe.edu.upeu.pharmamobil.data.local.AlmacenSeguro> { pe.edu.upeu.pharmamobil.data.local.AlmacenSeguroIos() }
    single(named("depuracion")) { kotlin.native.Platform.isDebugBinary }
    single<HttpClientEngine> { Darwin.create() }
    single(named("urlBase")) { "http://localhost:8080/api/v1/" }
    single<Compartidor> { CompartidorIos() }
}
