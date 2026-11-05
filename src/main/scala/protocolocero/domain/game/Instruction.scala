package protocolocero.domain.game

sealed trait Instruction

final case class Activate(deviceId: String) extends Instruction
final case class Deactivate(deviceId: String) extends Instruction

sealed trait GameResult[+A]
object GameResult:
  final case class Success[A](value: A) extends GameResult[A]
  final case class Failure(message: String) extends GameResult[Nothing]
