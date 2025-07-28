package com.mann.core

import scala.collection.mutable

class SequenceMemory(val maxSequenceLength: Int) {
  case class SequenceLink(from: SDR, to: SDR, var weight: Double, var frequency: Int, var lastSeen: Long)
  private val links = mutable.Map[(SDR, SDR), SequenceLink]()
  val activeSequence = new CircularBuffer[SDR](maxSequenceLength)

  def addPattern(pattern: SDR, timestamp: Long): Unit = {
    if (activeSequence.size > 0) {
      val prev = activeSequence.get(activeSequence.size - 1)
      val key = (prev, pattern)
      val isNewLink = !links.contains(key)
      val link = links.getOrElseUpdate(key, SequenceLink(prev, pattern, 0.0, 0, 0))
      
      // If the link is new, give it a significant starting weight.
      if (isNewLink) {
        link.weight = 0.6
      } else {
        link.weight = math.min(1.0, link.weight + 0.2)
      }
      val oldWeight = if(isNewLink) 0.0 else link.weight - 0.1
      link.frequency += 1
      link.lastSeen = timestamp
    }
    activeSequence.add(pattern)
  }

  def getPossibleNext(pattern: SDR): Seq[(SDR, Double, Int, Long)] = {
    links.filter { case ((from, _), _) => from.overlap(pattern) > 0.8 }
         .map { case ((_, to), link) => (to, link.weight, link.frequency, link.lastSeen) }
         .toSeq
  }

  def applyNegativeFeedback(from: SDR, to: SDR, weight: Double): Unit = {
    val key = (from, to)
    links.get(key).foreach { link =>
      link.weight = math.max(0.0, link.weight + weight)
      link.frequency = 0 // Reset frequency to unlearn faster
    }
  }
}
