package it.unibo.pcd.akka.cluster.advanced.messages

import akka.actor.typed.ActorRef
import it.unibo.pcd.akka.cluster.advanced.Grid

trait SudokuMessage
trait LobbyMessage extends SudokuMessage
trait PlayerMessage extends SudokuMessage

case class CreateGame(playerID: String, player: ActorRef[PlayerMessage]) extends LobbyMessage
case class EndGame(playerID: String) extends LobbyMessage
case class FindGames(replyTo: ActorRef[Games]) extends LobbyMessage
case class JoinGame(playerID: String, replyTo: ActorRef[GameGrid | NewMove | CellFocus]) extends PlayerMessage
case class GameGrid(grid: Grid) extends PlayerMessage
case class SendMove(playerID: String, row: Int, col: Int, number: Int) extends PlayerMessage
case class NewMove(row: Int, col: Int, number: Int, update: Int) extends PlayerMessage
case class Games(players: List[ActorRef[PlayerMessage]]) extends PlayerMessage
case class SendFocus(playerID: String, row: Int, col: Int) extends PlayerMessage
case class CellFocus(playerID: String, row: Int, col: Int) extends PlayerMessage