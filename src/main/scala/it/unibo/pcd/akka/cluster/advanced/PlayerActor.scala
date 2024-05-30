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
 
type PlayerMessageExtended = PlayerMessage | PListingResponse | Terminated
 
object PlayerActor:
  def apply(id: String, onActorCreated: (PlayerActor) => Unit): Behavior[PlayerMessageExtended] =
    Behaviors.setup(
      context => 
        val act = PlayerActor(context, id)
        onActorCreated(act)
        act
      )
 
private case class PListingResponse(listing: Receptionist.Listing)
 
case class PlayerActor(override val context: ActorContext[PlayerMessageExtended], id: String) extends AbstractBehavior[PlayerMessageExtended](context):
  var localGrid: Grid = null
  var lobby: ActorRef[LobbyMessage] = null
  var hostPlayer: ActorRef[PlayerMessage] = null
  var hostPlayerId: String = ""
  var activePlayers: MutableMap[String, ActorRef[PlayerMessage]] = MutableMap()
  var localGUI: SudokuGUI = null

  var availableGames: MutableMap[String, ActorRef[PlayerMessage]] = MutableMap()
  var onGamesUpdated: (List[(String, ActorRef[PlayerMessage])] => Unit) = _ => ()

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
 
    context.log.info("RECEIVED MESSAGE: {}", message.toString)
 
    message match
      case Games(players) =>
        availableGames = MutableMap.from(players)
        onGamesUpdated(players)
        Behaviors.same
 
      case JoinGame(id, replyTo) =>
        activePlayers.values.foreach(_ ! NewParticipant(id, replyTo))
        context.watchWith(replyTo, PlayerDeath(id, replyTo))
        replyTo ! GameInfo(localGrid.getCopy(), activePlayers.clone())
        Behaviors.same

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
        Behaviors.same

      case SendMove(id, row, col, n) =>
        if localGrid.get(row, col) != n
        then
          val upd = localGrid.set(row, col, n)
          activePlayers.filterNot(act => act._1 == this.id).values.foreach(act => act ! NewMove(row, col, n, upd))
          localGUI.render()
        Behaviors.same
 
      case NewMove(row, col, n, upd) =>
        if localGrid.specialSet(row, col, n, upd)
        then
          localGUI.render()
        else
          println("NO UPDATE")
        Behaviors.same
 
      case SendFocus(id, row, col) =>
        activePlayers.values.foreach(act => act ! CellFocus(id, row, col))
        Behaviors.same

      case CellFocus(id, row, col) =>
        localGUI.updateGUIFocus(id, row, col)
        Behaviors.same
      
      case PlayerDeath(id, actor) => 
        if activePlayers.contains(id) then
          activePlayers.remove(id)
          activePlayers.filterNot(_._1 == id).values.foreach(_ ! RemoveParticipant(id))

        if (hostPlayerId == id) 
        then 
          activePlayers.min(Ordering.by((s, _) => s)) match // The new host is the player with the "smallest" id
            case (pid, _) if pid == this.id => 
              hostPlayer = null
              hostPlayerId = ""
              lobby ! CreateGame(this.id, context.self)
            case (pid, act) => 
              hostPlayer = act
              hostPlayerId = pid
              context.watchWith(hostPlayer, PlayerDeath(hostPlayerId, hostPlayer))
        Behaviors.same

      case WatchHost => 
        context.watchWith(hostPlayer, PlayerDeath(hostPlayerId, hostPlayer))
        Behaviors.same

      case Die => Behaviors.stopped

      case _ => Behaviors.same
 
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

    