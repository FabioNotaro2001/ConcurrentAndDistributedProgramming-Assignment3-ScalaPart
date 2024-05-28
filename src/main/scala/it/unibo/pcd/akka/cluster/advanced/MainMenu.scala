package it.unibo.pcd.akka.cluster.advanced

import javax.swing.*
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

object SimpleGameGUI extends App {
  val frame = new JFrame("Game GUI")
  frame.setSize(400, 200)
  frame.setLayout(new BorderLayout())

  val topPanel = new JPanel()
  val createGameButton = new JButton("Create Game")
  topPanel.add(createGameButton)
  frame.add(topPanel, BorderLayout.NORTH)

  val centerPanel = new JPanel()
  centerPanel.setLayout(new FlowLayout())

  val choices = Array("a", "b", "c")
  val dropdownMenu = new JComboBox[String](choices)
  centerPanel.add(dropdownMenu)

  val joinButton = new JButton("Join")
  centerPanel.add(joinButton)

  frame.add(centerPanel, BorderLayout.CENTER)

  createGameButton.addActionListener(new ActionListener {
    override def actionPerformed(e: ActionEvent): Unit = {
      JOptionPane.showMessageDialog(frame, "Game Created!")
      frame.setVisible(false)
    }
  })

  joinButton.addActionListener(new ActionListener {
    override def actionPerformed(e: ActionEvent): Unit = {
      val selectedChoice = dropdownMenu.getSelectedItem.toString
      JOptionPane.showMessageDialog(frame, s"Joined game: $selectedChoice")
      frame.setVisible(false)
    }
  })
  frame.setVisible(true)
}