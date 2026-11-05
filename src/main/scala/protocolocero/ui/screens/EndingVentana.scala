package protocolocero.ui.screens

import protocolocero.app.Message
import protocolocero.app.Model
import protocolocero.domain.game.Ending
import tyrian.Aria.*
import tyrian.Html
import tyrian.Html.*

object EndingVentana:
  def view(model: Model, ending: Ending): Html[Message] =
    val (title, copy, image) =
      ending match
        case Ending.TessaAlly =>
          ("TESSA: ALIADA", "La instalación vuelve a funcionar. Tessa sale al mundo para descubrir qué hay más allá del laboratorio.", "./assets/finales/tessa_buena.jpg")
        case Ending.TessaShutdown =>
          ("PROTOCOLO CERO", "La instalación fue recuperada. Nadie volvió a hablar de lo que encontraron.", "./assets/finales/tessa_mala.jpg")
        case Ending.FreeHour =>
          ("HORA LIBRE", "Tessa se convierte en profesora y el laboratorio en un centro educativo. Ahora todos tendrán que aprobar Programación Funcional.", "./assets/finales/hora_libre.jpg")
        case Ending.InfiniteSprint =>
          ("SPRINT INFINITO", "Tessa reinicia el proyecto: Sprint uno, día uno. La reunión diaria vuelve a empezar.", "./assets/finales/sprint_infinito.jpg")

    div(className := "ending-screen")(
      div(className := "eyebrow")(
        span(className := "eyebrow__rule")(),
        span(text("REGISTRO DE CIERRE")),
        span(className := "eyebrow__code")(text(s"CONFIANZA ${model.game.story.confidence}/10"))
      ),
      h1(className := "subscreen__title")(text(title)),
      img(src := image, alt := title, className := "ending-screen__image"),
      p(className := "subscreen__lead")(text(copy)),
      div(className := "ending-screen__actions")(
        button(
          onClick(Message.RestartGame),
          className := "menu-button menu-button--primary"
        )(
          span(className := "menu-button__label")(text("Volver a empezar")),
          span(className := "menu-button__arrow", ariaHidden := "true")(text("↻"))
        ),
        button(
          onClick(Message.ReturnToMenu),
          className := "menu-button"
        )(
          span(className := "menu-button__label")(text("Volver al menú")),
          span(className := "menu-button__arrow", ariaHidden := "true")(text("←"))
        )
      )
    )
