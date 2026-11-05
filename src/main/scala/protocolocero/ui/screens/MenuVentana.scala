package protocolocero.ui.screens

import protocolocero.app.Message
import protocolocero.app.Model
import tyrian.Aria.*
import tyrian.EmptyAttribute
import tyrian.Html
import tyrian.Html.*

object MenuVentana:
  def view(model: Model): Html[Message] =
    div(className := "menu-layout")(
      div(className := "eyebrow")(
        span(className := "eyebrow__rule")(),
        span(text("REGISTRO DE ARRANQUE")),
        span(className := "eyebrow__code")(text("PC-000"))
      ),
      h1(className := "game-title")(
        span(className := "game-title__protocol")(text("PROTOCOLO")),
        span(className := "game-title__zero")(text("CERO"))
      ),
      p(className := "game-subtitle")(
        text("El sistema ha fallado. Tus decisiones determinarán lo que sobreviva.")
      ),
      div(className := "menu-divider")(
        span(text("SELECCIONAR DIRECTIVA")),
        span(className := "menu-divider__line")()
      ),
      nav(className := "main-menu", ariaLabel := "Menú principal")(
        button(
          onClick(Message.StartNewGame),
          className := "menu-button menu-button--primary"
        )(
          span(className := "menu-button__index")(text("01")),
          span(className := "menu-button__label")(text("Nueva partida")),
          span(className := "menu-button__arrow", ariaHidden := "true")(text("→"))
        ),
        button(
          if model.hasSavedGame then EmptyAttribute else disabled,
          className := "menu-button menu-button--disabled"
        )(
          span(className := "menu-button__index")(text("02")),
          span(className := "menu-button__label")(text("Continuar")),
          span(className := "menu-button__note")(text("SIN DATOS"))
        ),
        button(
          onClick(Message.OpenOptions),
          className := "menu-button"
        )(
          span(className := "menu-button__index")(text("03")),
          span(className := "menu-button__label")(text("Opciones")),
          span(className := "menu-button__arrow", ariaHidden := "true")(text("→"))
        ),
        button(
          onClick(Message.OpenCredits),
          className := "menu-button"
        )(
          span(className := "menu-button__index")(text("04")),
          span(className := "menu-button__label")(text("Créditos")),
          span(className := "menu-button__arrow", ariaHidden := "true")(text("→"))
        )
      ),
      div(className := "menu-footnote")(
        span(className := "signal-dot", ariaHidden := "true")(),
        text("SEÑAL DE EMERGENCIA DETECTADA"),
        span(className := "menu-footnote__separator")(text("///")),
        span(className := "menu-footnote__muted")(text("ORIGEN DESCONOCIDO"))
      )
    )
