# Requisitos técnicos en PROTOCOLO CERO

La implementación Scala utiliza estado y colecciones inmutables y no declara variables mutables (`var`). Nota académica: el pseudolenguaje de los puzzles acepta `while` y, en el reto DJ, un contador para leer `canales(indice)`. No modifica elementos de la lista por índice. El enunciado de la materia pide evitar ciclos imperativos y puede que también restrinja esa sintaxis dentro de la DSL; ver [`ENTREGA.md`](ENTREGA.md).

| Requisito | Ubicación | Uso dentro del juego |
|---|---|---|
| Clases y objetos | [`GameState.scala`](src/main/scala/protocolocero/domain/game/GameState.scala), [`Story.scala`](src/main/scala/protocolocero/domain/game/Story.scala), [`World.scala`](src/main/scala/protocolocero/domain/game/World.scala), [`Characters.scala`](src/main/scala/protocolocero/domain/game/Characters.scala) | `Room`, `Exit`, `Device`, `GameState`, `StoryState` y personajes representan el mundo, el estado narrativo y el progreso de la nave. |
| Inmutabilidad; sin `var` | `src/main/scala/protocolocero/domain/game/` y [`Update.scala`](src/main/scala/protocolocero/app/Update.scala) | Los modelos usan `val` en sus parámetros; los cambios producen valores nuevos mediante `copy`, `map` y listas nuevas. Los archivos de lógica no declaran variables mutables. |
| Uso de listas | [`World.scala`](src/main/scala/protocolocero/domain/game/World.scala), [`GameState.scala`](src/main/scala/protocolocero/domain/game/GameState.scala), [`Story.scala`](src/main/scala/protocolocero/domain/game/Story.scala), [`Instruction.scala`](src/main/scala/protocolocero/domain/game/Instruction.scala) | `ShipMap.rooms` lista las habitaciones y sus salidas; el estado almacena habitaciones visitadas, personajes, pistas, decisiones, sistemas y registro. |
| Transformaciones funcionales | [`GameRules.scala`](src/main/scala/protocolocero/domain/game/GameRules.scala), [`ProgramCompiler.scala`](src/main/scala/protocolocero/domain/game/ProgramCompiler.scala), [`PuzzleInterpreter.scala`](src/main/scala/protocolocero/domain/game/PuzzleInterpreter.scala), [`ExplorationVentana.scala`](src/main/scala/protocolocero/ui/screens/ExplorationVentana.scala) | Se usa `find` para localizar, `filter` para obtener los dispositivos de una habitación y filtrar entradas, `map` para producir estados y `foldLeft` para procesar programas sin mutar el estado original. |
| Función de orden superior | [`ProgramCompiler.scala`](src/main/scala/protocolocero/domain/game/ProgramCompiler.scala), método `executeAll` | Recibe la función `step` como parámetro y la aplica a cada instrucción durante `foldLeft`. |
| `case class` y pattern matching | [`Instruction.scala`](src/main/scala/protocolocero/domain/game/Instruction.scala), [`StoryScenes.scala`](src/main/scala/protocolocero/domain/game/StoryScenes.scala), [`StoryRules.scala`](src/main/scala/protocolocero/domain/game/StoryRules.scala), [`ProgramCompiler.scala`](src/main/scala/protocolocero/domain/game/ProgramCompiler.scala) | Las instrucciones, interacciones, decisiones y finales son datos explícitos; `match` selecciona diálogos, efectos, puzzles y desenlaces. |
| Jerarquías: `trait`, `sealed trait` y clase abstracta | [`Characters.scala`](src/main/scala/protocolocero/domain/game/Characters.scala), [`Instruction.scala`](src/main/scala/protocolocero/domain/game/Instruction.scala) | `Identified` define datos comunes; `Character` es una clase abstracta extendida por `Engineer` y `StationAI`; `Instruction` y `GameResult` son jerarquías selladas para instrucciones y resultados conocidos. |
| Genéricos | [`Instruction.scala`](src/main/scala/protocolocero/domain/game/Instruction.scala), tipo `GameResult[A]`; [`ProgramCompiler.scala`](src/main/scala/protocolocero/domain/game/ProgramCompiler.scala), método `executeAll[A]` | El resultado tipado sirve para éxito con valores de distintos tipos o error; `executeAll` procesa instrucciones con cualquier tipo de acumulador compatible. |
| Separación entre interfaz y lógica | [`ExplorationVentana.scala`](src/main/scala/protocolocero/ui/screens/ExplorationVentana.scala), [`Update.scala`](src/main/scala/protocolocero/app/Update.scala), `src/main/scala/protocolocero/domain/game/` | La ventana muestra mapa, habitación, decisiones y editores contextuales; los mensajes llegan a `Update`; las reglas del dominio no manipulan el DOM. |
| Pruebas de la lógica | [`GameRulesSuite.scala`](src/test/scala/protocolocero/app/GameRulesSuite.scala), [`UpdateSuite.scala`](src/test/scala/protocolocero/app/UpdateSuite.scala), [`StoryGameSuite.scala`](src/test/scala/protocolocero/app/StoryGameSuite.scala) | Comprueban navegación, puzzles, progreso, confianza, decisiones idempotentes, restricciones de acceso y finales. |

## Juego narrativo y reglas

