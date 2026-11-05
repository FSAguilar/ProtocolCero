package protocolocero.ui.screens

import protocolocero.app.Message
import protocolocero.app.Model
import protocolocero.domain.Screen
import tyrian.Aria.*
import tyrian.Html
import tyrian.Html.*

object MainVentanas:
  def view(model: Model): Html[Message] =
    val motionClass = if model.preferences.reducedMotion then " reduced-motion" else ""
    val screenClass = if model.screen == Screen.Exploration then " game-shell--exploration" else ""

    main(
      className := s"game-shell$motionClass$screenClass",
      role := "main"
    )(
      div(className := "ambient ambient--left")(),
      div(className := "ambient ambient--right")(),
      div(className := "noise-layer")(),
      header(className := "system-bar")(
        div(className := "system-brand")(
          span(className := "brand-mark", ariaHidden := "true")(
            span(className := "brand-mark__core")()
          ),
          div(
            span(className := "system-brand__name")(text("NEXUS / 09")),
            span(className := "system-brand__sub")(text("COMPLEJO DE INVESTIGACIÓN"))
          )
        ),
        div(className := "system-status")(
          span(className := "status-indicator", ariaHidden := "true")(),
          span(text("ESTADO DE LA INSTALACIÓN")),
          strong(text("CRÍTICO"))
        )
      ),
      div(className := "content-frame")(
        aside(className := "side-index", ariaHidden := "true")(
          span(className := "side-index__line")(),
          span(text("SECTOR 07")),
          span(text("2140.11.08")),
          span(className := "side-index__line")()
        ),
        section(className := "screen-content")(
          screenContent(model)
        ),
        aside(className := "side-readout", ariaHidden := "true")(
          span(text("SYS.LOG")),
          span(text("ERR_0x09")),
          span(className := "side-readout__red")(text("SIGNAL LOST"))
        )
      ),
      footer(className := "system-footer")(
        span(text("PROTOCOLO DE CONTINGENCIA // CERO")),
        span(className := "footer-coordinates")(text("N 42° 18' 06.4\"  /  E 12° 33' 41.8\"")),
        span(text("CONEXIÓN EXTERNA: ")),
        strong(text("SIN RESPUESTA"))
      )
    )

  private def screenContent(model: Model): Html[Message] =
    model.screen match
      case Screen.MainMenu =>
        MenuVentana.view(model)

      case Screen.Exploration =>
        ExplorationVentana.view(model)

      case Screen.Options =>
        OpcionesVentana.view(model)

      case Screen.Credits =>
        CreditosVentana.view
