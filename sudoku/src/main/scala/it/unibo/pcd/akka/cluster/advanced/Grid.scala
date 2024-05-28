package it.unibo.pcd.akka.cluster.advanced

import com.fasterxml.jackson.annotation.JsonManagedReference
import java.beans.Transient
import it.unibo.pcd.akka.cluster.advanced.SudokuGenerator.fillGrid

case class Grid(private val rows: Int, private val cols: Int):
  val grid = Array.ofDim[Int](rows, cols)
  fillGrid(grid)
  def get(row: Int, col: Int): Int = grid(row)(col)
  def set(row: Int, col: Int, number: Int) = grid(row)(col) = number

  @Transient
  def getCopy(): Grid = {
    val newMatrix = Grid(rows, cols)
    for (i <- 0 until rows) {
      for (j <- 0 until cols) {
        newMatrix.set(i, j, grid(i)(j))
      }
    }
    newMatrix
  }
