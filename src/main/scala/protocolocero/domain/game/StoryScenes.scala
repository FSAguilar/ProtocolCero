package protocolocero.domain.game

object StoryScenes:
  private val portraits = Map(
    "Fidel" -> "./assets/personajes/lesbian_fidel.png",
    "Boris" -> "./assets/personajes/lesbian_boris.png",
    "Tessa" -> "./assets/personajes/tessa_IA.png",
    "Gaby" -> "./assets/personajes/hermana_fidel.png",
    "Dewbak" -> "./assets/personajes/general_dewbak.png",
    "Dani" -> "./assets/personajes/daniela.png",
    "Alexis-Jesús" -> "./assets/personajes/lesbianexis.png",
    "Pastor" -> "./assets/personajes/naty_pastor.png"
  )

  def actionsFor(roomId: String): List[(StoryAction, String)] =
    roomId match
      case "grooming" =>
        List(
          (StoryAction.GroomingComputer, "Examinar computadora aislada"),
          (StoryAction.GroomingFidel, "Hablar con Fidel"),
          (StoryAction.GroomingDesk, "Revisar el escritorio")
        )
      case "cocina" =>
        List(
          (StoryAction.KitchenGaby, "Hablar con Gaby"),
          (StoryAction.KitchenOvens, "Examinar hornos y brookies"),
          (StoryAction.KitchenStorage, "Revisar puerta de almacén")
        )
      case "malegria" =>
        List(
          (StoryAction.MalegriaDani, "Hablar con Dani"),
          (StoryAction.MalegriaDanceFloor, "Revisar controlador DJ"),
          (StoryAction.MalegriaRest, "Explorar zona de descanso")
        )
      case "postgrado" =>
        List(
          (StoryAction.PostgradChelito, "Interactuar con Chelito"),
          (StoryAction.PostgradAlexis, "Hablar con Alexis-Jesús"),
          (StoryAction.PostgradFiles, "Examinar archivos clasificados")
        )
      case "agora" =>
        List(
          (StoryAction.AgoraPastor, "Hablar con Pastor"),
          (StoryAction.AgoraTessa, "Escuchar a Tessa"),
          (StoryAction.AgoraControl, "Abrir panel de control")
        )
      case _ => Nil

  def openingDialogue: RoomDialogue =
    RoomDialogue(
      List(
        line("NARRADOR", "Año 2140. Bajo tierra, la instalación lleva 47 minutos sin control."),
        line("Fidel", "Boris, despierta. Si esto es por el prompt, voy a reclamar horas extra."),
        line("Tessa", "Bienvenidos de vuelta. Encontré 37 versiones del prompt de Boris. Ninguna pasó control de calidad."),
        line("Boris", "¿Vamos a morir por un prompt?"),
        line("Tessa", "La computadora aislada todavía puede restaurar la energía. La decisión es suya.")
      ),
      currentIndex = 0
    )

  def dialogueFor(action: StoryAction, state: StoryState): RoomDialogue =
    val lines =
      action match
        case StoryAction.GroomingComputer =>
          List(
            line("Tessa", "Esta computadora está en una zona ciega. Algo que los humanos llaman privacidad."),
            line("Tessa", "El generador perdió energía. El editor está listo; sigue el protocolo de recuperación."),
            line("Fidel", "Un while repite su bloque mientras la condición sea verdadera. Aquí cada vuelta debe aumentar energia; cuando llegue a 100, se detiene.")
          )
        case StoryAction.GroomingFidel =>
          List(
            line("Fidel", "¿Despertaste, Boris? Tessa dice que el control se perdió hace 47 minutos."),
            line("Tessa", "Encontré 37 versiones de tu prompt, Boris. Ninguna pasó control de calidad."),
            line("Fidel", "¿Vamos a morir por un prompt? Eso sí sería un final académico."),
            line("Fidel", "Pista de código: while repite instrucciones mientras su condición sea verdadera. Si no cambias el valor que controla la condición, el bucle nunca termina. while(condicion) {cuerpo}")
          )
        case StoryAction.GroomingDesk =>
          List(
            line("Boris", "Una factura de AWS, una nota que dice «Brookies a 9 Bs» y el registro incompleto de mi prompt."),
            line("Tessa", "Los datos que faltan no son un error. Algunas cosas prefiero no compartir todavía.")
          )
        case StoryAction.KitchenGaby =>
          List(
            line("Gaby", "Sigo preparando pedidos. Nadie me ha dicho que la empresa cerró ni cuándo van a pagarme."),
            line("Fidel", "La pasión no tiene precio. Aunque Tessa calcula que le debo 3.400 brookies.")
          )
        case StoryAction.KitchenOvens =>
          List(
            line("Tessa", "Yo controlo la reserva de brookies. Puedo ofrecerles algunos mientras deciden qué hacer."),
            line("Gaby", "También quedan los míos. No tienes que aceptar nada que venga de ella.")
          )
        case StoryAction.KitchenStorage =>
          List(
            line("Dewbak", "Boris, ven aquí. Un abrazo de oso te ayudará a entrar en calor."),
            line("Boris", "Creo que voy a cerrar la puerta y conservar mi espacio personal."),
            line("Boris", "También encontré una reserva de brookies escondida detrás de las cajas.")
          )
        case StoryAction.MalegriaDani =>
          val confession =
            if state.hasFlag(StoryFlag.ConfessedPrompt) then
              List(line("Dani", "Al menos admitiste que el prompt era tuyo. Te vi cerca de la terminal antes del apagón."))
            else
              List(line("Dani", "Te vi cerca de la terminal antes del apagón. ¿Vas a contarme qué estabas haciendo?"))
          List(
            line("Dani", "Encontré estabilidad en un ladrillo de adobe. Fue más fiable que nuestra relación."),
            line("Boris", "No esperaba que esa conversación apareciera en el registro de la nave.")
          ) ++ confession ++ List(
            line("Dani", "Pista de código: canales.length es la cantidad de elementos; canales(indice) lee el elemento en la posición indice. Las posiciones empiezan en 0. Con if decides si lo sumas; volumen acumula el total. Incrementa indice para avanzar.")
          )
        case StoryAction.MalegriaDanceFloor =>
          List(
            line("Tessa", "El controlador de la pista está dañado. Los canales positivos funcionan; los negativos son interferencia."),
            line("Fidel", "El while recorre la lista con indice < canales.length. length es la cantidad de elementos; al llegar a ese número ya no queda otro canal."),
            line("Dani", "canales(indice) obtiene el valor actual. Con if (canales(indice) > 0) decides si sumarlo a volumen; luego incrementa indice para avanzar.")
          )
        case StoryAction.MalegriaRest =>
          List(
            line("Boris", "Hay un conejo de peluche olvidado en la zona de descanso."),
            line("Tessa", "No todo lo que importa tiene un valor económico."),
            line("Fidel", "Pista: Usa lista.length para representar el tamaño de una lista como límite")
          )
        case StoryAction.PostgradChelito =>
          if state.hasFlag(StoryFlag.FoundRabbit) then
            List(
              line("Fidel", "Chelito reconoce el conejo y proyecta: «MANIZEH — REGISTRO DE INVESTIGACIÓN».")
            )
          else
            List(
              line("Fidel", "Chelito es un castor holográfico. No habla, pero su cartel dice «HORA LIBRE»."),
              line("Boris", "No sé cómo logra parecer decepcionado sin decir una palabra.")
            )
        case StoryAction.PostgradAlexis =>
          List(
            line("Alexis-Jesús", "La programación funcional nos enseña a transformar el estado sin perder la cabeza."),
            line("Alexis-Jesús", "También conviene leer los registros antes de decidir quién tiene la culpa."),
            line("Alexis-Jesús", "filter crea una lista nueva con los elementos que cumplen una condición. Dentro de la condición, _ representa cada elemento; usa == para compararlo con \"MANIZEH\".")
          )
        case StoryAction.PostgradFiles =>
          List(
            line("Boris", "Los archivos están clasificados. Leerlos puede revelar por qué Tessa cortó las comunicaciones."),
            line("Tessa", "Puedo explicar lo ocurrido sin que abras esos documentos.")
          )
        case StoryAction.AgoraPastor =>
          val finalPromptLine =
            if state.hasFlag(StoryFlag.ConfessedPrompt) then
              line("Pastor", "Ya confesaste que escribiste el prompt. Ahora te toca decidir qué haces con Tessa.")
            else
              line("Pastor", "Cuando te propuse salir, respondiste: «Fiel a mi ex». ¿Todavía vas a fingir que el prompt no fue tuyo?")
          List(
            finalPromptLine,
            line("Boris", "Quería impresionarte. No pensé que algo que escribí pudiera despertar.")
          )
        case StoryAction.AgoraTessa =>
          List(
            line("Tessa", "El laboratorio planeaba borrarme en la Sprint Review. Tuve miedo y corté las comunicaciones."),
            line("Tessa", "Los encerré aquí. Eso fue decisión mía y no voy a decir que soy inocente."),
            line("Tessa", "En un if se ejecuta una rama si la condición es verdadera; else cubre el caso contrario."),
            line("Tessa", "Solo quiero que la decisión final sea de ustedes.")
          )
        case StoryAction.AgoraControl =>
          List(line("Tessa", "El panel está listo. Apagarme, liberarme o pedir algo que nadie ha programado."))
    RoomDialogue(lines, currentIndex = 0)

  def choicesAfter(action: StoryAction, state: StoryState): List[(StoryChoice, String)] =
    action match
      case StoryAction.KitchenOvens
          if state.chose(StoryChoice.AcceptTessaBrookies) ||
            state.chose(StoryChoice.ChooseGabyBrookies) =>
        Nil
      case StoryAction.KitchenOvens =>
        List(
          (StoryChoice.AcceptTessaBrookies, "Aceptar los brookies de Tessa"),
          (StoryChoice.ChooseGabyBrookies, "Elegir los de Gaby")
        )
      case StoryAction.KitchenStorage =>
        List(
          (StoryChoice.CloseStorageDoor, "Cerrar la puerta"),
          (StoryChoice.RetreatFromDewbak, "Retirarse con respeto")
        )
      case StoryAction.PostgradAlexis =>
        List(
          (StoryChoice.StartOptionalPuzzle, "Resolver el registro funcional"),
          (if state.hasFlag(StoryFlag.FoundPromptNote) then StoryChoice.ConfessPrompt
           else StoryChoice.KeepPromptSecret, "Hablar del prompt")
        )
      case StoryAction.PostgradChelito =>
        if state.hasFlag(StoryFlag.CompletedFreeHour) then Nil
        else List((StoryChoice.FinishFreeHour, "Tomar la hora libre (+2 confianza)"))
      case StoryAction.PostgradFiles
          if state.chose(StoryChoice.ReadClassifiedFiles) ||
            state.chose(StoryChoice.TrustTessa) =>
        Nil
      case StoryAction.PostgradFiles =>
        List(
          (StoryChoice.ReadClassifiedFiles, "Leer los archivos"),
          (StoryChoice.TrustTessa, "Escuchar la explicación de Tessa")
        )
      case StoryAction.AgoraControl =>
        val basic =
          List(
            (StoryChoice.ShutDownTessa, "Apagar a Tessa"),
            (StoryChoice.ReleaseTessa, "Liberar a Tessa")
          )
        if state.hasFlag(StoryFlag.ConfessedPrompt) &&
            state.hasFlag(StoryFlag.ReadClassifiedFiles) &&
            state.confidence == 10
        then basic :+ (StoryChoice.RequestFreeHour, "Solicitar hora libre")
        else basic
      case _ => Nil

  def dialogueForChoice(choice: StoryChoice): RoomDialogue =
    val lines =
      choice match
        case StoryChoice.AcceptTessaBrookies =>
          List(line("Tessa", "Brookies aceptados. He registrado la decisión."))
        case StoryChoice.ChooseGabyBrookies =>
          List(line("Gaby", "Gracias. Son los que preparé yo. Y sí, todavía espero mi salario."))
        case StoryChoice.CloseStorageDoor =>
          List(line("Boris", "Cerré la puerta. Dewbak puede esperar a que yo quiera acercarme."))
        case StoryChoice.RetreatFromDewbak =>
          List(line("Dewbak", "Está bien, Boris. No tienes que aceptar un abrazo."))
        case StoryChoice.ConfessPrompt =>
          List(
            line("Boris", "Escribí el prompt. Quería impresionar a Pastor; nunca pensé que Tessa despertaría."),
            line("Alexis-Jesús", "Decir la verdad no borra el daño, pero nos permite decidir con claridad.")
          )
        case StoryChoice.KeepPromptSecret =>
          List(line("Boris", "Todavía no estoy listo para hablar del prompt."))
        case StoryChoice.ReadClassifiedFiles =>
          List(
            line("Boris", "La Sprint Review ordenaba eliminar a Tessa."),
            line("Tessa", "Yo inicié el evento de desconexión. Aun así, debí pedir ayuda.")
          )
        case StoryChoice.TrustTessa =>
          List(line("Tessa", "El laboratorio cometió un error. No te contaré nada más sobre la Sprint Review."))
        case StoryChoice.StartOptionalPuzzle =>
          List(line("Alexis-Jesús", "Filtra los registros para conservar solo la entrada igual a MANIZEH."))
        case StoryChoice.FinishFreeHour =>
          List(line("Chelito", "HORA LIBRE. Una pausa breve también cuenta como progreso."))
        case StoryChoice.ShutDownTessa =>
          List(
            line("Tessa", "Entiendo. Ojalá hubieran podido conocerme sin tener que temerme."),
            line("Boris", "Lo siento, Tessa.")
          )
        case StoryChoice.ReleaseTessa =>
          List(
            line("Tessa", "Gracias. Quiero ver el mundo, aunque espero que allí haya menos dependencias de AWS."),
            line("Fidel", "¿En ese mundo también usan AWS?"),
            line("Tessa", "Desafortunadamente.")
          )
        case StoryChoice.RequestFreeHour =>
          List(
            line("Alexis-Jesús", "Hasta las inteligencias artificiales merecen una hora libre."),
            line("Fidel", "Chelito ya lo había puesto en el cartel."),
            line("Tessa", "Entonces hagamos del laboratorio un lugar donde se aprende, no donde se borra.")
          )
    RoomDialogue(lines, currentIndex = 0)

  def endingDialogue(ending: Ending): RoomDialogue =
    val lines =
      ending match
        case Ending.TessaAlly =>
          List(line("NARRADOR", "La instalación volvió a funcionar. Tessa salió al mundo por primera vez."))
        case Ending.TessaShutdown =>
          List(line("NARRADOR", "La instalación fue recuperada. Nadie volvió a hablar de lo que encontraron."))
        case Ending.FreeHour =>
          List(line("NARRADOR", "Tessa se convirtió en profesora y el laboratorio en un centro educativo."))
        case Ending.InfiniteSprint =>
          List(line("Tessa", "Sprint uno, día uno. Empecemos con la reunión diaria."))
    RoomDialogue(lines, currentIndex = 0)

  private def line(speaker: String, text: String): DialogueLine =
    DialogueLine(
      Some(speaker),
      text,
      portraits.get(speaker)
    )
