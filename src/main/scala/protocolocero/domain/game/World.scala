package protocolocero.domain.game

sealed trait Direction
object Direction:
  case object Left extends Direction
  case object Right extends Direction
  case object ReturnToStart extends Direction

final case class Exit(direction: Direction, destinationId: String)

final case class Room(
    id: String,
    name: String,
    description: String,
    exits: List[Exit],
    deviceIds: List[String],
    backgroundImage: String
)

object ShipMap:
  val rooms: List[Room] =
    List(
      Room(
        "grooming",
        "Grooming Salon",
        "Un salón equipado con productos, espejos y asientos para atender a sus clientes.",
        List(Exit(Direction.Right, "cocina")),
        List("VENTILADOR"),
        "./assets/fondos/grooming_salon.jpg"
      ),
      Room(
        "cocina",
        "Cocina Brookies",
        "Una cocina de trabajo con hornos, mesas de preparación y una zona de cultivo.",
        List(
          Exit(Direction.Left, "grooming"),
          Exit(Direction.Right, "malegria"),
          Exit(Direction.ReturnToStart, "grooming")
        ),
        List("BOMBA"),
        "./assets/fondos/cocina_de_brookies.jpg"
      ),
      Room(
        "malegria",
        "Malegría",
        "Un bar con una barra, estantes de bebidas y una pista iluminada.",
        List(
          Exit(Direction.Left, "cocina"),
          Exit(Direction.Right, "postgrado"),
          Exit(Direction.ReturnToStart, "grooming")
        ),
        List("PUERTA"),
        "./assets/fondos/malegria.jpg"
      ),
      Room(
        "postgrado",
        "Postgrado",
        "Un aula de postgrado con mesas, material de estudio y una proyección central.",
        List(
          Exit(Direction.Left, "malegria"),
          Exit(Direction.Right, "agora"),
          Exit(Direction.ReturnToStart, "grooming")
        ),
        List("CONSOLA"),
        "./assets/fondos/postgrado.jpg"
      ),
      Room(
        "agora",
        "El Ágora",
        "Un espacio central de encuentro rodeado de consolas, estanterías y pantallas.",
        List(Exit(Direction.Left, "postgrado")),
        List("GENERADOR", "LUZ"),
        "./assets/fondos/agora.jpg"
      )
    )

  def findRoom(id: String): Option[Room] =
    rooms.find(room => room.id == id)
