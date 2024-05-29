package it.unibo.pcd.akka.cluster.advanced.messages

import akka.actor.typed.ActorRef
import it.unibo.pcd.akka.cluster.advanced.Grid
import scala.collection.mutable.Map as MutableMap

trait SudokuMessage
trait LobbyMessage extends SudokuMessage
trait PlayerMessage extends SudokuMessage

case class FindGames(playerID: String, replyTo: ActorRef[PlayerMessage]) extends LobbyMessage
case class CreateGame(playerID: String, player: ActorRef[PlayerMessage]) extends LobbyMessage
case class EndGame(playerID: String) extends LobbyMessage

case class Games(players: List[ActorRef[PlayerMessage]]) extends PlayerMessage
object WatchHost extends PlayerMessage
object Die extends PlayerMessage
case class PlayerDeath(playerID: String, player: ActorRef[PlayerMessage]) extends PlayerMessage, LobbyMessage

case class JoinGame(playerID: String, replyTo: ActorRef[PlayerMessage]) extends PlayerMessage
case class GameInfo(grid: Grid, participants: MutableMap[String, ActorRef[PlayerMessage]]) extends PlayerMessage

case class NewParticipant(id: String, actor: ActorRef[PlayerMessage]) extends PlayerMessage
case class RemoveParticipant(id: String) extends PlayerMessage

case class SendMove(playerID: String, row: Int, col: Int, number: Int) extends PlayerMessage
case class NewMove(row: Int, col: Int, number: Int, update: Int) extends PlayerMessage

case class SendFocus(playerID: String, row: Int, col: Int) extends PlayerMessage
case class CellFocus(playerID: String, row: Int, col: Int) extends PlayerMessage