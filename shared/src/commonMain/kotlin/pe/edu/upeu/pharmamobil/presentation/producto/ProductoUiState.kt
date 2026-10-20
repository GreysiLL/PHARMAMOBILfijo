package pe.edu.upeu.pharmamobil.presentation.producto
data class ProductoUiState(
 val fase: Fase = Fase.Cargando,
 val formulario: FormularioProducto = FormularioProducto(),
 val operacion: Operacion = Operacion.Inactiva,
 val mensajeExito: String? = null,
 val sincronizando: Boolean = false,
 val avisoRed: String? = null,
 val pendientes: Int = 0
) {
 val registrando: Boolean get() = operacion is Operacion.EnCurso
 sealed interface Fase {
  data object Cargando : Fase
  data object SinProductos : Fase
  data class ConProductos(val productos: List<ProductoUi>) : Fase
  data class Error(val mensaje: String) : Fase
 }
 sealed interface Operacion {
  data object Inactiva : Operacion
  data class EnCurso(val tipo: Tipo, val id: Long? = null) : Operacion
  data class Fallida(val mensaje: String) : Operacion
  enum class Tipo { Crear, Actualizar, Eliminar, Obtener }
 }
}
data class FormularioProducto(
 val nombre: String = "", val precio: String = "", val stock: String = "",
 val nombreError: String? = null, val precioError: String? = null, val stockError: String? = null,
 val idEdicion: Long? = null
)
