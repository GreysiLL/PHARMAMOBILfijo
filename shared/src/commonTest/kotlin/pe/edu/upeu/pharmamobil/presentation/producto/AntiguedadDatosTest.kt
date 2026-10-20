package pe.edu.upeu.pharmamobil.presentation.producto
import kotlin.test.*
class AntiguedadDatosTest {
 @Test fun nuncaSincronizadoEsExplicito() { assertEquals("Nunca sincronizado",antiguedadDatos(null,1000)) }
 @Test fun recientesMinutosYHoras() {
  assertEquals("Actualizado hace un momento",antiguedadDatos(1000,1000))
  assertEquals("Actualizado hace 1 minuto",antiguedadDatos(1000,61_000))
  assertEquals("Actualizado hace 2 horas",antiguedadDatos(1000,7_201_000))
 }
 @Test fun masDeUnDiaYUnaFechaFutura() {
  assertEquals("Actualizado hace 2 días",antiguedadDatos(1000,172_801_000))
  assertEquals("Actualizado hace un momento",antiguedadDatos(5000,1000))
 }
}
