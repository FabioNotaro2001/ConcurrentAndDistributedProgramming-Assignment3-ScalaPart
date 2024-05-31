package it.unibo.pcd.akka.cluster.advanced

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import it.unibo.pcd.akka.cluster.advanced.messages.{CreateGame, JoinGame, SudokuMessage}
import scala.collection.mutable.Map as MutableMap
import messages.*
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.Terminated

type LobbyMessageExtended = LobbyMessage | LListingResponse

private case class LListingResponse(listing: Receptionist.Listing)

object LobbyActor:
  val lobbyKey: ServiceKey[LobbyMessageExtended] = ServiceKey("lobby")

  def apply(): Behavior[LobbyMessageExtended] =
    Behaviors.setup(context => LobbyActor(context)) // Crea lobby actor e restituisce il behavior corrispondente.

  private case class LobbyActor(override val context: ActorContext[LobbyMessageExtended]) extends AbstractBehavior[LobbyMessageExtended](context):
    private val activeGames = MutableMap[String, ActorRef[PlayerMessage]]() // Mappa dei sudoku attivi dall'id del creatore all'attore creatore.
    private val activePlayers = MutableMap[String, ActorRef[PlayerMessage]]() // Mappa dall'id del giocaroe all'attore del giocatore stesso.

    private val listingResponseAdapter = context.messageAdapter[Receptionist.Listing](LListingResponse.apply) // Receptionist, serve a trasformare i messaggi di tipo Receptionist.Listing a un tipo definito da te LListingResponse.
    context.system.receptionist ! Receptionist.Register(lobbyKey, context.self) // Manda un messaggio al receptionist dicendo di registrare il lobby actor su quella lobby key.

    // Metodo che definisce cosa fare quando un lobbyActor riceve un messaggio.
    override def onMessage(message: LobbyMessageExtended): Behavior[LobbyMessageExtended] =
      message match
        // Id e player sono quelli del richiedente.
        case FindGames(id, player) =>
          activePlayers.put(id, player)
          player ! Games(activeGames.toList) // al player mandiamo il massaggio dei player attivi.
          context.watchWith(player, PlayerDeath(id, player)) // Il lobbyactor si mette in ascolto per un possibile segnale di morte del giocatore, al fine di sapere quale giocatore è morto.
          Behaviors.same

        case CreateGame(id, player) =>
          activeGames.put(id, player)
          activePlayers.filterNot(_._2 == player).foreach(_._2 ! Games(activeGames.toList)) // Avvisa tutti gli altri giocatori diversi dal creatore per aggiornare la lista di games.
          Behaviors.same
        
        case PlayerDeath(id, actor) =>
          // Elimina il giocatore dagli attiva, prova ad eliminare il giocatore dalle partite attive e se ci riesce avvisa del cambio
          // tutti i giocatori attivi. Potrebbe non riuscirci se il giocatore non ha una partita attiva.
          activePlayers.remove(id)
          activeGames.remove(id) match
            case _: Some[Any] => activePlayers.foreach(_._2 ! Games(activeGames.toList))  
            case _ => ()
          Behaviors.same

        case _ => Behaviors.same