La demo recorre Grooming Salon, Cocina Brookies, Malegría, Postgrado y El Ágora. Desde Cocina Brookies, Malegría y Postgrado se puede volver directamente a Grooming Salon. Cada sala presenta tres interacciones narrativas principales. La partida empieza en Grooming Salon con una secuencia de despertar; el movimiento hacia adelante se habilita según los puzzles y decisiones obligatorias. Los atajos de regreso no eliminan progreso. El botón `D` abre los datos de habitación; los editores de código se abren al interactuar con la computadora, el controlador DJ o el registro de Postgrado.

El estado narrativo es inmutable y lleva confianza (0–10), sistemas restaurados, inventario, pistas, decisiones resueltas, puzzles y final. Las decisiones D1 y D2 solo aplican una vez. D1: aceptar los brookies de Tessa da +2, elegir los de Gaby da −1. D2: leer los archivos da −2 y activa la pista de la Sprint Review; confiar en Tessa da +2. La confianza inicia en 5. Confesar el prompt da +1; resolver el puzzle funcional opcional da +1 o +2; la hora libre opcional da +2. Estos valores iniciales/recompensas completan los datos que el lore deja sin fijar y hacen alcanzable tanto el final secreto como el Sprint Infinito.

## Puzzles de código

Los editores usan un lenguaje Scala-like deliberadamente limitado. No evalúan Scala arbitrario ni ejecutan procesos. En los puzzles de energía y DJ, el jugador escribe el `while` completo; la lista, las variables iniciales y la comprobación final ya están escritas en el editor. En energía debe incrementar `energia` hasta llegar exactamente a 100. En DJ debe recorrer `canales` con `indice`, sumar solo los positivos a `volumen` e incrementar el índice en cada vuelta. El lexer separa identificadores, números y símbolos, e ignora espacios y tabulaciones. El parser convierte el texto en instrucciones `case class` (`WhileLoop`, `IfThenElse` y `Assign`); el intérprete las ejecuta mediante pattern matching sobre mapas inmutables de variables y listas. Se admiten comparaciones `<`, `>` y `==`, acceso por índice a una lista, `while` anidados, `if/else` y asignaciones aritméticas. Hay límites de 100 iteraciones por bucle, 1.000 instrucciones ejecutadas, 20 niveles de anidamiento y 100 instrucciones por bloque. El puzzle de energía se resuelve si termina exactamente en 100; DJ debe recorrer todos los canales y terminar con volumen 100.

En El Ágora aparece un tercer ejercicio obligatorio antes de mostrar las decisiones finales: escribir un `if/else` que active comunicaciones cuando la energía supera 99. Los diálogos explican `while`, condiciones, índices, acumuladores y funciones de listas para orientar al jugador sin incluir una solución completa en los placeholders. El puzzle opcional de Postgrado requiere usar `filter` y una condición de igualdad para conservar el registro MANIZEH. Los intentos fallidos no cambian el estado y se pueden repetir; los editores rechazan entradas mayores de 5.000 caracteres o 100 líneas.

## Finales y decisiones resueltas

- **Tessa aliada:** liberar con confianza ≥7; restaura todos los sistemas.
- **Tessa apagada:** seleccionar apagarla; restaura todos los sistemas.
- **Hora Libre:** solo aparece con el prompt confesado, archivos leídos y confianza exactamente 10.
- **Sprint Infinito:** al abrir el panel del Ágora con confianza ≤2 y el puzzle DJ pendiente; el resultado es un reinicio narrativo y permite volver a empezar.

El lore no determina qué ocurre al liberar con confianza baja; la demo muestra un aviso y deja elegir otra opción. También exige que D1 habilite Malegría pero que una decisión opcional no bloquee el avance: aquí D1 es una decisión obligatoria de dos alternativas antes de salir de Cocina. Para hacer alcanzable Sprint Infinito, confianza ≤2 permite un paso de emergencia a Postgrado sin resolver el DJ; con confianza mayor, el controlador debe repararse. La confesión se ofrece en Postgrado si se encontró la nota del prompt. En esta versión se añadió un ejercicio de `if/else` en El Ágora para restaurar comunicaciones antes de mostrar las decisiones finales; es una ampliación de la demo, no un puzzle descrito explícitamente por el lore.

```text
val energia = 0
while (energia < 100) {
  energia = energia + 20
}
if (energia == 100) {
  println("GENERADOR ACTIVADO")
} else {
  println("ERROR DE ENERGIA")
}
```

El jugador escribe el `while` completo para el generador; el editor fija la inicialización y la condición final. Para el controlador DJ, el editor fija la lista, las variables iniciales y la validación final, mientras el jugador escribe el bucle y su cuerpo:

```text
while (indice < canales.length) {
  if (canales(indice) > 0) {
    volumen = volumen + canales(indice)
  }
  indice = indice + 1
}
```

El ejercicio de comunicaciones en El Ágora requiere escribir un `if/else` que active el sistema cuando `energia > 99`. La lógica Scala del juego y del intérprete permanece inmutable y no declara variables mutables. La sintaxis dentro del editor es una DSL restringida para el minijuego, no una ejecución del lenguaje Scala. La posible discrepancia académica está en que la DSL enseña y acepta `while` y acceso a listas mediante un contador (`canales(indice)`); no se muta una lista por posición. Conviene confirmar con el docente si la restricción aplica también a ese pseudolenguaje.
