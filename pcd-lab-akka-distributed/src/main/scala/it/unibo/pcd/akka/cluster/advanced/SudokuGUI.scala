package it.unibo.pcd.akka.cluster.advanced

import javax.swing.{JFrame, JPanel, JTextField, SwingConstants, SwingUtilities}
import java.awt.{Color, Dimension, GridLayout}
import java.awt.event.{ActionEvent, ActionListener, FocusAdapter, FocusEvent}
import scala.collection.mutable.Map as MutableMap

class SudokuGUI(val size: Int, player: PlayerActor):
  private val elementWidth = 40
  private val frame = JFrame()
  private val panel = SudokuPanel()
  frame.setSize(size * elementWidth, size * elementWidth)
  frame.setVisible(true)
  frame.setLocationRelativeTo(null)
  frame.add(panel)
  
  private val textFields = Array.ofDim[JTextField](9, 9)
  private val selectedNow = MutableMap[String, (Int, Int)]()

  def render(): Unit = SwingUtilities.invokeLater { () =>
    panel.updateGrid()
    panel.invalidate()
    panel.repaint()
  }

  def updateGUIFocus(id: String, row: Int, col: Int): Unit =
    if(selectedNow.contains(id)) then
      val pos = selectedNow(id)
      textFields(pos._1)(pos._2).setBackground(Color.WHITE)
      selectedNow.remove(id)
    selectedNow.put(id, (row, col))
    textFields(row)(col).setBackground(Color.RED)

  private class SudokuPanel() extends JPanel:

    setLayout(new GridLayout(9, 9))
    setPreferredSize(new Dimension(SudokuGUI.this.size * elementWidth, SudokuGUI.this.size * elementWidth))

    for (i <- 0 until 9; j <- 0 until 9) {
      val textField = new JTextField()
      val gridCell = player.localGrid.get(i, j)
      if(gridCell != 0) then
        textField.setText(gridCell.toString)
      textField.setHorizontalAlignment(SwingConstants.CENTER)
      textField.setPreferredSize(new Dimension(elementWidth, elementWidth))
      textField.addFocusListener(new FocusAdapter {
        override def focusGained(e: FocusEvent): Unit = {
          player.cellFocused(i, j)
        }
      })
      textFields(i)(j) = textField
      add(textField)
    }

    def updateGrid(): Unit = {
      for (i <- 0 until 9; j <- 0 until 9) {
        val value = player.localGrid.get(i, j)
        textFields(i)(j).setText(if (value != 0) value.toString else "")
//        selectedNow.foreach { color =>
//          textFields(i)(j).setBackground(color)
//        }
      }
    }