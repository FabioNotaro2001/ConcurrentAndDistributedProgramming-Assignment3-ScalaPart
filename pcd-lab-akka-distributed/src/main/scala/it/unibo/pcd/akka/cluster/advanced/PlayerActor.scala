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
  var activePlayers: MutableMap[String, ActorRef[GameGrid | NewMove | CellFocus]] = null
  var localGUI: SudokuGUI = null

  private val listingResponseAdapter = context.messageAdapter[Receptionist.Listing](ListingResponse.apply)

  context.system.receptionist ! Receptionist.Find(lobbyKey, listingResponseAdapter)

  override def onMessage(message: PlayerMessageExtended): Behavior[PlayerMessageExtended] =

    context.log.info("RECEIVED MESSAGE: {}", message.toString)

    message match
      case ListingResponse(contents) =>
        lobby = contents.getServiceInstances(lobbyKey).iterator().next()
        lobby ! FindGames(context.self.narrow[Games])
        Behaviors.same

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
        Behaviors.same
        localGUI = SudokuGUI(9, this)
        localGUI.render()
        Behaviors.same

      case SendMove(id, row, col, n) =>
        localGrid.set(row, col, n)
        activePlayers.filter((pid, _) => id != pid).values.foreach(act => act ! NewMove(row, col, n))
        Behaviors.same

      case NewMove(row, col, n) =>
        localGrid.set(row, col, n)
        localGUI.render()
        Behaviors.same

      case CellFocus(id, row, col) =>
        if (hostPlayer == null) then // We are the host player.
          activePlayers.filter((pid, _) => id != pid).values.foreach(act => act ! CellFocus(id, row, col))
        localGUI.updateGUIFocus(id, row, col)
        Behaviors.same


  def cellFocused(row: Int, col: Int): Unit = activePlayers.values.foreach(act => act ! CellFocus(id, row, col))