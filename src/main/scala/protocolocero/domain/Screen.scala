package protocolocero.domain

enum Screen derives CanEqual:
  case MainMenu, Exploration, Options, Credits

final case class Preferences(reducedMotion: Boolean) derives CanEqual

object Preferences:
  val default: Preferences = Preferences(reducedMotion = false)
