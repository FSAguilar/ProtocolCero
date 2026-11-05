package protocolocero.app

import munit.FunSuite
import protocolocero.domain.game.Activate
import protocolocero.domain.game.Deactivate
import protocolocero.domain.game.Direction
import protocolocero.domain.game.GameResult
import protocolocero.domain.game.GameRules
import protocolocero.domain.game.GameState
import protocolocero.domain.game.ProgramCompiler
import protocolocero.domain.game.ShipMap

final class GameRulesSuite extends FunSuite:
  test("the five map locations use the matching background assets in route order"):
    assertEquals(
      ShipMap.rooms.map(_.name),
      List("Grooming Salon", "Cocina Brookies", "Malegría", "Postgrado", "El Ágora")
    )
    assertEquals(
      ShipMap.rooms.map(_.backgroundImage),
      List(
        "./assets/fondos/grooming_salon.jpg",
        "./assets/fondos/cocina_de_brookies.jpg",
        "./assets/fondos/malegria.jpg",
        "./assets/fondos/postgrado.jpg",
        "./assets/fondos/agora.jpg"
      )
    )
    assert(
      ShipMap.rooms.forall { room =>
        room.deviceIds == GameState.initial.devices.filter(_.roomId == room.id).map(_.id)
      }
    )

  test("moving right twice follows the requested room order without mutating the old state"):
    val initial = GameState.initial.copy(
      story = GameState.initial.story.copy(
        solvedPuzzles = List(protocolocero.domain.game.PuzzleId.KitchenGenerator),
        resolvedChoices = List(protocolocero.domain.game.StoryChoice.AcceptTessaBrookies)
      )
    )
    val firstMove = GameRules.move(initial, Direction.Right)
    val moved = firstMove match
      case GameResult.Success(state) => GameRules.move(state, Direction.Right)
      case GameResult.Failure(message) => fail(message)

    moved match
      case GameResult.Success(state) =>
        assertEquals(state.currentRoomId, "malegria")
        assert(state.visitedRoomIds.contains("cocina"))
        assert(state.visitedRoomIds.contains("malegria"))
        assertEquals(state.characters.find(_.id == "player").map(_.roomId), Some("malegria"))
        assertEquals(initial.currentRoomId, "grooming")
        assertEquals(initial.visitedRoomIds, List("grooming"))
      case GameResult.Failure(message) =>
        fail(message)

  test("the route ends at El Ágora instead of looping back to the starting room"):
    val stateAtCorridor = GameState.initial.copy(
      currentRoomId = "agora",
      visitedRoomIds = List("grooming", "cocina", "malegria", "postgrado", "agora")
    )

    GameRules.move(stateAtCorridor, Direction.Right) match
      case GameResult.Success(_) => fail("Ágora should be the end of the route.")
      case GameResult.Failure(message) => assertEquals(message, "No hay una salida en esa dirección.")

  test("cocina, malegria, and postgrado provide a direct return to grooming"):
    List("cocina", "malegria", "postgrado").foreach { roomId =>
      val state = GameState.initial.copy(currentRoomId = roomId)

      GameRules.move(state, Direction.ReturnToStart) match
        case GameResult.Success(returned) =>
          assertEquals(returned.currentRoomId, "grooming")
          assert(returned.visitedRoomIds.contains("grooming"))
        case GameResult.Failure(message) =>
          fail(s"$roomId should have a return route: $message")
    }

  test("el agora is the final location and has no direct return shortcut"):
    val state = GameState.initial.copy(currentRoomId = "agora")

    GameRules.move(state, Direction.ReturnToStart) match
      case GameResult.Success(_) => fail("El Ágora should not have a return shortcut.")
      case GameResult.Failure(message) =>
        assertEquals(message, "No hay una salida en esa dirección.")

  test("the compiler parses and executes multiple local commands in order"):
    val initial = GameState.initial
    val result = ProgramCompiler.execute(
      "DESACTIVAR VENTILADOR\nACTIVAR VENTILADOR",
      initial
    )

    result match
      case GameResult.Success(state) =>
        assertEquals(state.devices.find(_.id == "VENTILADOR").map(_.active), Some(true))
        assertEquals(state.log.length, initial.log.length + 2)
        assertEquals(initial.devices.find(_.id == "VENTILADOR").map(_.active), Some(true))
      case GameResult.Failure(message) =>
        fail(message)

  test("compiler rejects unknown commands without changing the game state"):
    val initial = GameState.initial
    val result = ProgramCompiler.execute("ABRIR PUERTA", initial)

    result match
      case GameResult.Success(_) => fail("An unknown command should not succeed.")
      case GameResult.Failure(message) =>
        assert(message.contains("Comando no reconocido"))
        assertEquals(initial, GameState.initial)

  test("the compiler does not allow controlling a device from another room"):
    val result = ProgramCompiler.execute("ACTIVAR GENERADOR", GameState.initial)

    result match
      case GameResult.Success(_) => fail("A remote device command should not succeed.")
      case GameResult.Failure(message) =>
        assert(message.contains("no está disponible en esta habitación"))

  test("instruction values are distinct case-class commands"):
    assertEquals(Activate("VENTILADOR"), Activate("VENTILADOR"))
    assert(Activate("VENTILADOR") != Deactivate("VENTILADOR"))
