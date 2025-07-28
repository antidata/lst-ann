package com.mann.layer

import com.mann.core.{SDR, SequenceMemory}
import com.mann.interfaces.{Layer, TemporalPooler}
import com.mann.prediction.Prediction
import java.util.UUID

class TemporalLayerImpl(val name: String, val sdrSize: Int, val sequenceLength: Int) extends Layer {
  val sequenceMemory = new SequenceMemory(sequenceLength)
  val temporalPooler = new TemporalPooler()
  var lastPredictions: Seq[Prediction] = Seq.empty
  private var _inputLayers = Seq.empty[Layer]
  private var _outputLayers = Seq.empty[Layer]
  private var currentSDR: Option[SDR] = None

  def processInput(input: Any, timestamp: Long): Option[SDR] = {
    val sdr = input.asInstanceOf[SDR]
    currentSDR = Some(sdr)
    com.mann.learning.LearningAlgorithm.learn(this, sdr, timestamp)
    
    val activeSeq = sequenceMemory.activeSequence.toSeq
    if (activeSeq.length >= sequenceLength) {
      Some(temporalPooler.poolSequence(activeSeq))
    } else {
      None
    }
  }

  def processPrediction(prediction: Prediction): Unit = {
    // Modulate current activity with prediction
    if (prediction.confidence > 0.5) {
      prediction.patterns.foreach { pattern =>
        currentSDR = currentSDR.map { sdr =>
          val modulatedValues = sdr.getActiveValues.map { case (k, v) => k -> (v * 0.5) }
          val predictedValues = pattern.getActiveValues.map { case (k, v) => k -> (v * 0.5) }
          new SDR(sdr.size, modulatedValues ++ predictedValues)
        }
      }
    }
  }

  private def calculateConfidence(weight: Double, frequency: Int, lastSeen: Long, currentTime: Long): Double = {
    val weightScore = weight
    val frequencyScore = 1.0 - math.exp(-frequency / 10.0) // Normalize frequency
    val recencyScore = math.exp(-(currentTime - lastSeen) / 10000.0) // Decay over 10 seconds
    (weightScore * 0.5) + (frequencyScore * 0.3) + (recencyScore * 0.2)
  }

  def getPredictions(max: Int): Seq[Prediction] = {
    if (sequenceMemory.activeSequence.size == 0) return Seq.empty
    val lastSDR = sequenceMemory.activeSequence.get(sequenceMemory.activeSequence.size - 1)
    val currentTime = System.currentTimeMillis()
    
    sequenceMemory.getPossibleNext(lastSDR)
      .map { case (sdr, weight, freq, lastSeen) =>
        val confidence = calculateConfidence(weight, freq, lastSeen, currentTime)
        Prediction(Seq(sdr), confidence, id, currentTime)
      }
      .sortBy(-_.confidence)
      .take(max)
  }
  
  def getLastPredictions(): Seq[Prediction] = lastPredictions
  def applyNegativeFeedback(from: SDR, to: SDR, weight: Double): Unit = sequenceMemory.applyNegativeFeedback(from, to, weight)
  def inputLayers: Seq[Layer] = _inputLayers
  def outputLayers: Seq[Layer] = _outputLayers
  def connectTo(layer: Layer, bidirectional: Boolean = true): Unit = {
    _outputLayers :+= layer
    if (bidirectional) {
      layer.asInstanceOf[TemporalLayerImpl]._inputLayers :+= this
    }
  }

  def reset(): Unit = {
    sequenceMemory.activeSequence.clear()
    lastPredictions = Seq.empty
  }
}
