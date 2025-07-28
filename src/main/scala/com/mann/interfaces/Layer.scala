package com.mann.interfaces

import com.mann.core.SDR
import com.mann.prediction.Prediction
import java.util.UUID

trait Layer {
  val id: UUID = UUID.randomUUID()
  val name: String
  def processInput(input: Any, timestamp: Long): Option[SDR]
  def processPrediction(prediction: Prediction): Unit
  def getPredictions(max: Int): Seq[Prediction]
  def getLastPredictions(): Seq[Prediction]
  def applyNegativeFeedback(from: SDR, to: SDR, weight: Double): Unit
  def inputLayers: Seq[Layer]
  def outputLayers: Seq[Layer]
  def connectTo(layer: Layer, bidirectional: Boolean = true): Unit
}
