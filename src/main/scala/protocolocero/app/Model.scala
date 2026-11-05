package protocolocero.app

import protocolocero.domain.Preferences
import protocolocero.domain.Screen
import protocolocero.domain.game.ExplorationPanel
import protocolocero.domain.game.GameState
import protocolocero.domain.game.PuzzleId
import protocolocero.domain.game.RoomDialogue
import protocolocero.domain.game.StoryChoice

final case class Model(
    screen: Screen,
    preferences: Preferences,
    hasSavedGame: Boolean,
    game: GameState,
    explorationPanel: ExplorationPanel,
    programText: String,
    terminalFeedback: Option[String],
    roomDialogue: Option[RoomDialogue],
    sceneChoices: Option[List[(StoryChoice, String)]],
    activePuzzle: Option[PuzzleId],
    puzzleFeedback: Option[String]
)

object Model:
  val initial: Model =
    Model(
      screen = Screen.MainMenu,
      preferences = Preferences.default,
      hasSavedGame = false,
      game = GameState.initial,
      explorationPanel = ExplorationPanel.Closed,
      programText = "",
      terminalFeedback = None,
      roomDialogue = None,
      sceneChoices = None,
      activePuzzle = None,
      puzzleFeedback = None
    )
