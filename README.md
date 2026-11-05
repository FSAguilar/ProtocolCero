# PROTOCOLO CERO

Videojuego educativo y narrativo en desarrollo para la materia Programación Funcional. Incluye menú, exploración de ubicaciones, diálogos, decisiones y puzzles de código.

## Tecnologías

- Scala 3.8.4
- Scala.js 1.22.0
- Tyrian 0.30.0-M6 (API moderna `App`, no Tyrian Classic)
- sbt 1.10.7
- MUnit 1.0.2 para pruebas

## Estructura

```text
src/main/scala/protocolocero/
  app/       Estado, mensajes, actualización y punto de entrada
  domain/
    game/    Mapa de ubicaciones, personajes, estado y mini compilador
             implementados con colecciones inmutables
    Screen.scala  Pantallas y preferencias
  ui/screens/
    MainVentanas.scala           Composición y selección de la ventana activa
    MenuVentana.scala            Menú principal
    ExplorationVentana.scala     Vista de exploración, mapa, diálogos y puzzles
    OpcionesVentana.scala        Preferencias visuales
    CreditosVentana.scala        Autores del proyecto
src/test/scala/  Pruebas de transiciones puras
req.md           Requisitos académicos y su ubicación en el código
ENTREGA.md       Descripción del dominio, modelo de datos y auditoría del enunciado
styles/          Estilos de la interfaz
project/         Configuración de sbt y Scala.js
index.html       Documento anfitrión
```

La lógica de actualización y el dominio del juego son funcionales e inmutables. La aventura sigue cinco ubicaciones: Grooming Salon, Cocina Brookies, Malegría, Postgrado y El Ágora. Cada sala tiene tres interacciones narrativas principales; los diálogos avanzan con clic, las decisiones alteran la confianza en Tessa y el inventario registra pistas. Un HUD muestra confianza, sistemas restaurados y objetos encontrados. El botón `D` abre los datos de la sala; los editores de código aparecen al interactuar con la computadora aislada, el controlador DJ o el registro opcional, no como una terminal global.

La historia incluye el protocolo del generador, el controlador DJ, la revisión opcional de archivos funcionales, una reparación de comunicaciones antes de la decisión final, las decisiones sobre los brookies y los archivos clasificados, la confesión del prompt y cuatro finales: Tessa aliada, apagado de Tessa, Hora Libre y Sprint Infinito. En energía y DJ, el jugador escribe el `while` completo; la inicialización y la comprobación final aparecen fijas. El parser convierte el pseudocódigo en instrucciones `case class` y el intérprete las ejecuta mediante pattern matching. Admite `while`, `if/else`, acceso a listas, comparaciones, asignaciones aritméticas y valores inmutables, con límites de tamaño y pasos. No ejecuta Scala arbitrario ni comandos del sistema.

Los diálogos también explican conceptos necesarios para resolver los ejercicios: repetición de `while`, condiciones con `if/else`, acumuladores, índices de listas y `filter` con predicados. Los editores muestran instrucciones, no soluciones copiables en placeholders.

El mapa actual se dibuja con `assets/ship-map.svg` y refleja las conexiones implementadas. Se usan retratos de `assets/personajes/` durante los diálogos y las ilustraciones de `assets/finales/` en los desenlaces. `assets/interfaz/menu_principal.jpg` contiene texto y una llamada a comenzar dentro de la ilustración, así que no se combina con el menú funcional para evitar duplicar títulos y acciones.

## Recorrido de la demo narrativa

1. En Grooming Salon, avanza el diálogo inicial. Examina la computadora y completa el protocolo de energía para llegar a Cocina Brookies. Revisa el escritorio para recoger la factura de AWS y la nota del prompt.
2. En la cocina, habla con Gaby, decide qué brookies aceptar y revisa la puerta opcional. La decisión de brookies abre el camino a Malegría.
3. En Malegría, habla con Dani, revisa el controlador DJ y la zona de descanso. El puzzle de sonido abre Postgrado; encontrar el conejo permite descubrir el registro de Manizeh.
4. En Postgrado, interactúa con Chelito y Alexis-Jesús, decide si leer los archivos y, opcionalmente, filtra los registros para conservar la entrada MANIZEH. Si encontraste la nota del prompt, puedes confesarlo aquí.
5. En El Ágora, habla con Pastor y Tessa antes de abrir el panel. Liberar a Tessa requiere confianza de al menos 7. La opción Hora Libre aparece con prompt confesado, archivos leídos y confianza exactamente 10.

La confianza comienza en 5 y siempre queda entre 0 y 10. Volver a tomar una decisión ya registrada no vuelve a aplicar sus efectos. Las elecciones son locales a la partida actual; guardado/carga persistente todavía no está implementado.

## Requisitos

- JDK 17 o posterior
- sbt 1.10.7
- Navegador moderno

## Ejecutar

Desde la raíz del proyecto:

```bash
sbt test
sbt fastLinkJS
```

Después de compilar, sirve la raíz del proyecto por HTTP y abre `index.html`. Por ejemplo, con Python instalado:

```bash
python3 -m http.server 8000
```

Abre <http://localhost:8000>. La aplicación carga el módulo compilado desde `target/scala-3.8.4/protocolo-cero-fastopt/main.js`.

## Estado de implementación

- Menú principal, atmósfera visual adaptable y navegación Tyrian: implementados.
- Nueva partida: inicia con la escena de despertar en Grooming Salon.
- Exploración: muestra el plano, navegación con bloqueos de progreso y los fondos de cada ubicación.
- Interacciones: tres acciones principales por sala; diálogo con retrato, decisiones narrativas, confianza e inventario.
- Puzzles: generador, controlador DJ, ejercicio condicional de comunicaciones y reto opcional de registros (valida el filtro esperado, pero no ejecuta la transformación).
- Finales: aliados, apagado, Hora Libre y Sprint Infinito.
- Continuar: deshabilitado, porque aún no existe ninguna partida guardada.
- Opciones: preferencia real de reducir animaciones, guardada en el estado de la aplicación en memoria.
- Créditos: muestra los autores indicados.
- Guardado/carga persistente, combate y misiones adicionales: no implementados.

La guía [`ENTREGA.md`](ENTREGA.md) relaciona cada requisito con sus archivos y señala una discrepancia académica a confirmar: el enunciado pide evitar `while` y cambios con índices, mientras los retos de energía y DJ enseñan esos conceptos en una DSL restringida.
# ProtocolCero
