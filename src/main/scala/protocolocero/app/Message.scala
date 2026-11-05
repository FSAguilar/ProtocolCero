package protocolocero.app

import tyrian.GlobalMsg
import protocolocero.domain.game.StoryAction
import protocolocero.domain.game.StoryChoice

enum Message extends GlobalMsg:
  case NoOp
  case StartNewGame
  case ContinueGame
  case OpenOptions
  case OpenCredits
  case ReturnToMenu
  case ToggleReducedMotion
  case MoveLeft
  case MoveRight
  case ReturnToStart
  case RestartGame
  case ShowRoomData
  case OpenRoomTerminal
  case CloseExplorationPanel
  case SelectStoryAction(action: StoryAction)
  case SelectStoryChoice(choice: StoryChoice)
  case AdvanceRoomDialogue
  case ProgramInputChanged(value: String)
  case RunProgram
  case PuzzleInputChanged(value: String)
  case RunPuzzle
  case ClosePuzzle
  