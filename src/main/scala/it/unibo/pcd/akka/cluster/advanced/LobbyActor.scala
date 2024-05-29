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
    Behaviors.setup(context => LobbyActor(context))

  private case class LobbyActor(override val context: ActorContext[LobbyMessageExtended]) extends AbstractBehavior[LobbyMessageExtended](context):
    private val activeGames = MutableMap[String, ActorRef[PlayerMessage]]()
    private val activePlayers = MutableMap[String, ActorRef[PlayerMessage]]()

    private val listingResponseAdapter = context.messageAdapter[Receptionist.Listing](LListingResponse.apply)
    context.system.receptionist ! Receptionist.Register(lobbyKey, context.self)

    override def onMessage(message: LobbyMessageExtended): Behavior[LobbyMessageExtended] =
      println("ENTRATOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO " + message)
      message match
        case LListingResponse(contents) =>
          println("CONTENUTOOOOOOOOOOOOOOOOOOO LOBBYYYYYYYYYYYYYYYYYY" + contents)
          Behaviors.same
        
        case FindGames(id, player) =>
          activePlayers.put(id, player)
          player ! Games(activeGames.values.toList)
          Behaviors.same

        case CreateGame(id, player) =>
          activeGames.put(id, player)
          activePlayers.filterNot(_._2 == player).foreach(_._2 ! Games(activeGames.values.toList))
          context.watchWith(player, PlayerDeath(id, player))
          Behaviors.same
        
        case EndGame(id) =>
          activeGames.remove(id)
          activePlayers.foreach(_._2 ! Games(activeGames.values.toList))  
          Behaviors.same
        
        case PlayerDeath(id, actor) =>
          activeGames.get(id) match
            case Some(_) => 
              activePlayers.remove(id)
              activeGames.remove(id) match
                case _: Some[Any] => activePlayers.foreach(_._2 ! Games(activeGames.values.toList))  
                case _ => ()
            case _ => ()
          Behaviors.same

        case _ => Behaviors.same