package it.unibo.pcd.akka.cluster.advanced
 
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import it.unibo.pcd.akka.cluster.advanced.LobbyActor.{LobbyActor, lobbyKey}
import messages.*
import it.unibo.pcd.akka.cluster.advanced.messages.{FindGames, SudokuMessage}
import scala.collection.mutable.Map as MutableMap
 
type PlayerMessageExtended = PlayerMessage | ListingResponse
 
object PlayerActor:
  def apply(id: String): Behavior[PlayerMessageExtended] =
    Behaviors.setup(context => PlayerActor(context, id))
 
private case class ListingResponse(listing: Receptionist.Listing)
 
case class PlayerActor(override val context: ActorContext[PlayerMessageExtended], id: String) extends AbstractBehavior[PlayerMessageExtended](context):
  var localGrid: Grid = null
  var lobby: ActorRef[LobbyMessage] = null
  var hostPlayer: ActorRef[PlayerMessage] = null
  var activePlayers: MutableMap[String, ActorRef[GameGrid | NewMove | CellFocus]] = MutableMap(id -> context.self)
  var localGUI: SudokuGUI = null

  context.spawnAnonymous {
    Behaviors.setup[Receptionist.Listing] {
      inner =>
        context.system.receptionist ! Receptionist.Find(lobbyKey, inner.self)
        Behaviors.receiveMessage {
          case listing if listing.getServiceInstances(lobbyKey).isEmpty =>
            inner.log.info("No lobby found")
            Behaviors.same
          case listing =>
            inner.log.info("Lobby found")
            lobby = listing.getServiceInstances(lobbyKey).iterator().next()
            lobby ! FindGames(context.self.narrow[Games])
            Behaviors.same
        }
    }
  }

  override def onMessage(message: PlayerMessageExtended): Behavior[PlayerMessageExtended] =
 
    context.log.info("RECEIVED MESSAGE: {}", message.toString)
 
    message match
      case Games(players) =>
        if (players.isEmpty) then
          localGrid = Grid(9, 9)
          lobby ! CreateGame(id, context.self)
          localGUI = SudokuGUI(9, this)
          localGUI.render()
        else
          hostPlayer = players.head
          hostPlayer ! JoinGame(id, context.self.narrow[GameGrid | NewMove | CellFocus])
        Behaviors.same
 
      case JoinGame(id, replyTo) =>
        activePlayers.put(id, replyTo)
        replyTo ! GameGrid(localGrid.getCopy())
        Behaviors.same
 
      case GameGrid(globalGrid) =>
        localGrid = globalGrid
        localGUI = SudokuGUI(9, this)
        localGUI.render()
        Behaviors.same
 
      case SendMove(id, row, col, n) =>
        if(localGrid.get(row, col) != n) then
          activePlayers.values.foreach(act => act ! NewMove(row, col, n))
        Behaviors.same
 
      case NewMove(row, col, n) =>
        localGrid.set(row, col, n)
        localGUI.render()
        Behaviors.same
 
      case SendFocus(id, row, col) =>
        activePlayers.values.foreach(act => act ! CellFocus(id, row, col))
        Behaviors.same

      case CellFocus(id, row, col) =>
        localGUI.updateGUIFocus(id, row, col)
        Behaviors.same
 
 
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