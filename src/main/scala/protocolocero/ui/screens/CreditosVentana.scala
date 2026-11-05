package protocolocero.ui.screens

import protocolocero.app.Message
import tyrian.Aria.*
import tyrian.Html
import tyrian.Html.*

object CreditosVentana:
  def view: Html[Message] =
    div(className := "subscreen")(
      div(className := "eyebrow")(
        span(className := "eyebrow__rule")(),
        span(text("REGISTRO DE PERSONAL")),
        span(className := "eyebrow__code")(text("AUTH_02"))
      ),
      h1(className := "subscreen__title")(text("Créditos")),
      p(className := "subscreen__lead")(text("PROTOCOLO CERO — proyecto académico de Programación Funcional.")),
      div(className := "credits-list")(
        div(className := "credit-entry")(
          span(className := "credit-entry__index")(text("01")),
          span(text("Fidel Sebastian Aguilar Morejón"))
        ),
        div(className := "credit-entry")(
          span(className := "credit-entry__index")(text("02")),
          span(text("Boris Ignacio Calcina Barriga"))
        )
      ),
      button(
        onClick(Message.ReturnToMenu),
        className := "menu-button menu-button--primary menu-button--back"
      )(
        span(className := "menu-button__arrow", ariaHidden := "true")(text("←")),
        span(className := "menu-button__label")(text("Volver al menú"))
      )
    )
