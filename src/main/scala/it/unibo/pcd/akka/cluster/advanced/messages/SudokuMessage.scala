package it.unibo.pcd.akka.cluster.advanced.messages

import akka.actor.typed.ActorRef
import it.unibo.pcd.akka.cluster.advanced.Grid
import scala.collection.mutable.Map as MutableMap

trait SudokuMessage
trait LobbyMessage extends SudokuMessage
trait PlayerMessage extends SudokuMessage

// Tutti le tipologie di messaggi che vengono utilizzati nel sistema.

/**
  * Lo manda l'attore giocatore all'attore lobby.
  */
case class FindGames(playerID: String, replyTo: ActorRef[PlayerMessage]) extends LobbyMessage
/**
  * Lo manda l'attore giocatore all'attore lobby.
  */
case class CreateGame(playerID: String, player: ActorRef[PlayerMessage]) extends LobbyMessage
/**
 * Lo manda l'attore giocatore all'attore lobby.
 */
case class EndGame(playerID: String) extends LobbyMessage

/**
  * La risposta al messaggio FindGames dell'attore della lobby al giocatore.
  */
case class Games(players: List[(String, ActorRef[PlayerMessage])]) extends PlayerMessage
/**
  * Messaggio al giocatore di guardare lo stato del suo host (usato per decidere la successione dell'host quando muore)
  */
object WatchHost extends PlayerMessage
/**
  * Messaggio di morte che la gui manda all'attore per morire.
  */
object Die extends PlayerMessage
/**
  * Messaggio che viene inviato ad un attore quando un attore che stava seguendo muore.
  */
case class PlayerDeath(playerID: String, player: ActorRef[PlayerMessage]) extends PlayerMessage, LobbyMessage

/**
  * Messaggio che viene mandato da un attore al creatore della partita per joinare.
  */
case class JoinGame(playerID: String, replyTo: ActorRef[PlayerMessage]) extends PlayerMessage
/** 
  * Messaggio di riposta a JoinGame con cui l'attore creatore(host) comunica la proprioa griglia e gli altri giocatori al nuovo partecipante.    
  */
case class GameInfo(grid: Grid, participants: MutableMap[String, ActorRef[PlayerMessage]]) extends PlayerMessage

/**
  * Messaggio che viene mandato dell'host per dire ai partecipanti che uno nuovo si è unito alla partita.
  */
case class NewParticipant(id: String, actor: ActorRef[PlayerMessage]) extends PlayerMessage
case class RemoveParticipant(id: String) extends PlayerMessage

/**
  * Messaggio che viene mandato dal giocatore all'host per avvisare che è stata fatta una mossa.
  */
case class SendMove(playerID: String, row: Int, col: Int, number: Int) extends PlayerMessage
/**
  * Messaggio che è mandato dell'host a tutti gli altri giocatori della nuova mossa.
  */
case class NewMove(row: Int, col: Int, number: Int, update: Int) extends PlayerMessage

/**
  * Messaggio che viene mandato dal giocatore all'host per avvisare che è stata fatto un nuovo focus.
  */
case class SendFocus(playerID: String, row: Int, col: Int) extends PlayerMessage
/**
  * Messaggio che è mandato dell'host a tutti gli altri giocatori per avvisarli del nuovo focus.
  */
case class CellFocus(playerID: String, row: Int, col: Int) extends PlayerMessage