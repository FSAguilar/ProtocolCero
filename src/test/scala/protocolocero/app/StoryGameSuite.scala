package protocolocero.app

import munit.FunSuite
import protocolocero.domain.game.Direction
import protocolocero.domain.game.Ending
import protocolocero.domain.game.GameResult
import protocolocero.domain.game.GameState
import protocolocero.domain.game.PuzzleId
import protocolocero.domain.game.PuzzleInterpreter
import protocolocero.domain.game.StoryAction
import protocolocero.domain.game.StoryChoice
import protocolocero.domain.game.StoryFlag
import protocolocero.domain.game.StoryRules
import protocolocero.domain.game.StoryScenes
import protocolocero.domain.game.StoryState
import protocolocero.domain.game.SystemId

final class StoryGameSuite extends FunSuite:
  private def value(result: GameResult[StoryState]): StoryState =
    result match
      case GameResult.Success(story) => story
      case GameResult.Failure(message) => fail(message)

  private val generatorSolution =
    """while (energia < 100) {
      |  energia = energia + 20
      |}""".stripMargin

  private val djSolution =
    """while (indice < canales.length) {
      |  if (canales(indice) > 0) {
      |    volumen = volumen + canales(indice)
      |  }
      |  indice = indice + 1
      |}""".stripMargin

  test("each location presents exactly three primary story interactions"):
    assertEquals(
      List("grooming", "cocina", "malegria", "postgrado", "agora")
        .map(room => StoryScenes.actionsFor(room).length),
      List(3, 3, 3, 3, 3)
    )

  test("the generator puzzle validates exact energy and restores kitchen systems"):
    val result = PuzzleInterpreter.run(PuzzleId.KitchenGenerator, generatorSolution, StoryState.initial)

    result match
      case GameResult.Success(story) =>
        assert(story.solved(PuzzleId.KitchenGenerator))
        assert(story.hasSystem(SystemId.Energy))
        assert(story.hasSystem(SystemId.Kitchen))
        assertEquals(StoryState.initial.restoredSystems, Nil)
      case GameResult.Failure(message) =>
        fail(message)

  test("the generator evaluates a while loop until its condition becomes false"):
    val tenPerStep = PuzzleInterpreter.run(
      PuzzleId.KitchenGenerator,
      """while (energia < 100) {
        |  energia = energia + 10
        |}""".stripMargin,
      StoryState.initial
    )
    assert(tenPerStep.isInstanceOf[GameResult.Success[StoryState]])

    val wrongVariable = PuzzleInterpreter.run(
      PuzzleId.KitchenGenerator,
      """while (energia < 100) {
        |  energia = total + 20
        |}""".stripMargin,
      StoryState.initial
    )
    assert(wrongVariable.isInstanceOf[GameResult.Failure])

  test("the generator parser ignores whitespace and evaluates nested while blocks"):
    val compact = PuzzleInterpreter.run(
      PuzzleId.KitchenGenerator,
      "while(energia<100){energia=energia+20}",
      StoryState.initial
    )
    assert(compact.isInstanceOf[GameResult.Success[StoryState]])

    val nested =
      """while (energia < 100) {
        |  while (energia < 50) {
        |    energia = energia + 10
        |  }
        |  energia = energia + 10
        |}""".stripMargin
    assert(
      PuzzleInterpreter
        .run(PuzzleId.KitchenGenerator, nested, StoryState.initial)
        .isInstanceOf[GameResult.Success[StoryState]]
    )

    val withTabs =
      "while\t(\tenergia < 100\t) {\n\tenergia\t=\tenergia\t+\t20\n}"
    assert(
      PuzzleInterpreter
        .run(PuzzleId.KitchenGenerator, withTabs, StoryState.initial)
        .isInstanceOf[GameResult.Success[StoryState]]
    )

  test("the generator rejects a non-exact result, zero increment, and oversized source"):
    val invalid = generatorSolution.replace("+ 20", "+ 30")
    assert(PuzzleInterpreter.run(PuzzleId.KitchenGenerator, invalid, StoryState.initial).isInstanceOf[GameResult.Failure])
    assert(
      PuzzleInterpreter
        .run(
          PuzzleId.KitchenGenerator,
          """while (energia < 100) {
            |  energia = energia + 0
            |}""".stripMargin,
          StoryState.initial
        )
        .isInstanceOf[GameResult.Failure]
    )
    assert(
      PuzzleInterpreter
        .run(
          PuzzleId.KitchenGenerator,
          generatorSolution.replace("energia + 20", "energia + 20; ejecutar()"),
          StoryState.initial
        )
        .isInstanceOf[GameResult.Failure]
    )
    assert(
      PuzzleInterpreter
        .run(PuzzleId.KitchenGenerator, "x" * 5001, StoryState.initial)
        .isInstanceOf[GameResult.Failure]
    )

  test("the DJ puzzle filters the specified positive channels and restores sound"):
    PuzzleInterpreter.run(PuzzleId.DjController, djSolution, StoryState.initial) match
      case GameResult.Success(story) =>
        assert(story.solved(PuzzleId.DjController))
        assert(story.hasSystem(SystemId.Sound))
      case GameResult.Failure(message) =>
        fail(message)

  test("the DJ puzzle interprets the player's while loop and rejects incorrect logic"):
    val compactLoop =
      "while(indice<canales.length){if(canales(indice)>0){volumen=volumen+canales(indice)}indice=indice+1}"
    assert(
      PuzzleInterpreter
        .run(PuzzleId.DjController, compactLoop, StoryState.initial)
        .isInstanceOf[GameResult.Success[StoryState]]
    )

    val withElseBranch =
      djSolution.replace(
        "}\nindice =",
        "} else {\n}\nindice ="
      )
    assert(
      PuzzleInterpreter
        .run(PuzzleId.DjController, withElseBranch, StoryState.initial)
        .isInstanceOf[GameResult.Success[StoryState]]
    )

    val doesNotSkipNegativeChannels =
      """volumen = volumen + canales(indice)
        |indice = indice + 1""".stripMargin
    assert(
      PuzzleInterpreter
        .run(PuzzleId.DjController, doesNotSkipNegativeChannels, StoryState.initial)
        .isInstanceOf[GameResult.Failure]
    )

    val neverAdvances =
      """if (canales(indice) > 0) {
        |  volumen = volumen + canales(indice)
        |}""".stripMargin
    assert(
      PuzzleInterpreter
        .run(PuzzleId.DjController, neverAdvances, StoryState.initial)
        .isInstanceOf[GameResult.Failure]
    )

  test("the optional records puzzle gives bounded confidence and the research clue"):
    val source =
      """val registros = List("BORIS", "MANIZEH", "TESSA")
        |val pista = registros.filter(_.contains("MANIZEH")).sorted""".stripMargin
    PuzzleInterpreter.run(PuzzleId.PostgradRecords, source, StoryState.initial) match
      case GameResult.Success(story) =>
        assertEquals(story.confidence, 7)
        assert(story.hasItem(protocolocero.domain.game.StoryItem.ManizehRecord))
      case GameResult.Failure(message) =>
        fail(message)

  test("the communications puzzle evaluates an if/else before the final choice"):
    val solution =
      """if (energia > 99) {
        |  comunicaciones = 1
        |} else {
        |  comunicaciones = 0
        |}""".stripMargin
    PuzzleInterpreter.run(PuzzleId.AgoraCommunications, solution, StoryState.initial) match
      case GameResult.Success(story) =>
        assert(story.solved(PuzzleId.AgoraCommunications))
        assert(story.hasSystem(SystemId.Communications))
      case GameResult.Failure(message) =>
        fail(message)

    val wrongBranch = solution.replace("comunicaciones = 1", "comunicaciones = 0")
    assert(
      PuzzleInterpreter
        .run(PuzzleId.AgoraCommunications, wrongBranch, StoryState.initial)
        .isInstanceOf[GameResult.Failure]
    )

  test("decisions are idempotent and clamp confidence to zero through ten"):
    val accepted =
      StoryRules.applyChoice(StoryState.initial, StoryChoice.AcceptTessaBrookies)
    accepted match
      case GameResult.Success(story) =>
        assertEquals(story.confidence, 7)
        assert(story.hasFlag(StoryFlag.AcceptedTessaBrookies))
        assert(StoryRules.applyChoice(story, StoryChoice.AcceptTessaBrookies).isInstanceOf[GameResult.Failure])
        assert(StoryRules.applyChoice(story, StoryChoice.ChooseGabyBrookies).isInstanceOf[GameResult.Failure])
      case GameResult.Failure(message) =>
        fail(message)

    val lowConfidence = StoryState.changeConfidence(StoryState.initial, -30)
    assertEquals(lowConfidence.confidence, 0)

  test("the hidden ending appears only with confession, files, and exactly ten trust"):
    val unlocked = StoryState.initial.copy(
      confidence = 10,
      flags = List(StoryFlag.ConfessedPrompt, StoryFlag.ReadClassifiedFiles)
    )
    val locked = unlocked.copy(confidence = 9)

    assert(StoryScenes.choicesAfter(StoryAction.AgoraControl, unlocked).exists(_._1 == StoryChoice.RequestFreeHour))
    assert(!StoryScenes.choicesAfter(StoryAction.AgoraControl, locked).exists(_._1 == StoryChoice.RequestFreeHour))

  test("low confidence cannot release Tessa; shutdown and secret request produce distinct endings"):
    val lowTrust = StoryState.initial.copy(confidence = 6)
    assert(StoryRules.applyChoice(lowTrust, StoryChoice.ReleaseTessa).isInstanceOf[GameResult.Failure])

    val shutdown = StoryRules.applyChoice(lowTrust, StoryChoice.ShutDownTessa)
    assertEquals(shutdown, GameResult.Success(lowTrust.copy(ending = Some(Ending.TessaShutdown), restoredSystems = List(
      SystemId.Energy, SystemId.Kitchen, SystemId.Sound, SystemId.Communications
    ), resolvedChoices = List(StoryChoice.ShutDownTessa))))

    val secret = StoryRules.applyChoice(
      StoryState.initial.copy(
        confidence = 10,
        flags = List(StoryFlag.ConfessedPrompt, StoryFlag.ReadClassifiedFiles)
      ),
      StoryChoice.RequestFreeHour
    )
    assert(secret.isInstanceOf[GameResult.Success[StoryState]])

  test("the secret route is achievable with the documented trust rewards"):
    val withPrompt = StoryState.addFlag(StoryState.initial, StoryFlag.FoundPromptNote)
    val accepted = value(StoryRules.applyChoice(withPrompt, StoryChoice.AcceptTessaBrookies))
    val filesRead = value(StoryRules.applyChoice(accepted, StoryChoice.ReadClassifiedFiles))
    val confessed = value(StoryRules.applyChoice(filesRead, StoryChoice.ConfessPrompt))
    val puzzleCode =
      """val registros = List("BORIS", "MANIZEH", "TESSA")
        |val pista = registros.filter(_.contains("MANIZEH")).sorted""".stripMargin
    val puzzle = value(PuzzleInterpreter.run(PuzzleId.PostgradRecords, puzzleCode, confessed))
    val hour = value(StoryRules.applyChoice(puzzle, StoryChoice.FinishFreeHour))

    assertEquals(hour.confidence, 10)
    assert(StoryRules.canRequestFreeHour(hour))
    val ended = value(StoryRules.applyChoice(hour, StoryChoice.RequestFreeHour))
    assertEquals(ended.ending, Some(Ending.FreeHour))
    assertEquals(
      ended.restoredSystems,
      List(SystemId.Energy, SystemId.Kitchen, SystemId.Sound, SystemId.Communications)
    )
    assertEquals(ended.confidence, 10)

  test("low trust and an unresolved DJ puzzle trigger the infinite sprint ending"):
    val atAgora = Model.initial.copy(
      game = Model.initial.game.copy(
        currentRoomId = "agora",
        story = StoryState.initial.copy(confidence = 2)
      )
    )
    val ended = Update(atAgora, Message.SelectStoryAction(StoryAction.AgoraControl))

    assertEquals(ended.game.story.ending, Some(Ending.InfiniteSprint))

  test("the Agora control opens the communications puzzle before final choices"):
    val atAgora = Model.initial.copy(
      game = Model.initial.game.copy(
        currentRoomId = "agora",
        story = StoryState.initial.copy(
          solvedPuzzles = List(PuzzleId.KitchenGenerator, PuzzleId.DjController)
        )
      )
    )
    val puzzle = Update(atAgora, Message.SelectStoryAction(StoryAction.AgoraControl))
    val typed = Update(
      puzzle,
      Message.PuzzleInputChanged(
        """if (energia > 99) {
          |  comunicaciones = 1
          |} else {
          |  comunicaciones = 0
          |}""".stripMargin
      )
    )
    val completed = Update(typed, Message.RunPuzzle)
    val control = Update(completed, Message.SelectStoryAction(StoryAction.AgoraControl))

    assertEquals(puzzle.activePuzzle, Some(PuzzleId.AgoraCommunications))
    assert(completed.game.story.hasSystem(SystemId.Communications))
    assertEquals(completed.activePuzzle, None)
    assert(control.sceneChoices.exists(_.exists(_._1 == StoryChoice.ReleaseTessa)))

  test("finishing the first puzzle opens the kitchen and locks later puzzles until solved"):
    val started = Update(Model.initial, Message.StartNewGame)
    val editor = Update(started, Message.SelectStoryAction(StoryAction.GroomingComputer))
    val typed = Update(editor, Message.PuzzleInputChanged(generatorSolution))
    val completed = Update(typed, Message.RunPuzzle)

    assertEquals(editor.activePuzzle, Some(PuzzleId.KitchenGenerator))
    assertEquals(completed.game.currentRoomId, "cocina")
    assert(completed.game.story.hasSystem(SystemId.Kitchen))
    assert(completed.game.story.solved(PuzzleId.KitchenGenerator))
    assert(GameState.initial.story.restoredSystems.isEmpty)

  test("the action handler ignores interactions from a different room"):
    val initial = Model.initial.copy(game = Model.initial.game.copy(currentRoomId = "malegria"))
    assertEquals(
      Update(initial, Message.SelectStoryAction(StoryAction.GroomingComputer)),
      initial
    )

  test("a solved generator enables the immutable move to the kitchen"):
    val state = GameState.initial.copy(
      story = StoryState.initial.copy(
        confidence = 8,
        solvedPuzzles = List(PuzzleId.KitchenGenerator, PuzzleId.DjController)
      )
    )
    val result = protocolocero.domain.game.GameRules.move(state, Direction.Right)
    assertEquals(
      result,
      GameResult.Success(state.copy(
        currentRoomId = "cocina",
        visitedRoomIds = List("grooming", "cocina"),
        characters = state.characters.map {
          case engineer: protocolocero.domain.game.Engineer if engineer.id == "player" =>
            engineer.copy(roomId = "cocina")
          case character => character
        },
        log = state.log :+ "Entraste en Cocina Brookies."
      ))
    )
    assertEquals(state.currentRoomId, "grooming")
