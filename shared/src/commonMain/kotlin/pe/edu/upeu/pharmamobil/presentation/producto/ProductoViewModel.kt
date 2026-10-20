package pe.edu.upeu.pharmamobil.presentation.producto
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import pe.edu.upeu.pharmamobil.domain.error.*
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.usecase.*
class ProductoViewModel(
 private val registrarProducto: RegistrarProductoUseCase,
 private val listarProductos: ListarProductosUseCase,
 private val obtenerProducto: ObtenerProductoUseCase,
 private val actualizarProducto: ActualizarProductoUseCase,
 private val eliminarProducto: EliminarProductoUseCase
) : ViewModel() {
 private val _uiState = MutableStateFlow(ProductoUiState())
 val uiState: StateFlow<ProductoUiState> = _uiState.asStateFlow()
 private var seleccionado: Producto? = null
 private var carga: Job? = null
 private var operacion: Job? = null
 init {
  viewModelScope.launch {
   listarProductos.observar().catch { fallo ->
    _uiState.update { it.copy(fase = ProductoUiState.Fase.Error(mensaje(fallo))) }
   }.collect { productos ->
    _uiState.update { it.copy(
     fase = if(productos.isEmpty()) ProductoUiState.Fase.SinProductos else ProductoUiState.Fase.ConProductos(productos.map { p -> p.aUi() })
    ) }
   }
  }
  viewModelScope.launch {
   listarProductos.observarPendientes().catch { }.collect { cantidad -> _uiState.update { it.copy(pendientes = cantidad) } }
  }
  sincronizar()
 }
 fun cargarProductos() = sincronizar()
 fun sincronizar() {
  if(_uiState.value.sincronizando || _uiState.value.registrando) return
  carga = viewModelScope.launch {
   _uiState.update { it.copy(sincronizando = true) }
   try {
    listarProductos.sincronizar().onSuccess {
     _uiState.update { it.copy(avisoRed = null) }
    }.onFailure { fallo ->
     val sinRed = (fallo as? ErrorApiException)?.error in setOf(ErrorApi.SinConexion, ErrorApi.TiempoAgotado)
     _uiState.update { it.copy(avisoRed = if(sinRed) "Sin conexión: mostrando datos guardados" else mensaje(fallo)) }
    }
   } finally { _uiState.update { it.copy(sincronizando = false) } }
  }
 }
 fun onNombreChange(v: String) { if(!_uiState.value.registrando) _uiState.update { it.copy(formulario=it.formulario.copy(nombre=v,nombreError=null),mensajeExito=null) } }
 fun onPrecioChange(v: String) { if(!_uiState.value.registrando) _uiState.update { it.copy(formulario=it.formulario.copy(precio=v,precioError=null),mensajeExito=null) } }
 fun onStockChange(v: String) { if(!_uiState.value.registrando) _uiState.update { it.copy(formulario=it.formulario.copy(stock=v,stockError=null),mensajeExito=null) } }
 fun cancelarEdicion() {
  if(_uiState.value.registrando) return
  seleccionado=null
  _uiState.update { it.copy(formulario=FormularioProducto(),operacion=ProductoUiState.Operacion.Inactiva,mensajeExito=null) }
 }
 fun editar(id: Long) {
  if(_uiState.value.registrando) return
  _uiState.update { it.copy(operacion=ProductoUiState.Operacion.EnCurso(ProductoUiState.Operacion.Tipo.Obtener,id),mensajeExito=null) }
  operacion = viewModelScope.launch {
   obtenerProducto(id).onSuccess { p ->
    seleccionado=p
    _uiState.update { it.copy(formulario=FormularioProducto(nombre=p.nombre,precio=p.precio.toString(),stock=p.stock.toString(),idEdicion=p.id),operacion=ProductoUiState.Operacion.Inactiva) }
   }.onFailure(::manejarFallo)
  }
 }
 fun registrar() {
  if(_uiState.value.registrando) return
  val f=_uiState.value.formulario
  val existente=seleccionado
  val tipo=if(existente==null) ProductoUiState.Operacion.Tipo.Crear else ProductoUiState.Operacion.Tipo.Actualizar
  _uiState.update { it.copy(operacion=ProductoUiState.Operacion.EnCurso(tipo,existente?.id),mensajeExito=null) }
  operacion = viewModelScope.launch {
   val result=if(existente==null) registrarProducto(f.nombre,f.precio,f.stock) else actualizarProducto(existente,f.nombre,f.precio,f.stock)
   result.onSuccess { producto ->
    seleccionado=null
    _uiState.update { it.copy(formulario=FormularioProducto(),mensajeExito=if(existente==null) "Producto \"${producto.nombre}\" registrado correctamente" else "Producto actualizado") }
    _uiState.update { it.copy(operacion=ProductoUiState.Operacion.Inactiva) }
   }.onFailure(::manejarFallo)
  }
 }
 fun eliminar(id: Long) {
  if(_uiState.value.registrando) return
  _uiState.update { it.copy(operacion=ProductoUiState.Operacion.EnCurso(ProductoUiState.Operacion.Tipo.Eliminar,id),mensajeExito=null) }
  operacion = viewModelScope.launch {
   eliminarProducto(id).onSuccess {
    if(seleccionado?.id==id) { seleccionado=null;_uiState.update { it.copy(formulario=FormularioProducto()) } }
    _uiState.update { it.copy(mensajeExito="Producto eliminado") }
    _uiState.update { it.copy(operacion=ProductoUiState.Operacion.Inactiva) }
   }.onFailure(::manejarFallo)
  }
 }
 fun cancelarOperaciones() {
  carga?.cancel()
  _uiState.update { it.copy(sincronizando = false) }
  operacion?.cancel()
  _uiState.update { it.copy(operacion=ProductoUiState.Operacion.Inactiva, fase=if(it.fase==ProductoUiState.Fase.Cargando) ProductoUiState.Fase.SinProductos else it.fase) }
 }
 private fun manejarFallo(fallo: Throwable) {
  val campos=when(fallo) {
   is ProductoInvalidoException -> mapOf("nombre" to fallo.errores.nombre,"precio" to fallo.errores.precio,"stock" to fallo.errores.stock)
   is ErrorApiException -> (fallo.error as? ErrorApi.Validacion)?.porCampo
   else -> null
  }
  if(campos!=null && campos.keys.any { it in setOf("nombre", "precio", "stock") }) _uiState.update { it.copy(operacion=ProductoUiState.Operacion.Inactiva,formulario=it.formulario.copy(nombreError=campos["nombre"],precioError=campos["precio"],stockError=campos["stock"])) }
  else _uiState.update { it.copy(operacion=ProductoUiState.Operacion.Fallida(if(campos!=null) campos.values.filterNotNull().joinToString(". ").ifBlank { mensaje(fallo) } else mensaje(fallo))) }
 }
 private fun mensaje(fallo: Throwable): String = (fallo as? ErrorApiException)?.let { mensajeDe(it.error) } ?: fallo.message ?: "No se pudo completar la operación."
}
