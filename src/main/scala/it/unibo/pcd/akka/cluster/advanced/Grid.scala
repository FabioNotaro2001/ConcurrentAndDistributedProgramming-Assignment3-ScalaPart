package it.unibo.pcd.akka.cluster.advanced

import com.fasterxml.jackson.annotation.JsonManagedReference
import java.beans.Transient
import it.unibo.pcd.akka.cluster.advanced.SudokuGenerator.fillGrid

// Wrapper per un array di array di interi.
case class Grid(private val rows: Int, private val cols: Int):
  val grid = Array.ofDim[Int](rows, cols)
  private val updates = Array.fill[Int](rows, cols)(0)

  def fill(): Unit =
    fillGrid(grid)

  def get(row: Int, col: Int): Int = synchronized:
    grid(row)(col)
  
  /**
    * Sets a new value for a cell
    *
    * @param row the row of the cell
    * @param col the column of the cell
    * @param number the new value of the cell
    * @return the new number of updates for the cell
    */
  def set(row: Int, col: Int, number: Int): Int = synchronized:
    val upd = updates(row)(col)
    specialSet(row, col, number, upd + 1)
    upd + 1

  /**
    * Attempts to set a cell's value and number of updates. If the given number of updates is lower
    * than the current one for the cell, the update is not performed
    *
    * @param row the row of the cell
    * @param col the column of the cell
    * @param number the new value of the cell
    * @param updates the number of updates of the cell
    * @return True if the update was performed, false otherwise
    */
  def specialSet(row: Int, col: Int, number: Int, updates: Int): Boolean = synchronized:
    if (this.updates(row)(col) < updates)
    then
      this.grid(row)(col) = number
      this.updates(row)(col) = updates
      return true
    false

  @Transient
  def getCopy(): Grid = {
    val newMatrix = Grid(rows, cols)
    for 
      i <- 0 until rows
      j <- 0 until cols
      value = grid(i)(j)
      upd = updates(i)(j)
    do
      newMatrix.grid(i)(j) = value
      newMatrix.updates(i)(j) = upd
    newMatrix
  }
