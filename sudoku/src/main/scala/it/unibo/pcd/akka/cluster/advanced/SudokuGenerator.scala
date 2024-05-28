package it.unibo.pcd.akka.cluster.advanced

import scala.util.Random

object SudokuGenerator {
  val size = 9
  val subGridSize = 3

  def fillGrid(grid: Array[Array[Int]]): Boolean = {
    for (row <- 0 until size; col <- 0 until size) {
      if (grid(row)(col) == 0) {
        val numbers = Random.shuffle((1 to size).toList)
        for (num <- numbers) {
          if (isValid(grid, row, col, num)) {
            grid(row)(col) = num
            if (fillGrid(grid)) return true
            grid(row)(col) = 0
          }
        }
        return false
      }
    }
    removeNumbers(grid, size * size / 2)
    true
  }

  def isValid(grid: Array[Array[Int]], row: Int, col: Int, num: Int): Boolean = {
    !usedInRow(grid, row, num) &&
    !usedInCol(grid, col, num) &&
    !usedInBox(grid, row - row % subGridSize, col - col % subGridSize, num)
  }

  def usedInRow(grid: Array[Array[Int]], row: Int, num: Int): Boolean = {
    grid(row).contains(num)
  }

  def usedInCol(grid: Array[Array[Int]], col: Int, num: Int): Boolean = {
    grid.exists(row => row(col) == num)
  }

  def usedInBox(grid: Array[Array[Int]], boxStartRow: Int, boxStartCol: Int, num: Int): Boolean = {
    for (i <- 0 until subGridSize; j <- 0 until subGridSize) {
      if (grid(boxStartRow + i)(boxStartCol + j) == num) return true
    }
    false
  }

  def removeNumbers(grid: Array[Array[Int]], count: Int): Unit = {
    var removed = 0
    while (removed < count) {
      val row = Random.nextInt(size)
      val col = Random.nextInt(size)
      if (grid(row)(col) != 0) {
        grid(row)(col) = 0
        removed += 1
      }
    }
  }

  def isValidSudoku(grid: Array[Array[Int]]): Boolean = 
    val size = 9

    def isUnique(arr: Array[Int]): Boolean = {
        val nums = arr.filter(_ != 0)  // Filter out zeros (for intermediary checks)
        nums.distinct.length == nums.length && nums.length == size
    }

    // Check all rows
    for (row <- 0 until size) {
        if (!isUnique(grid(row))) return false
    }

    // Check all columns
    for (col <- 0 until size) {
        val column = Array.ofDim[Int](size)
        for (row <- 0 until size) {
        column(row) = grid(row)(col)
        }
        if (!isUnique(column)) return false
    }

    // Check all 3x3 subgrids
    for (boxRow <- 0 until 3) {
        for (boxCol <- 0 until 3) {
        val box = Array.ofDim[Int](size)
        var index = 0
        for (row <- 0 until 3) {
            for (col <- 0 until 3) {
            box(index) = grid(boxRow * 3 + row)(boxCol * 3 + col)
            index += 1
            }
        }
        if (!isUnique(box)) return false
        }
    }
    true


}
