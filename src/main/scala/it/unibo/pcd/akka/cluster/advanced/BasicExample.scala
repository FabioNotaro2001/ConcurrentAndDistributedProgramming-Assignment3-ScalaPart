package it.unibo.pcd.akka.cluster.advanced
 
import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.*
import akka.cluster.typed.Cluster
import com.typesafe.config.ConfigFactory
import it.unibo.pcd.akka.cluster.seeds
 
import concurrent.duration.{DurationInt, FiniteDuration}
import scala.language.postfixOps
import scala.util.Random
import it.unibo.pcd.akka.cluster.*
import AntsRender.*
// create ants and then an frontend to visualise them.
// It uses role to decide what kind of ActorSystem the node should deploy
object Root:
  def apply(playerID: String): Behavior[Nothing] = Behaviors.setup { ctx =>
    val cluster = Cluster(ctx.system)
    if (cluster.selfMember.hasRole(Roles.frontend)) then
      ctx.spawnAnonymous(PlayerActor.apply(playerID))
    else
      ctx.spawnAnonymous(LobbyActor.apply())
    Behaviors.empty
  }
 
@main def startLobby: Unit =
  startupWithRole(Roles.backend, 2551)(Root(""))
 
@main def startPlayer1: Unit =
  startupWithRole(Roles.frontend, 0)(Root("P1"))
 
@main def startPlayer2: Unit =
  startupWithRole(Roles.frontend, 0)(Root("P2"))