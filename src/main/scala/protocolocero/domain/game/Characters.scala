package protocolocero.domain.game

trait Identified:
  def id: String
  def name: String

abstract class Character extends Identified:
  def roomId: String
  def introduction: String

final case class Engineer(
    id: String,
    name: String,
    roomId: String,
    specialty: String
) extends Character:
  def introduction: String =
    s"$name — ingeniero de $specialty. Desorientado, pero consciente."

final case class StationAI(
    id: String,
    name: String,
    roomId: String
) extends Character:
  def introduction: String =
    s"$name — inteligencia artificial de la estación. Su señal es inestable."

final case class CrewMember(
    id: String,
    name: String,
    roomId: String,
    role: String
) extends Character:
  def introduction: String =
    s"$name — $role."
