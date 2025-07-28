package com.mann.network

import com.mann.interfaces.Layer
import com.mann.prediction.Prediction
import scala.collection.mutable

class TemporalSequenceNetwork {
  private val layers = mutable.LinkedHashMap[String, Layer]()
  
  def addLayer(layer: Layer): Unit = layers(layer.name) = layer
  def connectLayers(from: String, to: String, bidirectional: Boolean = true): Unit = {
    layers(from).connectTo(layers(to), bidirectional)
  }

  def processInput(layerName: String, input: Any, timestamp: Long): Unit = {
    var currentInput: Any = input
    var currentOpt: Option[Layer] = layers.get(layerName)
    
    while(currentOpt.isDefined) {
        val currentLayer = currentOpt.get
        val pooledSDR = currentLayer.processInput(currentInput, timestamp)
        
        if (pooledSDR.isDefined && currentLayer.outputLayers.nonEmpty) {
            currentInput = pooledSDR.get
            currentOpt = currentLayer.outputLayers.headOption
        } else {
            currentOpt = None
        }
    }
  }
  
  def processPredictions(): Unit = {
    layers.values.toSeq.reverse.foreach { layer =>
      val predictions = layer.getPredictions(5)
      layer.inputLayers.foreach { inputLayer =>
        predictions.foreach(inputLayer.processPrediction)
      }
    }
  }
  
  def getPredictions(layerName: String, max: Int): Seq[Prediction] = {
    layers.get(layerName).map(_.getPredictions(max)).getOrElse(Seq.empty)
  }
}
