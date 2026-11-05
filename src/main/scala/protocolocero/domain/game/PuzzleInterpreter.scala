package protocolocero.domain.game

import scala.annotation.tailrec

object PuzzleInterpreter:
  import GameResult.*

  private val djChannels = List(10, -1, 20, 0, 30, -5, 40)

  private val maxLoopIterations = 100
  private val maxProgramSteps = 1000
  private val maxBlockNesting = 20
  private val maxStatementsPerBlock = 100

  private sealed trait GeneratorExpression
  private object GeneratorExpression:
    final case class Number(value: Int) extends GeneratorExpression
    final case class Variable(name: String) extends GeneratorExpression
    final case class ListElement(list: String, index: String) extends GeneratorExpression
    final case class ListLength(name: String) extends GeneratorExpression
    final case class Arithmetic(left: GeneratorExpression, operation: Char, right: GeneratorExpression)
        extends GeneratorExpression

  private enum Comparison:
    case LessThan, GreaterThan, EqualTo

  private final case class GeneratorCondition(
      left: GeneratorExpression,
      comparison: Comparison,
      right: GeneratorExpression
  )
  private final case class GeneratorState(
      variables: Map[String, Int],
      lists: Map[String, List[Int]]
  )

  private sealed trait GeneratorInstruction
  private object GeneratorInstruction:
    final case class Assign(target: String, expression: GeneratorExpression) extends GeneratorInstruction
    final case class WhileLoop(
        condition: GeneratorCondition,
        body: List[GeneratorInstruction]
    ) extends GeneratorInstruction
    final case class IfThenElse(
        condition: GeneratorCondition,
        ifBody: List[GeneratorInstruction],
        elseBody: List[GeneratorInstruction]
    ) extends GeneratorInstruction

  private sealed trait GeneratorToken
  private object GeneratorToken:
    final case class Identifier(value: String) extends GeneratorToken
    final case class Number(value: String) extends GeneratorToken
    final case class Symbol(value: Char) extends GeneratorToken

  def run(puzzle: PuzzleId, source: String, initial: StoryState): GameResult[StoryState] =
    val cleaned = cleanSource(source)
    if source.length > 5000 || cleaned.length > 100 then
      Failure("El programa supera el límite seguro de tamaño.")
    else if initial.solved(puzzle) then
      Failure("Este puzzle ya fue resuelto.")
    else if cleaned.isEmpty then
      Failure("Escribe un programa antes de ejecutarlo.")
    else
      puzzle match
        case PuzzleId.KitchenGenerator => runGenerator(cleaned, initial)
        case PuzzleId.DjController => runDjController(cleaned, initial)
        case PuzzleId.PostgradRecords => runRecords(cleaned, initial)
        case PuzzleId.AgoraCommunications => runCommunications(cleaned, initial)

  private def runGenerator(source: List[String], state: StoryState): GameResult[StoryState] =
    parseProgram(source.mkString("\n")) match
      case Left(message) =>
        Failure(message)
      case Right(instructions) =>
        executeProgram(instructions, Map("energia" -> 0), Map.empty, maxProgramSteps) match
          case Left(message) =>
            Failure(message)
          case Right(result) =>
            result.variables.get("energia") match
              case Some(100) =>
                val withEnergy = StoryState.restoreSystem(state, SystemId.Energy)
                val withKitchen = StoryState.restoreSystem(withEnergy, SystemId.Kitchen)
                Success(StoryState.solvePuzzle(withKitchen, PuzzleId.KitchenGenerator))
              case Some(result) if result < 100 =>
                Failure(s"El bucle terminó con $result. La energía debe llegar exactamente a 100.")
              case Some(result) =>
                Failure(s"El bucle llegó a $result y sobrepasó 100. Ajusta el incremento.")
              case None =>
                Failure("El programa debe actualizar la variable energia.")

  private def parseProgram(source: String): Either[String, List[GeneratorInstruction]] =
    // Convierte el texto en tokens, luego en instrucciones anidadas.
    tokenizeProgram(source).flatMap { tokens =>
      parseStatements(tokens, inBlock = false, nesting = 0).flatMap {
        case (instructions, Nil) => Right(instructions)
        case _ => Left("Hay código inesperado después del bloque.")
      }
    }

  private def parseStatements(
      tokens: List[GeneratorToken],
      inBlock: Boolean,
      nesting: Int
  ): Either[String, (List[GeneratorInstruction], List[GeneratorToken])] =
    import GeneratorToken.*

    // Lee instrucciones hasta el fin o hasta la llave que cierra el bloque actual.
    @tailrec
    def collect(
        remaining: List[GeneratorToken],
        reversed: List[GeneratorInstruction],
        count: Int
    ): Either[String, (List[GeneratorInstruction], List[GeneratorToken])] =
      remaining match
        case Nil if inBlock =>
          Left("Falta una llave de cierre '}' en un bloque.")
        case Nil =>
          Right((reversed.reverse, Nil))
        case Symbol('}') :: _ if inBlock =>
          Right((reversed.reverse, remaining))
        case Symbol('}') :: _ =>
          Left("Hay una llave de cierre '}' sin un while.")
        case _ if count >= maxStatementsPerBlock =>
          Left("El bloque tiene demasiadas instrucciones.")
        case _ =>
          parseInstruction(remaining, nesting) match
            case Left(message) => Left(message)
            case Right((instruction, rest)) =>
              collect(rest, instruction :: reversed, count + 1)

    collect(tokens, Nil, 0)

  private def parseInstruction(
      tokens: List[GeneratorToken],
      nesting: Int
  ): Either[String, (GeneratorInstruction, List[GeneratorToken])] =
    import GeneratorInstruction.*
    import GeneratorToken.*

    tokens match
      case Identifier("while") :: Symbol('(') :: rest =>
        if nesting >= maxBlockNesting then
          Left("Los bloques están anidados más profundamente de lo permitido.")
        else
          parseCondition(rest).flatMap { case (condition, afterCondition) =>
            afterCondition match
              case Symbol(')') :: Symbol('{') :: bodyTokens =>
                parseStatements(bodyTokens, inBlock = true, nesting = nesting + 1).flatMap {
                  case (body, Symbol('}') :: afterBlock) =>
                    Right((WhileLoop(condition, body), skipSemicolon(afterBlock)))
                  case _ =>
                    Left("Falta una llave de cierre '}' en el while.")
                }
              case _ =>
                Left("El while debe tener una condición entre paréntesis y un bloque entre llaves.")
          }
      case Identifier("if") :: Symbol('(') :: rest =>
        if nesting >= maxBlockNesting then
          Left("Los bloques están anidados más profundamente de lo permitido.")
        else
          parseCondition(rest).flatMap { case (condition, afterCondition) =>
            afterCondition match
              case Symbol(')') :: Symbol('{') :: bodyTokens =>
                parseStatements(bodyTokens, inBlock = true, nesting = nesting + 1).flatMap {
                  case (ifBody, Symbol('}') :: Identifier("else") :: Symbol('{') :: elseTokens) =>
                    parseStatements(elseTokens, inBlock = true, nesting = nesting + 1).flatMap {
                      case (elseBody, Symbol('}') :: afterElse) =>
                        Right((IfThenElse(condition, ifBody, elseBody), skipSemicolon(afterElse)))
                      case _ =>
                        Left("Falta una llave de cierre '}' en el else.")
                    }
                  case (ifBody, Symbol('}') :: afterIf) =>
                    Right((IfThenElse(condition, ifBody, Nil), skipSemicolon(afterIf)))
                  case _ =>
                    Left("Falta una llave de cierre '}' en el if.")
                }
              case _ =>
                Left("El if debe tener una condición entre paréntesis y un bloque entre llaves.")
          }
      case Identifier(target) :: Symbol('=') :: rest =>
        parseExpression(rest).map { case (value, afterExpression) =>
          (Assign(target, value), skipSemicolon(afterExpression))
        }
      case Identifier("while") :: _ =>
        Left("El while debe usar paréntesis y llaves: while (condición) { ... }.")
      case Identifier("if") :: _ =>
        Left("El if debe usar paréntesis y llaves: if (condición) { ... }.")
      case Identifier(_) :: _ =>
        Left("La asignación debe tener la forma: variable = variable + valor.")
      case _ =>
        Left("Se esperaba un while, un if o una asignación.")

  private def parseCondition(
      tokens: List[GeneratorToken]
  ): Either[String, (GeneratorCondition, List[GeneratorToken])] =
    import Comparison.*
    import GeneratorToken.*

    parseExpression(tokens).flatMap { case (left, rest) =>
      rest match
        case Symbol('<') :: afterOperator =>
          parseExpression(afterOperator).map { case (right, remaining) =>
            (GeneratorCondition(left, LessThan, right), remaining)
          }
        case Symbol('>') :: afterOperator =>
          parseExpression(afterOperator).map { case (right, remaining) =>
            (GeneratorCondition(left, GreaterThan, right), remaining)
          }
        case Symbol('=') :: Symbol('=') :: afterOperator =>
          parseExpression(afterOperator).map { case (right, remaining) =>
            (GeneratorCondition(left, EqualTo, right), remaining)
          }
        case _ =>
          Left("La condición debe comparar dos valores con <, > o ==.")
    }

  private def parseExpression(
      tokens: List[GeneratorToken]
  ): Either[String, (GeneratorExpression, List[GeneratorToken])] =
    import GeneratorExpression.{ListElement, ListLength, Variable}
    import GeneratorToken.*

    val parsed = tokens match
      case GeneratorToken.Number(rawValue) :: rest =>
        rawValue.toIntOption match
          case Some(value) => Right((GeneratorExpression.Number(value), rest))
          case None => Left("El número está fuera del rango permitido.")
      case Identifier(list) :: Symbol('(') :: Identifier(index) :: Symbol(')') :: rest =>
        Right((ListElement(list, index), rest))
      case Identifier(list) :: Symbol('.') :: Identifier("length") :: rest =>
        Right((ListLength(list), rest))
      case Identifier(name) :: rest =>
        Right((Variable(name), rest))
      case _ =>
        Left("Se esperaba una variable, un número o un elemento de lista.")

    parsed.flatMap { case (left, rest) =>
      rest match
        case Symbol(operation) :: afterOperator if operation == '+' || operation == '-' =>
          parseExpression(afterOperator).map { case (right, remaining) =>
            (GeneratorExpression.Arithmetic(left, operation, right), remaining)
          }
        case _ =>
          Right((left, rest))
    }

  private def skipSemicolon(tokens: List[GeneratorToken]): List[GeneratorToken] =
    tokens match
      case GeneratorToken.Symbol(';') :: rest => rest
      case _ => tokens

  private def tokenizeProgram(source: String): Either[String, List[GeneratorToken]] =
    import GeneratorToken.*

    @tailrec
    def scan(
        remaining: List[Char],
        reversed: List[GeneratorToken]
    ): Either[String, List[GeneratorToken]] =
      remaining match
        case char :: tail if char.isWhitespace =>
          scan(tail, reversed)
        case char :: _ if char.isLetter || char == '_' =>
          val (word, rest) = remaining.span(next => next.isLetterOrDigit || next == '_')
          scan(rest, Identifier(word.mkString) :: reversed)
        case char :: _ if char.isDigit =>
          val (number, rest) = remaining.span(_.isDigit)
          scan(rest, Number(number.mkString) :: reversed)
        case char :: tail if "(){}<>=+-;.".contains(char) =>
          scan(tail, Symbol(char) :: reversed)
        case char :: _ =>
          Left(s"Símbolo no permitido en el pseudocódigo: '$char'.")
        case Nil =>
          Right(reversed.reverse)

    scan(source.toList, Nil)

  private def executeProgram(
      instructions: List[GeneratorInstruction],
      initialValues: Map[String, Int],
      lists: Map[String, List[Int]],
      initialBudget: Int
  ): Either[String, GeneratorState] =
    import GeneratorInstruction.*
    import GeneratorExpression.*
    import Comparison.*

    // Cada asignación crea un mapa nuevo; el presupuesto limita el trabajo total.
    def executeStatements(
        remaining: List[GeneratorInstruction],
        state: GeneratorState,
        budget: Int
    ): Either[String, (GeneratorState, Int)] =
      remaining match
        case Nil =>
          Right((state, budget))
        case _ if budget <= 0 =>
          Left("El programa superó el límite seguro de instrucciones.")
        case Assign(target, expression) :: rest =>
          evaluate(expression, state).flatMap { result =>
            val afterAssignment = state.copy(variables = state.variables.updated(target, result))
            executeStatements(rest, afterAssignment, budget - 1)
          }
        case WhileLoop(condition, body) :: rest =>
          executeWhile(condition, body, state, budget, 0).flatMap {
            case (afterLoop, remainingBudget) =>
              executeStatements(rest, afterLoop, remainingBudget)
          }
        case IfThenElse(condition, ifBody, elseBody) :: rest =>
          evaluateCondition(condition, state).flatMap { isTrue =>
            val selectedBody = if isTrue then ifBody else elseBody
            executeStatements(selectedBody, state, budget - 1).flatMap {
              case (afterBranch, remainingBudget) =>
                executeStatements(rest, afterBranch, remainingBudget)
            }
          }

    def executeWhile(
        condition: GeneratorCondition,
        body: List[GeneratorInstruction],
        state: GeneratorState,
        budget: Int,
        iterations: Int
    ): Either[String, (GeneratorState, Int)] =
      evaluateCondition(condition, state).flatMap {
        case false =>
          Right((state, budget))
        case true if iterations >= maxLoopIterations =>
          Left(s"Un while superó el límite de $maxLoopIterations iteraciones.")
        case true if budget <= 0 =>
          Left("El programa superó el límite seguro de instrucciones.")
        case true =>
          executeStatements(body, state, budget - 1).flatMap {
            case (afterBody, remainingBudget) =>
              executeWhile(condition, body, afterBody, remainingBudget, iterations + 1)
          }
      }

    def evaluate(expression: GeneratorExpression, state: GeneratorState): Either[String, Int] =
      expression match
        case Number(value) =>
          Right(value)
        case Variable(name) =>
          state.variables.get(name).toRight(s"La variable '$name' no tiene un valor inicial.")
        case ListElement(list, index) =>
          for
            values <- state.lists.get(list).toRight(s"La lista '$list' no existe.")
            indexValue <- state.variables.get(index).toRight(s"La variable '$index' no tiene un valor inicial.")
            value <- values.lift(indexValue).toRight(s"El índice $indexValue está fuera de la lista '$list'.")
          yield value
        case ListLength(name) =>
          state.lists.get(name).map(_.length).toRight(s"La lista '$name' no existe.")
        case Arithmetic(left, operation, right) =>
          for
            leftValue <- evaluate(left, state)
            rightValue <- evaluate(right, state)
            result <- safeArithmetic(leftValue, operation, rightValue)
          yield result

    def evaluateCondition(condition: GeneratorCondition, state: GeneratorState): Either[String, Boolean] =
      for
        left <- evaluate(condition.left, state)
        right <- evaluate(condition.right, state)
      yield
        condition.comparison match
          case LessThan => left < right
          case GreaterThan => left > right
          case EqualTo => left == right

    def safeArithmetic(left: Int, operation: Char, right: Int): Either[String, Int] =
      val result =
        operation match
          case '+' => left.toLong + right.toLong
          case '-' => left.toLong - right.toLong
          case _ => left.toLong
      if result < Int.MinValue || result > Int.MaxValue then
        Left("La operación produjo un valor fuera del rango permitido.")
      else Right(result.toInt)

    val initialState = GeneratorState(initialValues, lists)
    executeStatements(instructions, initialState, initialBudget).map(_._1)

  private def runDjController(source: List[String], state: StoryState): GameResult[StoryState] =
    parseProgram(source.mkString("\n")) match
      case Left(message) =>
        Failure(message)
      case Right(instructions) =>
        val initialVariables = Map("indice" -> 0, "volumen" -> 0)
        val lists = Map("canales" -> djChannels)
        val hasExpectedLoop = instructions match
          case List(
                GeneratorInstruction.WhileLoop(
                  GeneratorCondition(
                    GeneratorExpression.Variable("indice"),
                    Comparison.LessThan,
                    GeneratorExpression.ListLength("canales")
                  ),
                  _
                )
              ) =>
            true
          case _ =>
            false

        if !hasExpectedLoop then
          Failure("Escribe un while que recorra los canales usando indice < canales.length.")
        else executeProgram(instructions, initialVariables, lists, maxProgramSteps) match
          case Left(message) =>
            Failure(message)
          case Right(result) =>
            val finishedList = result.variables.get("indice").contains(djChannels.length)
            val correctVolume = result.variables.get("volumen").contains(100)
            if finishedList && correctVolume then
              Success(
                StoryState.solvePuzzle(
                  StoryState.restoreSystem(state, SystemId.Sound),
                  PuzzleId.DjController
                )
              )
            else
              Failure(
                "Recorre todos los canales, suma solo los positivos y termina con volumen igual a 100."
              )

  private def runCommunications(source: List[String], state: StoryState): GameResult[StoryState] =
    import Comparison.*
    import GeneratorExpression.*
    import GeneratorInstruction.*

    val expectedProgram =
      List(
        IfThenElse(
          GeneratorCondition(Variable("energia"), GreaterThan, Number(99)),
          List(Assign("comunicaciones", Number(1))),
          List(Assign("comunicaciones", Number(0)))
        )
      )

    parseProgram(source.mkString("\n")) match
      case Left(message) =>
        Failure(message)
      case Right(instructions) if instructions != expectedProgram =>
        Failure("Usa if/else para activar comunicaciones solo cuando energia sea mayor que 99.")
      case Right(instructions) =>
        executeProgram(
          instructions,
          Map("energia" -> 100, "comunicaciones" -> 0),
          Map.empty,
          maxProgramSteps
        ) match
          case Left(message) =>
            Failure(message)
          case Right(result) if result.variables.get("comunicaciones").contains(1) =>
            val restored = StoryState.restoreSystem(state, SystemId.Communications)
            Success(StoryState.solvePuzzle(restored, PuzzleId.AgoraCommunications))
          case Right(_) =>
            Failure("Las comunicaciones siguen desconectadas.")

  private def runRecords(source: List[String], state: StoryState): GameResult[StoryState] =
    val text = source.mkString("\n").toLowerCase
    val filters = text.contains(".filter") || text.contains("filter(")
    val orders = text.contains(".sorted") || text.contains("sort(")
    val processesRecords = text.contains("registro") || text.contains("record")

    if filters && orders && processesRecords then
      val withTrust = StoryState.changeConfidence(state, 2)
      val withRecord = StoryState.addItem(withTrust, StoryItem.ManizehRecord)
      Success(StoryState.solvePuzzle(withRecord, PuzzleId.PostgradRecords))
    else if filters && processesRecords then
      val withTrust = StoryState.changeConfidence(state, 1)
      val withRecord = StoryState.addItem(withTrust, StoryItem.ManizehRecord)
      Success(StoryState.solvePuzzle(withRecord, PuzzleId.PostgradRecords))
    else
      Failure("Usa una función filter sobre los registros; ordénalos para encontrar la pista completa.")

  private def cleanSource(source: String): List[String] =
    source
      .split("\n")
      .toList
      // Quita comentarios # o // hasta el final de cada línea; .* significa "cualquier texto".
      .map(_.takeWhile(_ != '#').replaceAll("//.*$", "").trim)
      .filter(_.nonEmpty)
