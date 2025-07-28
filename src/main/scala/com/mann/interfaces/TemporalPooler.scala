package com.mann.interfaces

import com.mann.core.SDR
import scala.collection.mutable

class TemporalPooler {
  def poolSequence(sequence: Seq[SDR]): SDR = {
    val allValues = mutable.Map[Int, Double]()
    val counts = mutable.Map[Int, Int]()

    sequence.foreach { sdr =>
      sdr.getActiveValues.foreach { case (index, value) =>
        allValues(index) = allValues.getOrElse(index, 0.0) + value
        counts(index) = counts.getOrElse(index, 0) + 1
      }
    }

    val averagedValues = allValues.map { case (index, totalValue) =>
      index -> totalValue / counts(index)
    }.toMap

    new SDR(sequence.head.size, averagedValues)
  }
}
