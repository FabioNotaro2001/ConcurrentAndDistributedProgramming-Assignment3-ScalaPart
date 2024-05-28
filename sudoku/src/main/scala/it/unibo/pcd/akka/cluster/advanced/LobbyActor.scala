package it.unibo.pcd.akka.cluster.advanced

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import it.unibo.pcd.akka.cluster.advanced.messages.{CreateGame, JoinGame, SudokuMessage}
import scala.collection.mutable.Map as MutableMap
import messages.*
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}


private case class aListingResponse(listing: Receptionist.Listing)

object LobbyActor:
  val lobbyKey: ServiceKey[LobbyMessage | aListingResponse] = ServiceKey("lobby")

  def apply(): Behavior[LobbyMessage | aListingResponse] =
    Behaviors.setup(context => LobbyActor(context))

  private case class LobbyActor(override val context: ActorContext[LobbyMessage | aListingResponse]) extends AbstractBehavior[LobbyMessage | aListingResponse](context):
    private val players = MutableMap[String, ActorRef[PlayerMessage]]()

    private val listingResponseAdapter = context.messageAdapter[Receptionist.Listing](aListingResponse.apply)
    context.system.receptionist ! Receptionist.Register(lobbyKey, context.self)

    override def onMessage(message: LobbyMessage | aListingResponse): Behavior[LobbyMessage | aListingResponse] =
      println("ENTRATOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO " + message)
      message match
        case aListingResponse(contents) =>
          println("CONTENUTOOOOOOOOOOOOOOOOOOO LOBBYYYYYYYYYYYYYYYYYY" + contents)
          Behaviors.same
        case CreateGame(id, player) =>
          players.put(id, player)
          Behaviors.same
        case EndGame(id) =>
          players.remove(id)
          Behaviors.same
        case FindGames(receiver) =>
          receiver ! Games(players.values.toList)
          Behaviors.same