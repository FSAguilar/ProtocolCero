package protocolocero.ui.screens

import protocolocero.app.Message
import protocolocero.app.Model
import tyrian.Aria.*
import tyrian.Html
import tyrian.Html.*

object OpcionesVentana:
  def view(model: Model): Html[Message] =
    div(className := "subscreen")(
      div(className := "eyebrow")(
        span(className := "eyebrow__rule")(),
        span(text("CONFIGURACIÓN DEL SISTEMA")),
        span(className := "eyebrow__code")(text("CFG_01"))
      ),
      h1(className := "subscreen__title")(text("Opciones")),
      p(className := "subscreen__lead")(text("Ajusta la interfaz de la estación.")),
      div(className := "option-row")(
        div(
          span(className := "option-row__name")(text("Reducir animaciones")),
          span(className := "option-row__description")(text("Desactiva los efectos visuales en movimiento."))
        ),
        button(
          onClick(Message.ToggleReducedMotion),
          className := "option-toggle",
          ariaLabel := "Alternar animaciones reducidas",
          ariaPressed := model.preferences.reducedMotion.toString
        )(
          span(className := "option-toggle__track")(
            span(className := "option-toggle__thumb")()
          ),
          span(className := "option-toggle__state")(
            text(if model.preferences.reducedMotion then "ACTIVADO" else "DESACTIVADO")
          )
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
