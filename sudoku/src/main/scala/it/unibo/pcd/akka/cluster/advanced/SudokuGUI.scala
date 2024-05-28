package it.unibo.pcd.akka.cluster.advanced

import javax.swing.{JFrame, JPanel, JTextField, JButton, SwingConstants, SwingUtilities}
import java.awt.{Color, Dimension, GridLayout, BorderLayout}
import java.awt.event.{ActionEvent, ActionListener, FocusAdapter, FocusEvent}
import scala.collection.mutable.Map as MutableMap
import javax.swing.JOptionPane

class SudokuGUI(val size: Int, player: PlayerActor):
  private val textFields = Array.ofDim[JTextField](9, 9)
  private val elementWidth = 40
  private val frame = JFrame()
  private val panel = SudokuPanel()
  private val checkButton = new JButton("Check Solution")
  frame.setSize(size * elementWidth, size * elementWidth + 50)  // Extra space for the button
  frame.setVisible(true)
  frame.setLocationRelativeTo(null)
  frame.setLayout(new BorderLayout())  // Use BorderLayout to add the button at the bottom
  frame.add(checkButton, BorderLayout.SOUTH)
  frame.add(panel, BorderLayout.CENTER)
  
  private val selectedNow = MutableMap[String, (Int, Int)]()

  def render(): Unit = SwingUtilities.invokeLater { () =>
    panel.updateGrid()
    panel.invalidate()
    panel.repaint()
  }

  def updateGUIFocus(id: String, row: Int, col: Int): Unit =
    if(selectedNow.contains(id)) then
      val pos = selectedNow(id)
      selectedNow.remove(id)
      if(!LazyList.from(selectedNow.values).contains(pos)) then  
        textFields(pos._1)(pos._2).setBackground(Color.WHITE)
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
        override def focusGained(e: FocusEvent): Unit =
          player.cellFocused(i, j)

        override def focusLost(e: FocusEvent): Unit = 
          var propose = textField.getText().toIntOption
          var prevVal = player.localGrid.get(i, j)
          if(propose.isEmpty || propose.get < 1 || propose.get > 9) then textFields(i)(j).setText(if prevVal != 0 then prevVal.toString() else "")
          if(propose.isDefined && (0 to 9).contains(propose.get)) then player.cellWritten(i, j, propose.get)
      })
      textFields(i)(j) = textField
      add(textField)
    }

    def updateGrid(): Unit = {
      for (i <- 0 until 9; j <- 0 until 9) {
        val value = player.localGrid.get(i, j)
        textFields(i)(j).setText(if (value != 0) value.toString else "")
      }
    }

  
  checkButton.addActionListener(new ActionListener {
    override def actionPerformed(e: ActionEvent): Unit = {
      if (SudokuGenerator.isValidSudoku(player.localGrid.grid)) {
        JOptionPane.showMessageDialog(frame, "You won!", "Sudoku", JOptionPane.INFORMATION_MESSAGE)
      } else {
        JOptionPane.showMessageDialog(frame, "The Sudoku is not valid.", "Sudoku", JOptionPane.ERROR_MESSAGE)
      }
    }
  })

  
