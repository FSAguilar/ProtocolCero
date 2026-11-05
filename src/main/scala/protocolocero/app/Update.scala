package protocolocero.app

import protocolocero.domain.Screen
import protocolocero.domain.game.Direction
import protocolocero.domain.game.ExplorationPanel
import protocolocero.domain.game.GameState
import protocolocero.domain.game.GameResult
import protocolocero.domain.game.GameRules
import protocolocero.domain.game.ProgramCompiler
import protocolocero.domain.game.PuzzleId
import protocolocero.domain.game.PuzzleInterpreter
import protocolocero.domain.game.StoryAction
import protocolocero.domain.game.StoryChoice
import protocolocero.domain.game.StoryRules
import protocolocero.domain.game.StoryScenes
import protocolocero.domain.game.Ending

object Update:
  def apply(model: Model, message: Message): Model =
    message match
      case Message.NoOp =>
        model

      case Message.StartNewGame =>
        newGame(model)

      case Message.RestartGame =>
        newGame(model)

      case Message.ContinueGame =>
        model

      case Message.OpenOptions =>
        model.copy(screen = Screen.Options)

      case Message.OpenCredits =>
        model.copy(screen = Screen.Credits)

      case Message.ReturnToMenu =>
        model.copy(screen = Screen.MainMenu)

      case Message.ToggleReducedMotion =>
        model.copy(preferences = model.preferences.copy(reducedMotion = !model.preferences.reducedMotion))

      case Message.MoveLeft =>
        move(model, Direction.Left)

      case Message.MoveRight =>
        move(model, Direction.Right)

      case Message.ReturnToStart =>
        move(model, Direction.ReturnToStart)

      case Message.ShowRoomData =>
        model.copy(
          explorationPanel =
            if model.explorationPanel == ExplorationPanel.Data then ExplorationPanel.Closed
            else ExplorationPanel.Data
        )

      case Message.OpenRoomTerminal =>
        model.copy(
          explorationPanel =
            if model.explorationPanel == ExplorationPanel.Terminal then ExplorationPanel.Closed
            else ExplorationPanel.Terminal
        )

      case Message.CloseExplorationPanel =>
        model.copy(explorationPanel = ExplorationPanel.Closed)

      case Message.SelectStoryAction(action) =>
        selectStoryAction(model, action)

      case Message.SelectStoryChoice(choice) =>
        selectStoryChoice(model, choice)

      case Message.AdvanceRoomDialogue =>
        model.roomDialogue match
          case Some(dialogue) if dialogue.currentIndex + 1 < dialogue.lines.length =>
            model.copy(roomDialogue = Some(dialogue.copy(currentIndex = dialogue.currentIndex + 1)))
          case Some(_) =>
            model.copy(roomDialogue = None)
          case None =>
            model

      case Message.ProgramInputChanged(value) =>
        model.copy(programText = value, terminalFeedback = None)

      case Message.RunProgram =>
        ProgramCompiler.execute(model.programText, model.game) match
          case GameResult.Success(updatedGame) =>
            model.copy(
              game = updatedGame,
              terminalFeedback = Some("Programa ejecutado. Estado de los sistemas actualizado.")
            )
          case GameResult.Failure(message) =>
            model.copy(terminalFeedback = Some(s"ERROR: $message"))

      case Message.PuzzleInputChanged(value) =>
        model.copy(programText = value, puzzleFeedback = None)

      case Message.RunPuzzle =>
        runPuzzle(model)

      case Message.ClosePuzzle =>
        model.copy(activePuzzle = None, puzzleFeedback = None, programText = "")

  private def newGame(model: Model): Model =
    model.copy(
      screen = Screen.Exploration,
      game = GameState.initial,
      explorationPanel = ExplorationPanel.Closed,
      programText = "",
      terminalFeedback = None,
      roomDialogue = Some(StoryScenes.openingDialogue),
      sceneChoices = None,
      activePuzzle = None,
      puzzleFeedback = None
    )

  private def selectStoryAction(model: Model, action: StoryAction): Model =
    val roomActions = StoryScenes.actionsFor(model.game.currentRoomId).map(_._1)
    if model.game.story.ending.nonEmpty || !roomActions.contains(action) then model
    else if action == StoryAction.AgoraControl &&
        model.game.story.confidence <= 2 &&
        !model.game.story.solved(PuzzleId.DjController)
    then
      model.copy(
        game = model.game.copy(story = model.game.story.copy(ending = Some(Ending.InfiniteSprint))),
        roomDialogue = Some(StoryScenes.endingDialogue(Ending.InfiniteSprint)),
        sceneChoices = None
      )
    else
      val updatedStory = StoryRules.onInteraction(model.game.story, action)
      val updatedGame = model.game.copy(story = updatedStory)
      val puzzle =
        action match
          case StoryAction.GroomingComputer => Some(PuzzleId.KitchenGenerator)
          case StoryAction.MalegriaDanceFloor => Some(PuzzleId.DjController)
          case StoryAction.AgoraControl if !model.game.story.solved(PuzzleId.AgoraCommunications) =>
            Some(PuzzleId.AgoraCommunications)
          case _ => None
      puzzle match
        case Some(puzzleId) =>
          model.copy(
            game = updatedGame,
            activePuzzle = Some(puzzleId),
            programText = "",
            puzzleFeedback = None,
            roomDialogue = None,
            sceneChoices = None,
            terminalFeedback = None
          )
        case None =>
          val choices = StoryRules.choicesAfter(updatedStory, action)
          model.copy(
            game = updatedGame,
            roomDialogue = Some(StoryScenes.dialogueFor(action, updatedStory)),
            sceneChoices = if choices.isEmpty then None else Some(choices),
            activePuzzle = None,
            puzzleFeedback = None,
            terminalFeedback = None
          )

  private def selectStoryChoice(model: Model, choice: StoryChoice): Model =
    val availableChoices = model.sceneChoices.getOrElse(Nil).map(_._1)
    if !availableChoices.contains(choice) then model
    else
      StoryRules.applyChoice(model.game.story, choice) match
        case GameResult.Success(story) if choice == StoryChoice.StartOptionalPuzzle =>
          model.copy(
            activePuzzle = Some(PuzzleId.PostgradRecords),
            programText = "",
            puzzleFeedback = None,
            roomDialogue = Some(StoryScenes.dialogueForChoice(choice)),
            sceneChoices = None
          )
        case GameResult.Success(story) =>
          val updatedGame = model.game.copy(story = story)
          model.copy(
            game = updatedGame,
            roomDialogue = Some(StoryScenes.dialogueForChoice(choice)),
            sceneChoices = None,
            puzzleFeedback = None
          )
        case GameResult.Failure(message) =>
          model.copy(terminalFeedback = Some(message))

  private def runPuzzle(model: Model): Model =
    model.activePuzzle match
      case None => model
      case Some(puzzleId) =>
        PuzzleInterpreter.run(puzzleId, model.programText, model.game.story) match
          case GameResult.Failure(message) =>
            model.copy(puzzleFeedback = Some(message))
          case GameResult.Success(story) =>
            val logEntry =
              puzzleId match
                case PuzzleId.KitchenGenerator => "Protocolo del generador completado."
                case PuzzleId.DjController => "Controlador DJ reparado; sonido restaurado."
                case PuzzleId.PostgradRecords => "Registro de Manizeh encontrado."
                case PuzzleId.AgoraCommunications => "Comunicaciones restauradas."
            val updatedGame = model.game.copy(
              story = story,
              log = model.game.log :+ logEntry
            )
            val afterPuzzle =
              if puzzleId == PuzzleId.KitchenGenerator then
                GameRules.move(updatedGame, Direction.Right) match
                  case GameResult.Success(moved) => moved
                  case GameResult.Failure(_) => updatedGame
              else updatedGame
            model.copy(
              game = afterPuzzle,
              activePuzzle = None,
              programText = "",
              puzzleFeedback = None,
              roomDialogue = None,
              sceneChoices = None
            )

  private def move(model: Model, direction: Direction): Model =
    GameRules.move(model.game, direction) match
      case GameResult.Success(updatedGame) =>
        model.copy(
          game = updatedGame,
          terminalFeedback = None,
          roomDialogue = None,
          sceneChoices = None,
          activePuzzle = None,
          puzzleFeedback = None
        )
      case GameResult.Failure(message) =>
        model.copy(terminalFeedback = Some(message))
