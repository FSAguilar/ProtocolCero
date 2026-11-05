# PROTOCOLO CERO — descripción y requisitos

## Evaluación rápida

El proyecto cubre los requisitos principales de modelado, inmutabilidad en la
implementación Scala, listas, procesamiento funcional, funciones de orden
superior, `case class`, pattern matching, jerarquías y genéricos.

**Hay una observación importante antes de afirmar cumplimiento total:** el
enunciado de la materia pide evitar ciclos imperativos, pero los puzzles de
energía y DJ piden escribir `while` en un pseudolenguaje. El reto DJ además
recorre la lista con un contador y lee `canales(indice)`; no asigna ni modifica
elementos de la lista por posición. El intérprete del juego no usa `while` de
Scala: parsea el texto y evalúa las instrucciones recursivamente con estado
inmutable. Si la restricción también aplica al pseudocódigo del jugador, este
punto debe confirmarse con el docente.

## Dominio

PROTOCOLO CERO es una aventura narrativa educativa ambientada en una nave de
investigación. El jugador explora cinco habitaciones, conversa con la
tripulación, toma decisiones y resuelve retos de programación para restaurar
sistemas y decidir el desenlace.

- **Problema que aborda:** aprender y practicar conceptos de programación
  funcional mediante una aventura con objetivos y retroalimentación.
- **Usuarios:** estudiantes que exploran la historia y practican los conceptos
  vistos en la materia.
- **Información administrada:** habitaciones y salidas, personajes,
  dispositivos, progreso y visitas, confianza, decisiones, pistas, inventario,
  puzzles, sistemas restaurados y final.

## Modelo de datos y relaciones

```text
ShipMap
  └── List[Room]
        └── List[Exit] ──> Direction y habitación destino

GameState
  ├── currentRoomId ──> Room
  ├── visitedRoomIds: List[String]
  ├── devices: List[Device]
  ├── characters: List[Character]
  │     ├── Engineer
  │     ├── CrewMember
  │     └── StationAI
  ├── log: List[String]
  └── StoryState
        ├── confianza
        ├── flags, inventario, decisiones y puzzles resueltos
        ├── sistemas restaurados
        └── ending: Option[Ending]

Model (estado de la aplicación)
  ├── Screen y Preferences
  ├── GameState
  └── diálogo, elecciones, panel y editor activos
```

### Archivos del modelo y reglas

| Área | Ubicación | Responsabilidad |
|---|---|---|
| Entrada y ciclo Tyrian | [`Main.scala`](src/main/scala/protocolocero/app/Main.scala) | Inicia la aplicación, actualiza el modelo y construye la vista raíz. |
| Mensajes y estado de UI | [`Message.scala`](src/main/scala/protocolocero/app/Message.scala), [`Model.scala`](src/main/scala/protocolocero/app/Model.scala), [`Screen.scala`](src/main/scala/protocolocero/domain/Screen.scala) | Tipan las acciones de UI y mantienen el estado inmutable de la aplicación. |
| Enrutamiento de acciones | [`Update.scala`](src/main/scala/protocolocero/app/Update.scala) | Recibe mensajes, llama a las reglas y produce un `Model` nuevo con `copy`. |
| Mapa y navegación | [`World.scala`](src/main/scala/protocolocero/domain/game/World.scala), [`GameRules.scala`](src/main/scala/protocolocero/domain/game/GameRules.scala) | Define habitaciones, salidas, dirección y reglas de movimiento. |
| Partida y personajes | [`GameState.scala`](src/main/scala/protocolocero/domain/game/GameState.scala), [`Characters.scala`](src/main/scala/protocolocero/domain/game/Characters.scala) | Define dispositivos, estado de juego y jerarquía de personajes. |
| Historia y decisiones | [`Story.scala`](src/main/scala/protocolocero/domain/game/Story.scala), [`StoryScenes.scala`](src/main/scala/protocolocero/domain/game/StoryScenes.scala), [`StoryRules.scala`](src/main/scala/protocolocero/domain/game/StoryRules.scala) | Modela el estado narrativo, diálogos, opciones, recompensas y finales. |
| Terminal de dispositivos | [`Instruction.scala`](src/main/scala/protocolocero/domain/game/Instruction.scala), [`ProgramCompiler.scala`](src/main/scala/protocolocero/domain/game/ProgramCompiler.scala) | Parsea y ejecuta comandos locales como `ACTIVAR VENTILADOR`. |
| Puzzles de código | [`PuzzleInterpreter.scala`](src/main/scala/protocolocero/domain/game/PuzzleInterpreter.scala) | Tokeniza, parsea y simula la DSL restringida de los puzzles sin ejecutar Scala arbitrario. |

### Jerarquías y tipos

- [`Characters.scala`](src/main/scala/protocolocero/domain/game/Characters.scala):
  `Identified` es un `trait`; `Character` es una clase abstracta; `Engineer`,
  `CrewMember` y `StationAI` son `case class` concretas.
- [`Instruction.scala`](src/main/scala/protocolocero/domain/game/Instruction.scala):
  `Instruction` y `GameResult[A]` son jerarquías `sealed trait`. Sus casos
  concretos representan comandos y resultados tipados.
