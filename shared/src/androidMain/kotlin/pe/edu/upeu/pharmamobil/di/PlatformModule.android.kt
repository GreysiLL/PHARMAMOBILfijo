package pe.edu.upeu.pharmamobil.di
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.core.qualifier.named
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.android.ext.koin.androidContext
import pe.edu.upeu.pharmamobil.domain.platform.Compartidor
import pe.edu.upeu.pharmamobil.platform.CompartidorAndroid
actual val platformModule: Module = module {
    single<pe.edu.upeu.pharmamobil.data.local.CreadorDeDriver> { pe.edu.upeu.pharmamobil.data.local.CreadorDeDriverAndroid(androidContext()) }
    single<pe.edu.upeu.pharmamobil.data.local.AlmacenSeguro> { pe.edu.upeu.pharmamobil.data.local.AlmacenSeguroAndroid(androidContext()) }
    single(named("depuracion")) { (androidContext().applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0 }
    single<HttpClientEngine> { OkHttp.create() }
    single(named("urlBase")) { "http://10.0.2.2:8080/api/v1/" }
    single<Compartidor> { CompartidorAndroid(androidContext()) }
}
