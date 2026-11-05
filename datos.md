Proyecto Final — Programación Funcional con Scala
Descripción general
Como proyecto final de la asignatura, cada estudiante o equipo deberá desarrollar un mini sistema aplicando los principales conceptos estudiados durante el curso de Programación Funcional con Scala.

El objetivo del proyecto es diseñar una solución pequeña pero completa donde los datos sean modelados correctamente y procesados utilizando principios de programación funcional.

Cada equipo podrá elegir libremente el dominio o temática de su sistema.

Algunas ideas de dominios posibles:

Sistema de biblioteca.
Sistema de ventas.
Sistema de gestión deportiva.
Sistema de reservas.
Sistema hospitalario.
Sistema educativo.
Sistema de videojuegos.
Sistema de administración de restaurantes.
Sistema de transporte.
Sistema de inventario.
Estas ideas son solamente ejemplos. El dominio final puede ser elegido por cada equipo.

Objetivo del proyecto
Desarrollar un mini sistema que permita:

Modelar información mediante tipos adecuados.
Representar datos utilizando estructuras inmutables.
Crear funciones que procesen información.
Aplicar programación funcional combinada con orientación a objetos.
Utilizar abstracciones para evitar repetir lógica.
El proyecto no busca desarrollar una aplicación con interfaz gráfica o conexión a una base de datos.

El enfoque principal es el diseño del modelo y el procesamiento funcional de datos.

Requisitos generales
El proyecto debe cumplir obligatoriamente con los siguientes puntos.

1. Uso de clases y objetos
El sistema debe contener:

Clases o case classes para representar entidades del dominio.
Métodos asociados cuando corresponda.
Uso adecuado de encapsulamiento.
Ejemplo:

Un sistema de biblioteca podría tener:

Libro.
Usuario.
Préstamo.
2. Uso de datos inmutables
El proyecto debe seguir principios de inmutabilidad.

Debe:

Utilizar principalmente val.
Evitar modificar objetos existentes.
Crear nuevos valores como resultado de las operaciones.
Ejemplo:

En lugar de modificar un préstamo existente:

Préstamo antiguo
        ↓
Nueva operación
        ↓
Nuevo préstamo actualizado
3. Uso obligatorio de listas
El proyecto debe utilizar al menos una lista para almacenar información.

Ejemplos:

Lista de productos
Lista de usuarios
Lista de reservas
Lista de transacciones
Lista de registros
Las listas deberán ser procesadas mediante funciones del lenguaje.

4. Procesamiento funcional de datos
El sistema debe incluir funciones que procesen información.

Se espera el uso de operaciones como:

map
filter
flatMap
foldLeft
reduce
según corresponda al problema.

Ejemplos de funcionalidades:

Obtener totales.
Buscar elementos.
Transformar información.
Filtrar datos.
Generar estadísticas.
5. Uso de funciones de orden superior
El proyecto debe incluir al menos una función que:

Reciba otra función como parámetro.
Ejemplo de idea:

Una función que permita aplicar diferentes criterios de búsqueda o cálculo.

6. Uso de case classes y pattern matching
El proyecto debe utilizar:

case class.
case object cuando sea necesario.
Pattern matching mediante match.
Ejemplos:

Estados de un proceso.
Categorías.
Tipos de elementos.
Resultados de operaciones.
Ejemplo:

Un pedido puede tener estados:

Pendiente
Enviado
Entregado
Cancelado
7. Uso de jerarquías de tipos
El proyecto debe incluir al menos una jerarquía de tipos utilizando:

trait.
sealed trait.
Clases abstractas.
Ejemplo:

Un sistema de pagos:

MetodoPago

 ├── Tarjeta

 ├── Transferencia

 └── Efectivo
8. Uso de genéricos
El proyecto debe incluir al menos un componente genérico.

Ejemplos:

Una clase que pueda trabajar con diferentes tipos de datos.
Una estructura reutilizable.
Una función genérica.
Funcionalidades del sistema
Antes de comenzar la implementación, cada equipo deberá definir claramente qué puede realizar su sistema.

El documento del proyecto deberá incluir una sección:

Funcionalidades principales
Donde se describan las operaciones disponibles.

Ejemplo:

Sistema de biblioteca
El sistema permitirá:

Registrar libros.
Buscar libros por categoría.
Obtener cantidad de libros disponibles.
Calcular estadísticas de préstamos.
Cambiar el estado de un préstamo.
Diseño inicial requerido
Antes de programar, cada equipo deberá presentar:

1. Descripción del dominio
Explicar:

¿Qué problema resuelve el sistema?
¿Quién utilizaría este sistema?
¿Qué información administra?
2. Modelo de datos
Describir:

Clases principales.
Relaciones entre clases.
Jerarquías existentes.
Datos almacenados.
Ejemplo:

Usuario

    |
    |
Préstamo

    |
    |
Libro
3. Lista de funcionalidades
Cada funcionalidad debe describirse como una acción del sistema.

Ejemplos:

Calcular total de ventas.
Encontrar el producto más vendido.
Obtener pacientes pendientes.
Generar resumen de reservas.
Restricciones técnicas
El proyecto debe:

Estar desarrollado en Scala.
Utilizar programación funcional.
Evitar variables mutables.
Evitar ciclos imperativos (while, modificaciones con índices).
Evitar soluciones que solamente utilicen orientación a objetos tradicional.
Evitar almacenar toda la información en tipos genéricos como Any.
Evaluación del proyecto
Se considerará:

Diseño del modelo
Correcta representación del dominio.
Uso adecuado de tipos.
Buena separación de responsabilidades.
Programación funcional
Uso correcto de inmutabilidad.
Funciones puras.
Procesamiento mediante colecciones.
Uso de Scala
Uso adecuado de case class.
Pattern matching.
Genéricos.
Jerarquías de tipos.
Calidad del código
Claridad.
Nombres adecuados.
Organización.
Evitar repetición innecesaria.
Entrega final
El proyecto deberá incluir:

Código fuente completo.
Documento con descripción del sistema.
Explicación de las principales decisiones de diseño.
Ejemplos de ejecución.
Recomendación final
El objetivo no es construir el sistema más grande posible.

Un sistema pequeño pero bien diseñado, donde se utilicen correctamente los conceptos de programación funcional, tendrá mayor valor que un sistema grande con poca estructura.

La pregunta principal durante el desarrollo debe ser:

¿Cómo puedo representar estos datos y transformarlos utilizando funciones puras e inmutabilidad?