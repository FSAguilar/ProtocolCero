package protocolocero.ui.screens

import protocolocero.app.Message
import protocolocero.app.Model
import protocolocero.domain.game.Direction
import protocolocero.domain.game.ExplorationPanel
import protocolocero.domain.game.Ending
import protocolocero.domain.game.GameResult
import protocolocero.domain.game.GameRules
import protocolocero.domain.game.GameState
import protocolocero.domain.game.PuzzleId
import protocolocero.domain.game.Room
import protocolocero.domain.game.RoomDialogue
import protocolocero.domain.game.ShipMap
import protocolocero.domain.game.StoryAction
import protocolocero.domain.game.StoryChoice
import protocolocero.domain.game.StoryItem
import protocolocero.domain.game.StoryScenes
import protocolocero.domain.game.SystemId
import tyrian.Aria.*
import tyrian.Empty
import tyrian.Html
import tyrian.Html.*

object ExplorationVentana:
  def view(model: Model): Html[Message] =
    model.game.story.ending match
      case Some(ending) => EndingVentana.view(model, ending)
      case None =>
        ShipMap.findRoom(model.game.currentRoomId) match
          case Some(room) =>
            div(className := "exploration-layout")(
              div(className := "eyebrow")(
                span(className := "eyebrow__rule")(),
                span(text("EXPLORACIÓN DE UBICACIONES")),
                span(className := "eyebrow__code")(text(s"SECTOR ${room.id.toUpperCase}"))
              ),
              storyHud(model.game),
              if model.explorationPanel != ExplorationPanel.Closed then
                button(
                  onClick(Message.CloseExplorationPanel),
                  className := "exploration-modal-backdrop",
                  ariaLabel := "Cerrar panel al hacer clic fuera"
                )()
              else Empty,
              div(className := "exploration-grid")(
                shipMap(model.game),
                roomStage(room, model)
              ),
              button(
                onClick(Message.ReturnToMenu),
                className := "menu-button menu-button--back exploration-back"
              )(
                span(className := "menu-button__arrow")(text("←")),
                span(className := "menu-button__label")(text("Volver al menú"))
              )
            )
          case None =>
            div(className := "subscreen")(
              h1(className := "subscreen__title")(text("Error de navegación")),
              p(className := "subscreen__lead")(text("La habitación actual no existe en el mapa."))
            )

  private def storyHud(game: GameState): Html[Message] =
    div(className := "story-hud")(
      div(className := "story-hud__trust")(
        div(className := "story-hud__label")(
          span(text("CONFIANZA EN TESSA")),
          strong(text(s"${game.story.confidence}/10"))
        ),
        div(className := "story-hud__meter", ariaLabel := s"Confianza ${game.story.confidence} de 10")(
          div(
            className := "story-hud__meter-fill",
            style := s"width: ${game.story.confidence * 10}%"
          )()
        )
      ),
      div(className := "story-hud__systems")(
        (List(SystemId.Energy, SystemId.Kitchen, SystemId.Sound, SystemId.Communications)).map { system =>
          val active = game.story.hasSystem(system)
          val label =
            system match
              case SystemId.Energy => "ENERGÍA"
              case SystemId.Kitchen => "COCINA"
              case SystemId.Sound => "SONIDO"
              case SystemId.Communications => "COMMS"
          span(
            className := (if active then "story-hud__system story-hud__system--active"
                          else "story-hud__system")
          )(text(label))
        }
      ),
      div(className := "story-hud__inventory", ariaLabel := "Inventario y pistas")(
        span(className := "story-hud__label")(text("PISTAS")),
        if game.story.inventory.isEmpty then span(text("—"))
        else
          div(className := "story-hud__items")(
            game.story.inventory.map { item =>
              val label =
                item match
                  case StoryItem.AwsInvoice => "Factura AWS"
                  case StoryItem.PromptNote => "Nota del prompt"
                  case StoryItem.SecretBrookies => "Brookies"
                  case StoryItem.ManizehRabbit => "Conejo"
                  case StoryItem.ManizehRecord => "Registro Manizeh"
              span(className := "story-hud__item")(text(label))
            }
          )
      )
    )

  private def shipMap(game: GameState): Html[Message] =
    aside(className := "ship-map", ariaLabel := "Mapa de ubicaciones")(
      div(className := "ship-map__heading")(
        span(text("MAPA DE UBICACIONES")),
        span(className := "ship-map__count")(text(s"${game.visitedRoomIds.length}/${ShipMap.rooms.length}"))
      ),
      div(className := "ship-map__diagram")(
        img(
          src := "./assets/ship-map.svg",
          alt := "Ruta desde Grooming Salon hasta El Ágora, con atajos de regreso a Grooming Salon"
        ),
        div(className := "ship-map__rooms")(
          ShipMap.rooms.map { room =>
            val isCurrent = room.id == game.currentRoomId
            val isVisited = game.visitedRoomIds.contains(room.id)
            val stateClass =
              if isCurrent then "ship-map__room ship-map__room--current"
              else if isVisited then "ship-map__room ship-map__room--visited"
              else "ship-map__room"

            div(className := stateClass)(
              span(className := "ship-map__marker", ariaHidden := "true")(),
              span(className := "ship-map__room-name")(text(room.name))
            )
          }
        )
      ),
      div(className := "ship-map__legend")(
        span(className := "ship-map__legend-dot ship-map__legend-dot--current")(),
        span(text("POSICIÓN ACTUAL · RUTA CONECTADA"))
      )
    )

  private def roomStage(room: Room, model: Model): Html[Message] =
    div(className := "room-stage")(
      h1(className := "room-stage__title")(text(room.name)),
      div(
        className :=
          (if room.id == "grooming" then "room-stage__scene room-stage__scene--grooming"
           else "room-stage__scene")
      )(
        img(
          src := room.backgroundImage,
          alt := s"Escenario de ${room.name}",
          className := "room-stage__background"
        ),
        dialoguePortrait(model.roomDialogue),
        model.explorationPanel match
          case ExplorationPanel.Closed => Empty
          case ExplorationPanel.Data =>
            div(className := "room-stage__modal")(roomDataPanel(model.game))
          case ExplorationPanel.Terminal =>
            div(className := "room-stage__modal")(
              compilerPanel(model.game, model.programText, model.terminalFeedback)
            ),
        roomInteraction(room, model),
        panelTools(model.explorationPanel)
      ),
      div(className := "room-stage__navigation")(
        sideButton(room, model.game, Direction.Left, Message.MoveLeft),
        span(className := "room-stage__hint")(text("EXPLORA LOS SECTORES ADYACENTES")),
        sideButton(room, model.game, Direction.Right, Message.MoveRight),
        returnToStartButton(room)
      )
    )

  private def roomInteraction(room: Room, model: Model): Html[Message] =
    model.activePuzzle match
      case Some(puzzle) =>
        puzzleEditor(puzzle, model.programText, model.puzzleFeedback)
      case None =>
        model.roomDialogue match
          case Some(activeDialogue) =>
            activeDialogue.currentLine match
              case Some(line) =>
                button(
                  onClick(Message.AdvanceRoomDialogue),
                  className := "room-interaction room-interaction--dialogue",
                  ariaLabel := "Continuar diálogo"
                )(
                  div(className := "room-interaction__text")(
                    line.speaker match
                      case Some(name) => span(className := "room-interaction__speaker")(text(name))
                      case None => Empty,
                    p(text(line.text)),
                    span(className := "room-interaction__hint")(text("CLIC PARA CONTINUAR"))
                  )
                )
              case None => div[Message]()
          case None =>
            model.sceneChoices match
              case Some(choices) =>
                choiceButtons(choices, model.terminalFeedback)
              case None =>
                div(className := "room-interaction room-interaction--choices")(
                  StoryScenes.actionsFor(room.id).map { case (action, label) =>
                    button(
                      onClick(Message.SelectStoryAction(action)),
                      className := "room-interaction__choice"
                    )(text(label))
                  }
                )

  private def choiceButtons(
      choices: List[(StoryChoice, String)],
      feedback: Option[String]
  ): Html[Message] =
    div(className := "room-interaction room-interaction--choices")(
      div(className := "room-interaction__choice-list")(
        choices.map { case (choice, label) =>
          button(
            onClick(Message.SelectStoryChoice(choice)),
            className := "room-interaction__choice"
          )(text(label))
        }
      ),
      feedback match
        case Some(message) => p(className := "compiler-feedback")(text(message))
        case None => Empty
    )

  private def puzzleEditor(
      puzzle: PuzzleId,
      programText: String,
      feedback: Option[String]
  ): Html[Message] =
    puzzle match
      case PuzzleId.KitchenGenerator =>
        generatorEditor(programText, feedback)
      case PuzzleId.DjController =>
        djEditor(programText, feedback)
      case PuzzleId.PostgradRecords =>
        recordsEditor(programText, feedback)
      case PuzzleId.AgoraCommunications =>
        communicationsEditor(programText, feedback)

  private def generatorEditor(programText: String, feedback: Option[String]): Html[Message] =
    div(className := "puzzle-editor")(
      div(className := "puzzle-editor__heading")(
        div(
          h2(text("PROTOCOLO DE ENERGÍA")),
          p(text("Escribe el bucle while. Puedes usar espacios o tabulaciones; la energía debe llegar exactamente a 100."))
        ),
        button(
          onClick(Message.ClosePuzzle),
          className := "puzzle-editor__close",
          ariaLabel := "Cerrar editor"
        )(text("×"))
      ),
      pre(className := "puzzle-editor__scaffold")(
        text("val energia = 0\n"),
        textarea(
          className := "compiler-input puzzle-editor__input",
          value := programText,
          onInput(value => Message.PuzzleInputChanged(value)),
          ariaLabel := "Bucle while del protocolo de energía"
        )(text(programText)),
        text("\nif (energia == 100) {\n  activar(\"GENERADOR\")\n} else {\n  mostrarError()\n}")
      ),
      div(className := "puzzle-editor__actions")(
        button(
          onClick(Message.RunPuzzle),
          className := "menu-button menu-button--primary"
        )(
          span(className := "menu-button__label")(text("Ejecutar protocolo")),
          span(className := "menu-button__arrow", ariaHidden := "true")(text("▶"))
        ),
        feedback match
          case Some(message) => p(className := "compiler-feedback")(text(message))
          case None => Empty
      )
    )

  private def djEditor(programText: String, feedback: Option[String]): Html[Message] =
    div(className := "puzzle-editor")(
      div(className := "puzzle-editor__heading")(
        div(
          h2(text("CONTROLADOR DJ")),
          p(text("Escribe el while completo. canales.length da la cantidad de canales; canales(indice) lee el canal actual. Suma solo los positivos y aumenta indice en cada vuelta."))
        ),
        button(
          onClick(Message.ClosePuzzle),
          className := "puzzle-editor__close",
          ariaLabel := "Cerrar editor"
        )(text("×"))
      ),
      pre(className := "puzzle-editor__scaffold")(
        text("val canales = List(10, -1, 20, 0, 30, -5, 40)\nval indice = 0\nval volumen = 0\n"),
        textarea(
          className := "compiler-input puzzle-editor__input",
          value := programText,
          onInput(value => Message.PuzzleInputChanged(value)),
          ariaLabel := "Bucle while del controlador DJ"
        )(text(programText)),
        text("\nif (volumen == 100) {\n  activar(\"SONIDO\")\n} else {\n  mostrarError()\n}")
      ),
      div(className := "puzzle-editor__actions")(
        button(
          onClick(Message.RunPuzzle),
          className := "menu-button menu-button--primary"
        )(
          span(className := "menu-button__label")(text("Ejecutar protocolo")),
          span(className := "menu-button__arrow", ariaHidden := "true")(text("▶"))
        ),
        feedback match
          case Some(message) => p(className := "compiler-feedback")(text(message))
          case None => Empty
      )
    )

  private def communicationsEditor(programText: String, feedback: Option[String]): Html[Message] =
    div(className := "puzzle-editor")(
      div(className := "puzzle-editor__heading")(
        div(
          h2(text("RESTAURAR COMUNICACIONES")),
          p(text("Escribe un if/else: activa comunicaciones cuando energia sea mayor que 99; en caso contrario, déjalas apagadas."))
        ),
        button(
          onClick(Message.ClosePuzzle),
          className := "puzzle-editor__close",
          ariaLabel := "Cerrar editor"
        )(text("×"))
      ),
      pre(className := "puzzle-editor__scaffold")(
        text("val energia = 100\nval comunicaciones = 0\n"),
        textarea(
          className := "compiler-input puzzle-editor__input",
          value := programText,
          onInput(value => Message.PuzzleInputChanged(value)),
          ariaLabel := "Condicional del protocolo de comunicaciones"
        )(text(programText)),
        text("\n// El resultado se valida automáticamente")
      ),
      div(className := "puzzle-editor__actions")(
        button(
          onClick(Message.RunPuzzle),
          className := "menu-button menu-button--primary"
        )(
          span(className := "menu-button__label")(text("Probar comunicaciones")),
          span(className := "menu-button__arrow", ariaHidden := "true")(text("▶"))
        ),
        feedback match
          case Some(message) => p(className := "compiler-feedback")(text(message))
          case None => Empty
      )
    )

  private def recordsEditor(programText: String, feedback: Option[String]): Html[Message] =
    div(className := "puzzle-editor")(
      div(className := "puzzle-editor__heading")(
        div(
          h2(text("REGISTRO DE INVESTIGACIÓN")),
          p(text("Filtra los registros por MANIZEH y ordénalos para encontrar la pista."))
        ),
        button(
          onClick(Message.ClosePuzzle),
          className := "puzzle-editor__close",
          ariaLabel := "Cerrar editor"
        )(text("×"))
      ),
      textarea(
        className := "compiler-input puzzle-editor__input",
        value := programText,
        onInput(value => Message.PuzzleInputChanged(value)),
        ariaLabel := "Código del puzzle"
      )(text(programText)),
      div(className := "puzzle-editor__actions")(
        button(
          onClick(Message.RunPuzzle),
          className := "menu-button menu-button--primary"
        )(
          span(className := "menu-button__label")(text("Ejecutar protocolo")),
          span(className := "menu-button__arrow", ariaHidden := "true")(text("▶"))
        ),
        feedback match
          case Some(message) => p(className := "compiler-feedback")(text(message))
          case None => Empty
      )
    )

  private def dialoguePortrait(dialogue: Option[RoomDialogue]): Html[Message] =
    dialogue.flatMap(_.currentLine) match
      case Some(line) =>
        line.portrait match
          case Some(image) =>
            img(
              src := image,
              alt := line.speaker.getOrElse("Personaje"),
              className := "room-interaction__portrait"
            )
          case None => div[Message]()
      case None => div[Message]()

  private def panelTools(panel: ExplorationPanel): Html[Message] =
    nav(className := "exploration-tools", ariaLabel := "Paneles de habitación")(
      button(
        onClick(Message.ShowRoomData),
        className :=
          (if panel == ExplorationPanel.Data then "exploration-tools__button exploration-tools__button--active"
           else "exploration-tools__button"),
        ariaLabel := "Abrir Datos",
        ariaPressed := (panel == ExplorationPanel.Data).toString
      )(text("D")),
      span(className := "exploration-tools__hint")(text("EDITOR: OBJETOS"))
    )

  private def sideButton(
      room: Room,
      game: GameState,
      direction: Direction,
      message: Message
  ): Html[Message] =
    val destination =
      room.exits.find(_.direction == direction) match
        case Some(exit) => ShipMap.findRoom(exit.destinationId)
        case None => None
    val blockedMessage =
      GameRules.move(game, direction) match
        case GameResult.Success(_) => None
        case GameResult.Failure(reason) => Some(reason)
    val sideClass =
      direction match
        case Direction.Left => "explore-nav explore-nav--left"
        case Direction.Right => "explore-nav explore-nav--right"
        case Direction.ReturnToStart => "explore-nav explore-nav--return"

    destination match
      case Some(nextRoom) if blockedMessage.isEmpty =>
        button(
          onClick(message),
          className := sideClass,
          ariaLabel := s"Ir a ${nextRoom.name}"
        )(
          span(className := "explore-nav__direction", ariaHidden := "true")(
            text(if direction == Direction.Left then "←" else "→")
          ),
          span(className := "explore-nav__label")(text(nextRoom.name))
        )
      case Some(nextRoom) =>
        button(
          disabled,
          className := s"$sideClass explore-nav--disabled",
          ariaLabel := blockedMessage.getOrElse("Salida bloqueada")
        )(
          span(className := "explore-nav__direction", ariaHidden := "true")(text("🔒")),
          span(className := "explore-nav__label")(text(nextRoom.name))
        )
      case None =>
        button(
          disabled,
          className := s"$sideClass explore-nav--disabled",
          ariaLabel := "No hay salida en esta dirección"
        )(
          span(className := "explore-nav__direction", ariaHidden := "true")(text("—")),
          span(className := "explore-nav__label")(text("Sin salida"))
        )

  private def returnToStartButton(room: Room): Html[Message] =
    room.exits.find(_.direction == Direction.ReturnToStart) match
      case Some(_) =>
        button(
          onClick(Message.ReturnToStart),
          className := "explore-nav explore-nav--return",
          ariaLabel := "Volver directamente a Grooming Salon"
        )(
          span(className := "explore-nav__direction", ariaHidden := "true")(text("↶")),
          span(className := "explore-nav__label")(text("Volver al inicio"))
        )
      case None => div[Message]()

  private def roomDataPanel(game: GameState): Html[Message] =
    ShipMap.findRoom(game.currentRoomId) match
      case Some(room) =>
        val roomCharacters = game.characters.filter(_.roomId == room.id)
        val roomDevices = GameRules.availableDevices(game)

        div(className := "room-data-panel")(
          div(className := "room-data-panel__heading")(
            span(className := "room-data-panel__eyebrow")(text("DATOS DE HABITACIÓN")),
            h2(text(room.name))
          ),
          p(className := "room-data-panel__description")(text(room.description)),
          div(className := "room-panel__section-title")(text("SISTEMAS LOCALES")),
          div(className := "room-devices")(
            roomDevices.map { device =>
              val stateClass = if device.active then "device-status device-status--active" else "device-status"
              div(className := "device-card")(
                span(className := "device-card__name")(text(device.label)),
                span(className := stateClass)(text(if device.active then "ACTIVO" else "INACTIVO")),
                span(className := "device-card__command")(text(device.id))
              )
            }
          ),
          div(className := "room-characters")(
            roomCharacters.map(character =>
              div(className := "character-line")(
                span(className := "character-line__name")(text(character.name)),
                span(text(character.introduction))
              )
            )
          ),
          div(className := "visited-rooms")(
            span(className := "room-panel__section-title")(text("REGISTRO DE EXPLORACIÓN")),
            div(className := "visited-rooms__list")(
              game.log.map(entry => span(className := "visited-room")(text(entry)))
            )
          )
        )
      case None =>
        div(className := "room-data-panel")(
          p(text("No hay datos disponibles para esta habitación."))
        )

  private def compilerPanel(
      game: GameState,
      programText: String,
      feedback: Option[String]
  ): Html[Message] =
    div(className := "compiler-panel")(
      div(className := "compiler-panel__heading")(
        div(className := "compiler-panel__heading-copy")(
          h2(text("TERMINAL DE SISTEMAS")),
          span(text("COMPILADOR // V. 0.1"))
        ),
        img(
          src := "./assets/objetos/terminal.png",
          alt := "Terminal de programación",
          className := "compiler-panel__illustration"
        )
      ),
      p(className := "compiler-panel__help")(
        text("Una instrucción por línea. Usa ACTIVAR o DESACTIVAR seguido del código del sistema local.")
      ),
      div(className := "compiler-panel__commands")(
        GameRules.availableDevices(game).map(device =>
          span(className := "compiler-command")(text(device.id))
        )
      ),
      textarea(
        className := "compiler-input",
        placeholder := s"ACTIVAR ${GameRules.availableDevices(game).headOption.map(_.id).getOrElse("DISPOSITIVO")}",
        value := programText,
        onInput(value => Message.ProgramInputChanged(value))
      )(text(programText)),
      div(className := "compiler-panel__actions")(
        button(
          onClick(Message.RunProgram),
          className := "menu-button menu-button--primary compiler-run"
        )(
          span(className := "menu-button__index")(text(">_")),
          span(className := "menu-button__label")(text("Compilar y ejecutar"))
        ),
        feedback match
          case Some(message) => p[Message](className := "compiler-feedback")(text(message))
          case None => Empty
      ),
      div(className := "compiler-log")(
        span(className := "compiler-log__title")(text("REGISTRO")),
        div(className := "compiler-log__entries")(
          game.log.map(entry => p[Message](text(entry)))
        )
      )
    )
