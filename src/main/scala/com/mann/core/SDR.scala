package com.mann.core

import scala.collection.mutable

class SDR(val size: Int) {
  private val activeValues = mutable.Map[Int, Double]()

  def this(size: Int, values: Map[Int, Double]) = {
    this(size)
    activeValues ++= values.filter { case (k, v) => k < size && v > 0 }
  }

  def setValue(index: Int, value: Double): Unit = {
    if (index < size) {
      if (value > 0) activeValues(index) = value
      else activeValues.remove(index)
    }
  }

  def getValue(index: Int): Double = activeValues.getOrElse(index, 0.0)
  def getActiveValues: Map[Int, Double] = activeValues.toMap

  def overlap(other: SDR): Double = {
    val commonIndices = this.activeValues.keySet.intersect(other.activeValues.keySet)
    val dotProduct = commonIndices.map(i => this.getValue(i) * other.getValue(i)).sum
    
    val normA = math.sqrt(this.activeValues.values.map(v => v*v).sum)
    val normB = math.sqrt(other.activeValues.values.map(v => v*v).sum)

    if (normA == 0 || normB == 0) 0.0
    else dotProduct / (normA * normB)
  }

  override def hashCode(): Int = activeValues.hashCode()
  override def equals(obj: Any): Boolean = obj match {
    case that: SDR => this.activeValues == that.activeValues
    case _ => false
  }
}
