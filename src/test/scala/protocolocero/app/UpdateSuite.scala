package protocolocero.app

import munit.FunSuite
import protocolocero.domain.Preferences
import protocolocero.domain.Screen
import protocolocero.domain.game.ExplorationPanel
import protocolocero.domain.game.PuzzleId
import protocolocero.domain.game.StoryAction
import protocolocero.domain.game.StoryScenes

final class UpdateSuite extends FunSuite:
  test("starting a new game begins exploration in the initial room"):
    val updated = Update(Model.initial, Message.StartNewGame)
    assertEquals(updated.screen, Screen.Exploration)
    assertEquals(updated.game.currentRoomId, "grooming")
    assertEquals(updated.preferences, Preferences.default)
    assertEquals(
      updated.roomDialogue.flatMap(_.currentLine.flatMap(_.speaker)),
      Some("NARRADOR")
    )

  test("the return shortcut navigates directly back to Grooming Salon"):
    val atPostgrado = Model.initial.copy(
      screen = Screen.Exploration,
      game = Model.initial.game.copy(currentRoomId = "postgrado")
    )
    val returned = Update(atPostgrado, Message.ReturnToStart)

    assertEquals(returned.game.currentRoomId, "grooming")

  test("the options preference toggles immutably and remains enabled after navigation"):
    val options = Update(Model.initial, Message.OpenOptions)
    val enabled = Update(options, Message.ToggleReducedMotion)
    val returned = Update(enabled, Message.ReturnToMenu)

    assertEquals(enabled.preferences.reducedMotion, true)
    assertEquals(returned.screen, Screen.MainMenu)
    assertEquals(returned.preferences.reducedMotion, true)
    assertEquals(Model.initial.preferences.reducedMotion, false)

  test("continuing without a saved game does not invent a game state"):
    val updated = Update(Model.initial, Message.ContinueGame)
    assertEquals(updated, Model.initial)

  test("credits can return to the main menu"):
    val credits = Update(Model.initial, Message.OpenCredits)
    val returned = Update(credits, Message.ReturnToMenu)
    assertEquals(credits.screen, Screen.Credits)
    assertEquals(returned.screen, Screen.MainMenu)

  test("a failed terminal program does not commit its earlier commands"):
    val withProgram = Model.initial.copy(
      screen = Screen.Exploration,
      programText = "DESACTIVAR GENERADOR\nACTIVAR DESCONOCIDO"
    )
    val updated = Update(withProgram, Message.RunProgram)

    assertEquals(updated.game, withProgram.game)
    assert(updated.terminalFeedback.exists(_.startsWith("ERROR:")))

  test("exploration panels stay closed until opened, switch, and close on request"):
    val started = Update(Model.initial, Message.StartNewGame)
    val data = Update(started, Message.ShowRoomData)
    val terminal = Update(data, Message.OpenRoomTerminal)
    val closed = Update(terminal, Message.CloseExplorationPanel)

    assertEquals(started.explorationPanel, ExplorationPanel.Closed)
    assertEquals(data.explorationPanel, ExplorationPanel.Data)
    assertEquals(terminal.explorationPanel, ExplorationPanel.Terminal)
    assertEquals(closed.explorationPanel, ExplorationPanel.Closed)

  test("the opening and room conversations advance one click at a time"):
    val started = Update(Model.initial, Message.StartNewGame)
    val outOfRoom = Update(
      Model.initial.copy(game = Model.initial.game.copy(currentRoomId = "cocina")),
      Message.SelectStoryAction(StoryAction.GroomingFidel)
    )
    val afterOpening =
      (1 to 5).foldLeft(started)((model, _) => Update(model, Message.AdvanceRoomDialogue))
    val conversation = Update(afterOpening, Message.SelectStoryAction(StoryAction.GroomingFidel))
    val nextLine = Update(conversation, Message.AdvanceRoomDialogue)
    val lastLine = Update(nextLine, Message.AdvanceRoomDialogue)
    val returnedToChoices = Update(lastLine, Message.AdvanceRoomDialogue)

    assertEquals(outOfRoom, Model.initial.copy(game = Model.initial.game.copy(currentRoomId = "cocina")))
    assertEquals(afterOpening.roomDialogue, None)
    assert(conversation.roomDialogue.exists(_.currentLine.exists(_.portrait.contains("./assets/personajes/lesbian_fidel.png"))))
    assertEquals(nextLine.roomDialogue.flatMap(_.currentLine.flatMap(_.speaker)), Some("Tessa"))
    assert(lastLine.roomDialogue.exists(_.currentLine.exists(_.speaker.contains("Fidel"))))
    assert(returnedToChoices.roomDialogue.exists(_.currentLine.exists(_.text.contains("while repite"))))

  test("moving away clears the active story conversation"):
    val atGrooming = Model.initial.copy(
      screen = Screen.Exploration,
      game = Model.initial.game.copy(
        currentRoomId = "grooming",
        story = Model.initial.game.story.copy(
          solvedPuzzles = List(PuzzleId.KitchenGenerator)
        )
      ),
      roomDialogue = Some(
        StoryScenes.dialogueFor(StoryAction.GroomingFidel, Model.initial.game.story)
      )
    )
    val moved = Update(atGrooming, Message.MoveRight)

    assertEquals(moved.game.currentRoomId, "cocina")
    assertEquals(moved.roomDialogue, None)

  test("the DJ list and loop explanation is available in Dani's conversation before the puzzle"):
    val dialogue = StoryScenes.dialogueFor(StoryAction.MalegriaDani, Model.initial.game.story)
    val text = dialogue.lines.map(_.text).mkString(" ")

    assert(text.contains("canales.length es la cantidad de elementos"))
    assert(text.contains("volumen acumula el total"))
    assert(text.contains("canales.length"))
    assert(text.contains("canales(indice)"))
