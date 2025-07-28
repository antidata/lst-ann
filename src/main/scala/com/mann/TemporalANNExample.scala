package com.mann

import com.mann.core.SDR
import com.mann.layer.TemporalLayerImpl
import com.mann.network.TemporalSequenceNetwork

object TemporalANNExample extends App {
  def sdrFromInt(value: Int, size: Int): SDR = new SDR(size, Map(value % size -> 1.0))

  val network = new TemporalSequenceNetwork()
  val inputLayer = new TemporalLayerImpl("input", 100, 5)
  val layer2 = new TemporalLayerImpl("layer2", 200, 5)
  network.addLayer(inputLayer)
  network.addLayer(layer2)
  network.connectLayers("input", "layer2")

  val trainingSequence = List(10, 20, 30, 40, 50)
  
  println("--- Training ---")
  for (epoch <- 1 to 15) {
    println(s"Epoch $epoch")
    trainingSequence.foreach { value =>
      network.processInput("input", sdrFromInt(value, 100), System.currentTimeMillis())
      network.processPredictions() // Process top-down predictions
    }
    val predictions = network.getPredictions("input", 3)
    if (predictions.nonEmpty) {
      println("  Predictions:")
      predictions.foreach { p =>
        val predictedValue = p.patterns.head.getActiveValues.keys.head
        println(f"    - Value: $predictedValue (Confidence: ${p.confidence}%.2f)")
      }
    }
  }

  println("\n--- Testing ---")
  // First, show that the network learned the original sequence
  val learnedSequence = List(10, 20, 30, 40)
  learnedSequence.foreach { value =>
    network.processInput("input", sdrFromInt(value, 100), System.currentTimeMillis())
    network.processPredictions()
  }
  var predictions = network.getPredictions("input", 3)
  println("  Predictions after seeing 40 (should be 50):")
  predictions.foreach { p =>
    val predictedValue = p.patterns.head.getActiveValues.keys.head
    println(f"    - Value: $predictedValue (Confidence: ${p.confidence}%.2f)")
  }

  // Now, introduce the unexpected value
  println("\n  Introducing unexpected value 60...")
  network.processInput("input", sdrFromInt(60, 100), System.currentTimeMillis())
  network.processPredictions()
  
  predictions = network.getPredictions("input", 3)
  println("\n  Final Predictions after seeing 60 (should be none):")
  if (predictions.isEmpty) {
    println("    - No predictions, as expected.")
  } else {
    predictions.foreach { p =>
      val predictedValue = p.patterns.head.getActiveValues.keys.head
      println(f"    - Value: $predictedValue (Confidence: ${p.confidence}%.2f)")
    }
  }
}
