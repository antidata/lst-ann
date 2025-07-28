package com.mann.learning

import com.mann.core.SDR
import com.mann.layer.TemporalLayerImpl

object LearningAlgorithm {
  def learn(layer: TemporalLayerImpl, input: SDR, timestamp: Long): Unit = {
    // Get predictions BEFORE adding the new pattern
    val predictions = layer.getPredictions(5)

    // Apply negative feedback to incorrect predictions
    if (layer.sequenceMemory.activeSequence.size > 0) {
      val prevSDR = layer.sequenceMemory.activeSequence.get(layer.sequenceMemory.activeSequence.size - 1)
      predictions.foreach { pred =>
        val predictedPattern = pred.patterns.head
        if (predictedPattern != input && pred.confidence > 0.1) { // Lower threshold for more aggressive unlearning
          layer.applyNegativeFeedback(prevSDR, predictedPattern, -0.5) // More aggressive penalty
        }
      }
    }

    // Now, add the new pattern and update the sequence memory
    layer.sequenceMemory.addPattern(input, timestamp)
    layer.lastPredictions = layer.getPredictions(5)
  }
}
