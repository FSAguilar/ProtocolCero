package protocolocero.domain.game

object ProgramCompiler:
  import GameResult.*

  def compile(source: String): GameResult[List[Instruction]] =
    val lines = source.split("\n").toList.filter(line => line.trim.nonEmpty)

    if lines.isEmpty then Failure("Escribe al menos una instrucción.")
    else
      lines.foldLeft[GameResult[List[Instruction]]](Success(Nil)) {
        case (Failure(message), _) =>
          Failure(message)
        case (Success(instructions), line) =>
          parseLine(line) match
            case Success(instruction) => Success(instructions :+ instruction)
            case Failure(message) => Failure(message)
      }

  def execute(source: String, initialState: GameState): GameResult[GameState] =
    compile(source) match
      case Failure(message) =>
        Failure(message)
      case Success(instructions) =>
        executeAll(instructions, initialState)(GameRules.executeInstruction)

  def executeAll[A](
      instructions: List[Instruction],
      initialValue: A
  )(step: (A, Instruction) => GameResult[A]): GameResult[A] =
    instructions.foldLeft[GameResult[A]](Success(initialValue)) {
      case (Failure(message), _) =>
        Failure(message)
      case (Success(value), instruction) =>
        step(value, instruction)
    }

  private def parseLine(line: String): GameResult[Instruction] =
    // \\s+ significa "uno o más espacios en blanco", incluidos tabuladores.
    line.trim.toUpperCase.split("\\s+").toList match
      case "ACTIVAR" :: deviceId :: Nil =>
        Success(Activate(deviceId))
      case "DESACTIVAR" :: deviceId :: Nil =>
        Success(Deactivate(deviceId))
      case "ACTIVAR" :: Nil =>
        Failure("Falta el nombre del dispositivo. Ejemplo: ACTIVAR VENTILADOR.")
      case "DESACTIVAR" :: Nil =>
        Failure("Falta el nombre del dispositivo. Ejemplo: DESACTIVAR VENTILADOR.")
      case command :: _ =>
        Failure(s"Comando no reconocido: $command. Usa ACTIVAR o DESACTIVAR.")
      case Nil =>
        Failure("La instrucción está vacía.")