- [`World.scala`](src/main/scala/protocolocero/domain/game/World.scala):
  `Direction` es una jerarquía sellada con `case object`.
- Genéricos: `GameResult[A]` en
  [`Instruction.scala`](src/main/scala/protocolocero/domain/game/Instruction.scala)
  y `executeAll[A]` en
  [`ProgramCompiler.scala`](src/main/scala/protocolocero/domain/game/ProgramCompiler.scala).

## Funcionalidades principales

1. Iniciar o reiniciar una partida y avanzar el diálogo de apertura.
2. Explorar Grooming Salon, Cocina Brookies, Malegría, Postgrado y El Ágora;
   las salidas pueden depender del progreso.
3. Inspeccionar los datos de habitación y los dispositivos locales.
4. Interactuar con personajes y objetos, recoger pistas y decidir cómo actuar.
5. Ejecutar comandos locales para activar o desactivar dispositivos.
6. Resolver el puzzle del generador y el controlador DJ; en El Ágora, resolver
   el ejercicio condicional de comunicaciones antes de elegir el final.
7. Completar opcionalmente el reto de registros en Postgrado.
8. Alcanzar uno de cuatro finales mediante confianza, progreso y elecciones.

Los diálogos y botones de habitación se definen en
[`StoryScenes.scala`](src/main/scala/protocolocero/domain/game/StoryScenes.scala);
la vista de exploración y sus editores están en
[`ExplorationVentana.scala`](src/main/scala/protocolocero/ui/screens/ExplorationVentana.scala).

Los retos de energía, DJ y comunicaciones parsean y simulan una DSL limitada.
El reto opcional de registros es distinto: solo comprueba que el texto mencione
registros y `filter`, y otorga una recompensa mayor si también aparece `sorted`;
no ejecuta ni verifica el resultado de esas transformaciones.

## Requisitos técnicos de la materia

| Requisito | Estado | Dónde se demuestra |
|---|---|---|
| Scala y tipos de dominio | Cumple | `Room`, `Exit`, `Device`, `GameState`, `StoryState` y personajes en los archivos indicados arriba. |
| Inmutabilidad / evitar `var` | Cumple en el código Scala | Los modelos son `case class`; las transiciones usan `copy` y colecciones inmutables. No se encontró declaración `var` en `src/`. |
| Listas | Cumple | Habitaciones, salidas, personajes, dispositivos, decisiones y registros se almacenan en `List`. |
| Procesamiento funcional | Cumple | `find`, `map`, `filter` y `foldLeft` aparecen en reglas, compilador y vistas. Ejemplos: `GameRules.availableDevices` y `ProgramCompiler.compile`. |
| Función de orden superior | Cumple | `ProgramCompiler.executeAll[A]` recibe `step` como función y lo aplica en `foldLeft`. |
| `case class` y pattern matching | Cumple | Instrucciones, resultados, estado, entidades; `match` en `Update`, reglas y compiladores. |
| Jerarquías `trait` / `sealed trait` / clase abstracta | Cumple | Personajes, instrucciones, resultados y direcciones. |
| Genéricos | Cumple | `GameResult[A]` y `executeAll[A]`. |
| Funcionalidades, dominio y modelo documentados | Cumple | Esta guía, junto con [`README.md`](README.md) y [`req.md`](req.md). |
| Evitar ciclos imperativos y modificaciones con índices | **Revisar con el docente** | La aplicación Scala no usa ciclos imperativos ni muta listas por índice. La DSL del jugador sí usa `while` y acceso de lectura `canales(indice)` con un contador en el reto DJ. |
| No almacenar todo en `Any` | Cumple | El estado y los resultados usan tipos concretos o genéricos tipados; no se usa `Any` como modelo global. |

### Por qué hay una DSL de `while`

El jugador no ejecuta Scala. El intérprete convierte texto a tokens y luego a
instrucciones tipadas (`Assign`, `WhileLoop`, `IfThenElse`). La ejecución está
limitada por cantidad de pasos, iteraciones y profundidad; cada transición
produce un estado nuevo. Esto permite validar los puzzles de forma controlada,
pero no elimina la posible discrepancia académica si la indicación de evitar
`while` también se aplica al pseudocódigo del jugador.

## Ejemplos de ejecución

Desde la raíz del proyecto:

```bash
sbt test
sbt fastLinkJS
python3 -m http.server 8000
```

Abre `http://localhost:8000` después de compilar. El módulo JavaScript se genera
bajo `target/scala-3.8.4/protocolo-cero-fastopt/`; `target/` es un artefacto
generado y no debe versionarse.

Ejemplos de pseudocódigo aceptado por los puzzles:

```text
# Generador: la energía debe llegar exactamente a 100
while (energia < 100) {
  energia = energia + 20
}
```

```text
# DJ: recorrer los canales y acumular solo los positivos
while (indice < canales.length) {
  if (canales(indice) > 0) {
    volumen = volumen + canales(indice)
  }
  indice = indice + 1
}
```

Los tests automatizados están en [`src/test/scala/protocolocero/app/`](src/test/scala/protocolocero/app/):
`GameRulesSuite.scala`, `UpdateSuite.scala` y `StoryGameSuite.scala`.
