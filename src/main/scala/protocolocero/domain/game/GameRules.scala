package protocolocero.domain.game

object GameRules:
  import Direction.*
  import GameResult.*

  def move(state: GameState, direction: Direction): GameResult[GameState] =
    ShipMap.findRoom(state.currentRoomId) match
      case None =>
        Failure("La habitación actual no existe en el mapa.")
      case Some(room) =>
        room.exits.find(exit => exit.direction == direction) match
          case None =>
            Failure("No hay una salida en esa dirección.")
          case Some(exit) =>
            movementRequirement(state, direction) match
              case Some(message) =>
                Failure(message)
              case None =>
                ShipMap.findRoom(exit.destinationId) match
                  case None =>
                    Failure("La salida conduce a una habitación inexistente.")
                  case Some(destination) =>
                    val visited =
                      if state.visitedRoomIds.contains(destination.id) then state.visitedRoomIds
                      else state.visitedRoomIds :+ destination.id
                    val updatedCharacters =
                      state.characters.map {
                        case engineer: Engineer if engineer.id == "player" =>
                          engineer.copy(roomId = destination.id)
                        case character =>
                          character
                      }

                    Success(
                      state.copy(
                        currentRoomId = destination.id,
                        visitedRoomIds = visited,
                        characters = updatedCharacters,
                        log = state.log :+ s"Entraste en ${destination.name}."
                      )
                    )

  private def movementRequirement(state: GameState, direction: Direction): Option[String] =
    (state.currentRoomId, direction) match
      case ("grooming", Direction.Right) if !state.story.solved(PuzzleId.KitchenGenerator) =>
        Some("La cocina sigue sin energía. Resuelve el protocolo del generador primero.")
      case ("cocina", Direction.Right) if !hasBrookiesDecision(state.story) =>
        Some("Antes de avanzar, decide qué brookies aceptar en los hornos.")
      case ("malegria", Direction.Right)
          if !state.story.solved(PuzzleId.DjController) && state.story.confidence > 2 =>
        Some("El paso a Postgrado está bloqueado. Repara el controlador DJ o explora la historia.")
      case _ =>
        None

  private def hasBrookiesDecision(story: StoryState): Boolean =
    story.chose(StoryChoice.AcceptTessaBrookies) ||
      story.chose(StoryChoice.ChooseGabyBrookies)

  def availableDevices(state: GameState): List[Device] =
    state.devices.filter(device => device.roomId == state.currentRoomId)

  def executeInstruction(state: GameState, instruction: Instruction): GameResult[GameState] =
    val (deviceId, shouldBeActive, status) =
      instruction match
        case Activate(id) => (id, true, "ACTIVO")
        case Deactivate(id) => (id, false, "INACTIVO")

    state.devices.find(device => device.id == deviceId) match
      case None =>
        Failure(s"Dispositivo desconocido: $deviceId.")
      case Some(device) if device.roomId != state.currentRoomId =>
        Failure(s"$deviceId no está disponible en esta habitación.")
      case Some(device) if device.active == shouldBeActive =>
        Success(state.copy(log = state.log :+ s"${device.label} ya estaba ${if device.active then "activo" else "inactivo"}."))
      case Some(device) =>
        val updatedDevices =
          state.devices.map { current =>
            if current.id == deviceId then current.copy(active = shouldBeActive)
            else current
          }
        Success(
          state.copy(
            devices = updatedDevices,
            log = state.log :+ s"${device.label}: estado cambiado a $status."
          )
        )
