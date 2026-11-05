package protocolocero.domain.game

final case class Device(
    id: String,
    roomId: String,
    label: String,
    active: Boolean
)

final case class GameState(
    currentRoomId: String,
    visitedRoomIds: List[String],
    devices: List[Device],
    characters: List[Character],
    log: List[String],
    story: StoryState
)

object GameState:
  val initial: GameState =
    GameState(
      currentRoomId = "grooming",
      visitedRoomIds = List("grooming"),
      devices = List(
        Device("VENTILADOR", "grooming", "Ventilador", active = true),
        Device("GENERADOR", "agora", "Generador auxiliar", active = false),
        Device("BOMBA", "cocina", "Bomba de agua", active = true),
        Device("CONSOLA", "postgrado", "Consola del aula", active = true),
        Device("PUERTA", "malegria", "Puerta del bar", active = false),
        Device("LUZ", "agora", "Luces del ágora", active = true)
      ),
      characters = List(
        Engineer("player", "Boris", "grooming", "sistemas"),
        CrewMember("fidel", "Fidel", "grooming", "estudiante e ingeniero"),
        CrewMember("gaby", "Gaby", "cocina", "trabajadora de la cocina"),
        CrewMember("dewbak", "Dewbak", "cocina", "superviviente de la instalación"),
        CrewMember("dani", "Dani", "malegria", "operadora de la sala"),
        CrewMember("chelito", "Chelito", "postgrado", "castor holográfico"),
        CrewMember("alexis", "Alexis-Jesús", "postgrado", "estudiante y mentor"),
        CrewMember("pastor", "Pastor", "agora", "superviviente"),
        StationAI("tessa", "Tessa", "agora")
      ),
      log = List("Año 2140. La instalación lleva 47 minutos sin control."),
      story = StoryState.initial
    )
