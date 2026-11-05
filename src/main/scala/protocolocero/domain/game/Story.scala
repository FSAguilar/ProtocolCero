package protocolocero.domain.game

enum StoryAction derives CanEqual:
  case GroomingComputer, GroomingFidel, GroomingDesk
  case KitchenGaby, KitchenOvens, KitchenStorage
  case MalegriaDani, MalegriaDanceFloor, MalegriaRest
  case PostgradChelito, PostgradAlexis, PostgradFiles
  case AgoraPastor, AgoraTessa, AgoraControl

enum StoryChoice derives CanEqual:
  case AcceptTessaBrookies, ChooseGabyBrookies
  case CloseStorageDoor, RetreatFromDewbak
  case ConfessPrompt, KeepPromptSecret
  case ReadClassifiedFiles, TrustTessa
  case StartOptionalPuzzle, FinishFreeHour
  case ShutDownTessa, ReleaseTessa, RequestFreeHour

enum StoryFlag derives CanEqual:
  case AcceptedTessaBrookies, ReadClassifiedFiles, ConfessedPrompt
  case FoundRabbit, FoundAwsInvoice, FoundPromptNote, FoundSecretBrookies
  case ReturnedRabbit, CompletedFreeHour

enum StoryItem derives CanEqual:
  case AwsInvoice, PromptNote, SecretBrookies, ManizehRabbit, ManizehRecord

enum PuzzleId derives CanEqual:
  case KitchenGenerator, DjController, PostgradRecords, AgoraCommunications

enum Ending derives CanEqual:
  case TessaAlly, TessaShutdown, FreeHour, InfiniteSprint

enum SystemId derives CanEqual:
  case Energy, Kitchen, Sound, Communications

final case class DialogueLine(
    speaker: Option[String],
    text: String,
    portrait: Option[String]
)

final case class RoomDialogue(lines: List[DialogueLine], currentIndex: Int):
  def currentLine: Option[DialogueLine] =
    lines.lift(currentIndex)

final case class StoryState(
    confidence: Int,
    flags: List[StoryFlag],
    inventory: List[StoryItem],
    restoredSystems: List[SystemId],
    solvedPuzzles: List[PuzzleId],
    resolvedChoices: List[StoryChoice],
    ending: Option[Ending]
):
  def hasFlag(flag: StoryFlag): Boolean =
    flags.contains(flag)

  def hasItem(item: StoryItem): Boolean =
    inventory.contains(item)

  def hasSystem(system: SystemId): Boolean =
    restoredSystems.contains(system)

  def solved(puzzle: PuzzleId): Boolean =
    solvedPuzzles.contains(puzzle)

  def chose(choice: StoryChoice): Boolean =
    resolvedChoices.contains(choice)

object StoryState:
  val initial: StoryState =
    StoryState(
      confidence = 5,
      flags = Nil,
      inventory = Nil,
      restoredSystems = Nil,
      solvedPuzzles = Nil,
      resolvedChoices = Nil,
      ending = None
    )

  def changeConfidence(state: StoryState, amount: Int): StoryState =
    state.copy(confidence = (state.confidence + amount).max(0).min(10))

  def addFlag(state: StoryState, flag: StoryFlag): StoryState =
    if state.hasFlag(flag) then state else state.copy(flags = state.flags :+ flag)

  def addItem(state: StoryState, item: StoryItem): StoryState =
    if state.hasItem(item) then state else state.copy(inventory = state.inventory :+ item)

  def restoreSystem(state: StoryState, system: SystemId): StoryState =
    if state.hasSystem(system) then state
    else state.copy(restoredSystems = state.restoredSystems :+ system)

  def solvePuzzle(state: StoryState, puzzle: PuzzleId): StoryState =
    if state.solved(puzzle) then state else state.copy(solvedPuzzles = state.solvedPuzzles :+ puzzle)
