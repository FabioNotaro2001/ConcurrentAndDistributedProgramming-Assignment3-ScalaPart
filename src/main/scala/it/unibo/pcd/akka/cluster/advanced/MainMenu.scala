package it.unibo.pcd.akka.cluster.advanced

import javax.swing.*
import java.awt.*
import scala.collection.immutable.List
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import akka.actor.typed.Behavior
import akka.actor.typed.ActorRef
import it.unibo.pcd.akka.cluster.startupWithRole
import it.unibo.pcd.akka.cluster.advanced.messages.PlayerMessage

object SimpleGameGUI extends App {
  private val onPlayerActorCreated = (actor: PlayerActor) => 
    val frame = new JFrame("Game GUI")
    frame.setSize(400, 200)
    frame.setLayout(new BorderLayout())

    val topPanel = new JPanel()
    val createGameButton = new JButton("Create Game")
    topPanel.add(createGameButton)
    frame.add(topPanel, BorderLayout.NORTH)

    val centerPanel = new JPanel()
    centerPanel.setLayout(new FlowLayout())

    val dropdownMenu = new JComboBox[String]()
    centerPanel.add(dropdownMenu)
    
    actor.setOnGamesUpdated:
      (games: List[ActorRef[PlayerMessage]]) =>
        if (!frame.isVisible())
            frame.setVisible(true)

        dropdownMenu.removeAllItems()
        // actor.availablegames.map(_.toString()).toArray
        for 
          g <- games
          name = g.toString()
        do dropdownMenu.addItem(name)
        dropdownMenu.invalidate()

    val joinButton = new JButton("Join")
    centerPanel.add(joinButton)

    frame.add(centerPanel, BorderLayout.CENTER)

    createGameButton.addActionListener(new ActionListener {
      override def actionPerformed(e: ActionEvent): Unit = {
        actor.startNewGame()
        frame.setVisible(false)
      }
    })

    joinButton.addActionListener(new ActionListener {
      override def actionPerformed(e: ActionEvent): Unit = {
        val selectedChoice = dropdownMenu.getSelectedIndex()
        actor.joinGame(selectedChoice)
        frame.setVisible(false)
      }
    })

  startupWithRole(Roles.frontend, 0)(Root.player("P" + System.currentTimeMillis().hashCode().toString, onPlayerActorCreated))
}