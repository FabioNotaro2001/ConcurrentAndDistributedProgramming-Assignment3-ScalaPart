package it.unibo.pcd.akka.cluster.advanced
 
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import it.unibo.pcd.akka.cluster.advanced.LobbyActor.{LobbyActor, lobbyKey}
import messages.*
import it.unibo.pcd.akka.cluster.advanced.messages.{FindGames, SudokuMessage}
import scala.collection.mutable.Map as MutableMap
import akka.actor.typed.Terminated
import akka.actor.typed.Signal
import it.unibo.pcd.akka.cluster.advanced.Root.player
 
type PlayerMessageExtended = PlayerMessage | PListingResponse

object PlayerActor:
  def apply(id: String, onActorCreated: (PlayerActor) => Unit): Behavior[PlayerMessageExtended] =
    Behaviors.setup(
      context => 
        val act = PlayerActor(context, id)
        onActorCreated(act) // Utilizzato per creare il menù.
        act
      )
 
private case class PListingResponse(listing: Receptionist.Listing) // Wrapper per i messaggi inviati dal receptionist.
 
case class PlayerActor(override val context: ActorContext[PlayerMessageExtended], id: String) extends AbstractBehavior[PlayerMessageExtended](context):
  var localGrid: Grid = null
  var lobby: ActorRef[LobbyMessage] = null
  var hostPlayer: ActorRef[PlayerMessage] = null
  var hostPlayerId: String = ""
  var activePlayers: MutableMap[String, ActorRef[PlayerMessage]] = MutableMap()
  var localGUI: SudokuGUI = null

  var availableGames: MutableMap[String, ActorRef[PlayerMessage]] = MutableMap()
  var onGamesUpdated: (List[(String, ActorRef[PlayerMessage])] => Unit) = _ => () // Serve per aggiornare il menu.

  // Serve per trovare la lobby attraverso il receptionist. La lobby in precedenza si è registrata, 
  // quindi il receptionist quando qualcuno richiede gli attori, gli restituisce gli attori lobby.
  context.spawnAnonymous {
    Behaviors.setup[Receptionist.Listing] {
      inner =>
        context.system.receptionist ! Receptionist.Subscribe(lobbyKey, inner.self)
        Behaviors.receiveMessage {
          case listing if listing.getServiceInstances(lobbyKey).isEmpty =>
            inner.log.info("No lobby found")
            Behaviors.same
          case listing =>
            inner.log.info("Lobby found")
            lobby = listing.getServiceInstances(lobbyKey).iterator().next()
            lobby ! FindGames(id, context.self)
            Behaviors.same
        }
    }
  }

  override def onMessage(message: PlayerMessageExtended): Behavior[PlayerMessageExtended] =
    message match
      // Ci arriva il messaggio games con la lista dei giocatori che hanno creato delle partite.
      case Games(players) =>
        availableGames = MutableMap.from(players)
        onGamesUpdated(players)
        Behaviors.same
 
      // Messaggio mandato per partecipare alla partita, esso lo può ricevere solo l'host.
      case JoinGame(id, replyTo) =>
        activePlayers.values.foreach(_ ! NewParticipant(id, replyTo)) // Avvisa tutti i partecipanti che ce ne è uno nuovo.
        context.watchWith(replyTo, PlayerDeath(id, replyTo)) // Si mette in ascolto se gli arriva un segnale che il nuovo giocatore è morto.
        replyTo ! GameInfo(localGrid.getCopy(), activePlayers.clone()) // Restituisce le info di gioco.
        Behaviors.same

      // Messaggio che riceve un partecipante con le info della partita che ha fatto richiesta dall'host.
      case GameInfo(globalGrid, otherPlayers) =>
        localGrid = globalGrid
        localGUI = SudokuGUI(9, this)
        localGUI.render()
        activePlayers = otherPlayers
        activePlayers.put(id, context.self)
        Behaviors.same
 
      case NewParticipant(id, actor) =>
        activePlayers.put(id, actor)
        Behaviors.same

      case RemoveParticipant(id) =>
        activePlayers.remove(id)
        localGUI.removeFocus(id)
        Behaviors.same

      // La può ricevere solo l'host.
      case SendMove(id, row, col, n) =>
        if localGrid.get(row, col) != n
        then
          val upd = localGrid.set(row, col, n)
          activePlayers.filterNot(act => act._1 == this.id).values.foreach(act => act ! NewMove(row, col, n, upd)) // Manda a tutti i giocatori attivi, tranne a se stesso.
          localGUI.render()
        Behaviors.same
 
      case NewMove(row, col, n, upd) =>
        if localGrid.specialSet(row, col, n, upd)
        then
          localGUI.render()
        else
          println("NO UPDATE")
        Behaviors.same
 
      // Lo riceve solo l'host.
      case SendFocus(id, row, col) =>
        activePlayers.values.foreach(act => act ! CellFocus(id, row, col)) // L'host manda a tutti gli altri giocatori.
        Behaviors.same

      // Ricevuto da tutti gli altri giocatori.
      case CellFocus(id, row, col) =>
        localGUI.updateGUIFocus(id, row, col)
        Behaviors.same
      
      // Messaggio che viene inviato quando un attore muore.
      case PlayerDeath(id, actor) => 
        // Se è un morto un partecipante
        if activePlayers.contains(id) then
          activePlayers.remove(id)
          activePlayers.values.foreach(_ ! RemoveParticipant(id)) // Avvisiamo tutti di rimuovere il riferiemnto al partecipante morto.

        // Se è morto l'host
        if (hostPlayerId == id) 
        then 
          activePlayers.min(Ordering.by((s, _) => s)) match // Il nuovo host diventa il player con l'id più piccolo.
            // Se il minimo è se stesso diventa lui l'host. Poi avvisa la lobby che prende lui il possesso della partita, poi si mette ad ascoltare la morta dei giocatori di tutti quanti.
            case (pid, _) if pid == this.id => 
              hostPlayer = null
              hostPlayerId = ""
              lobby ! CreateGame(this.id, context.self)
              activePlayers.filterNot(act => act._1 == this.id).foreach((actId, act) => context.watchWith(act, PlayerDeath(actId, act)))
            // Se lui non è il partecipante con l'id più piccolo.
            case (pid, act) => 
              hostPlayer = act
              hostPlayerId = pid
              context.watchWith(hostPlayer, PlayerDeath(hostPlayerId, hostPlayer)) // Si mette ad ascoltare questo nuovo attore come host.
        Behaviors.same

      // Lo utilizziamo nel menù della gui.
      case WatchHost => 
        context.watchWith(hostPlayer, PlayerDeath(hostPlayerId, hostPlayer))
        Behaviors.same

      // Messaggio quando l'attore stesso muore.
      case Die => Behaviors.stopped

      case _ => Behaviors.same
 
  // MMetodi chiamati dalla GUI
  def startNewGame(): Unit =
    localGrid = Grid(9, 9)
    localGrid.fill()
    lobby ! CreateGame(id, context.self)
    activePlayers.put(id, context.self)
    localGUI = SudokuGUI(9, this)
    localGUI.render()

  def joinGame(playerId: String): Unit = 
    hostPlayer = availableGames(playerId)
    hostPlayerId = playerId
    context.self ! WatchHost
    hostPlayer ! JoinGame(id, context.self)

  // E' il setter per il callBack, usato per le partite attive. Perchè il menù è creato per un player actor specifico.
  def setOnGamesUpdated(callback: (List[(String, ActorRef[PlayerMessage])]) => Unit): Unit =
    onGamesUpdated = callback

  def cellFocused(row: Int, col: Int): Unit = 
    if(hostPlayer == null) then 
      context.self ! SendFocus(id, row, col)
    else
      hostPlayer ! SendFocus(id, row, col)

  def cellWritten(row: Int, col: Int, value: Int): Unit = 
    if(hostPlayer == null) then 
      context.self ! SendMove(id, row, col, value)
    else
      hostPlayer ! SendMove(id, row, col, value)

    