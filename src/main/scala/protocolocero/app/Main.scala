package protocolocero.app

import indigoengine.shared.collections.Batch
import protocolocero.ui.screens.MainVentanas
import tyrian.*
import tyrian.HtmlFragment
import tyrian.ui.theme.Theme

object Main extends App[Unit, Model]:
  given Theme = Theme.default

  def router: Location => GlobalMsg =
    Routing.none(Message.NoOp)

  def init(flags: Map[String, String]): Result[Model] =
    Result(Model.initial)

  def update(model: Model): GlobalMsg => Result[Model] =
    case message: Message =>
      Result(Update(model, message))
    case _ =>
      Result(model)

  def view(model: Model): HtmlRoot =
    HtmlRoot(HtmlFragment(MainVentanas.view(model)))

  def watchers(model: Model): Batch[Watcher] =
    Batch.empty

  def extensions(flags: Map[String, String], model: Model): Set[Extension[Unit, HtmlFragment]] =
    Set.empty

  def main(args: Array[String]): Unit =
    launch("protocolo-cero-app")
