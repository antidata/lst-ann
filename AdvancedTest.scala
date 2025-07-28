import com.mann.core.SDR
import com.mann.layer.TemporalLayerImpl
import com.mann.network.TemporalSequenceNetwork
import scala.io.Source
import java.io.File
import scala.collection.mutable

object AdvancedTest extends App {

  // --- Utility Functions ---

  def sdrFromInt(value: Int, size: Int): SDR = new SDR(size, Map(value % size -> 1.0))

  // --- Test Cases ---

  def testPredictionAccuracy(): Unit = {
    println("\n--- Testing Prediction Accuracy ---")
    val network = new TemporalSequenceNetwork()
    val inputLayer = new TemporalLayerImpl("input", 100, 5)
    network.addLayer(inputLayer)

    val trainingSequence = (1 to 100).toList
    val testSequence = (50 to 150).toList
    
    println("Training on sequence 1-100...")
    trainingSequence.foreach { value =>
      network.processInput("input", sdrFromInt(value, 100), System.currentTimeMillis())
    }

    var correctPredictions = 0
    var totalPredictions = 0

    println("Testing on sequence 50-150...")
    testSequence.dropRight(1).zip(testSequence.tail).foreach { case (input, expectedOutput) =>
      network.processInput("input", sdrFromInt(input, 100), System.currentTimeMillis())
      val predictions = network.getPredictions("input", 1)
      
      if (predictions.nonEmpty) {
        totalPredictions += 1
        val predictedValue = predictions.head.patterns.head.getActiveValues.keys.head
        if (predictedValue == expectedOutput % 100) {
          correctPredictions += 1
        }
      }
    }

    val accuracy = if (totalPredictions == 0) 0.0 else (correctPredictions.toDouble / totalPredictions) * 100
    println(f"Prediction Accuracy: $accuracy%.2f%% ($correctPredictions / $totalPredictions)")
  }

  def testPerformance(): Unit = {
    println("\n--- Testing Performance and Memory ---")
    val network = new TemporalSequenceNetwork()
    val inputLayer = new TemporalLayerImpl("input", 1000, 20)
    network.addLayer(inputLayer)

    val sequence = (1 to 10000).toList
    
    // Measure training time
    val startTime = System.nanoTime()
    sequence.foreach { value =>
      network.processInput("input", sdrFromInt(value, 1000), System.currentTimeMillis())
    }
    val endTime = System.nanoTime()
    val durationSeconds = (endTime - startTime) / 1e9
    println(f"Training time for 10,000 items: $durationSeconds%.4f seconds")

    // Measure memory footprint (a rough estimate)
    val runtime = Runtime.getRuntime
    runtime.gc() // Run garbage collector to get a cleaner measurement
    val memoryUsed = (runtime.totalMemory() - runtime.freeMemory()) / (1024.0 * 1024.0)
    println(f"Memory used after training: $memoryUsed%.2f MB")
  }

  def testAdaptationCurve(): Unit = {
    println("\n--- Generating Adaptation Curve Data ---")
    val network = new TemporalSequenceNetwork()
    val inputLayer = new TemporalLayerImpl("input", 100, 5)
    network.addLayer(inputLayer)

    val initialSequence = List(10, 20, 30, 40, 50)
    val newPattern = List(60, 70, 80, 90)
    val newSequence = List(10, 20, 30) ++ newPattern ++ newPattern ++ newPattern ++ newPattern

    println("Initial training on stable sequence...")
    for (_ <- 1 to 20) {
      initialSequence.foreach { value =>
        network.processInput("input", sdrFromInt(value, 100), System.currentTimeMillis())
      }
    }
    inputLayer.asInstanceOf[TemporalLayerImpl].reset()

    println("Introducing pattern change and measuring adaptation...")
    
    val writer = new java.io.PrintWriter(new File("adaptation_data.csv"))
    writer.println("TimeStep,Accuracy")

    val windowSize = 5
    val results = mutable.Queue[Boolean]()

    newSequence.dropRight(1).zip(newSequence.tail).zipWithIndex.foreach { case ((currentValue, nextValue), index) =>
      network.processInput("input", sdrFromInt(currentValue, 100), System.currentTimeMillis())
      val predictions = network.getPredictions("input", 1)
      
      var correct = false
      if (predictions.nonEmpty) {
        val predictedValue = predictions.head.patterns.head.getActiveValues.keys.head
        if (predictedValue == nextValue % 100) {
          correct = true
        }
      }
      
      results.enqueue(correct)
      if (results.size > windowSize) {
        results.dequeue()
      }
      
      val accuracy = (results.count(identity).toDouble / results.size) * 100
      writer.println(s"${index + 1},${"%.2f".format(accuracy)}")
    }
    writer.close()
    println("Adaptation data saved to adaptation_data.csv")
  }

  def testNegativeLearningEffectiveness(): Unit = {
    println("\n--- Testing Negative Learning Effectiveness ---")
    val network = new TemporalSequenceNetwork()
    val layer = new TemporalLayerImpl("input", 100, 5)
    network.addLayer(layer)

    val sdr1 = sdrFromInt(10, 100)
    val sdr2 = sdrFromInt(20, 100) // Unwanted
    val sdr3 = sdrFromInt(30, 100) // Correct

    // First, establish the correct link (10 -> 30)
    (1 to 15).foreach { _ =>
      layer.processInput(sdrFromInt(1, 100), System.currentTimeMillis()) // Break context
      layer.processInput(sdr1, System.currentTimeMillis())
      layer.processInput(sdr3, System.currentTimeMillis())
    }

    // Now, establish the unwanted link (10 -> 20) and make it stronger
    (1 to 15).foreach { _ =>
      layer.processInput(sdrFromInt(1, 100), System.currentTimeMillis()) // Break context
      layer.processInput(sdr1, System.currentTimeMillis())
      layer.processInput(sdr2, System.currentTimeMillis())
    }
    
    // Check initial activations
    layer.processInput(sdr1, System.currentTimeMillis())
    var predictions = layer.getPredictions(2)
    val unwantedActivationBefore = predictions.find(_.patterns.head.equals(sdr2)).map(_.confidence).getOrElse(0.0)
    val correctActivationBefore = predictions.find(_.patterns.head.equals(sdr3)).map(_.confidence).getOrElse(0.0)
    println(f"Unwanted Pattern Activation (Before): $unwantedActivationBefore%.2f")
    println(f"Correct Pattern Activation (Before): $correctActivationBefore%.2f")

    // Apply strong negative feedback to the unwanted link
    layer.applyNegativeFeedback(sdr1, sdr2, -2.0)
    
    // Check activations after feedback
    predictions = layer.getPredictions(2)
    val unwantedActivationAfter = predictions.find(_.patterns.head.equals(sdr2)).map(_.confidence).getOrElse(0.0)
    val correctActivationAfter = predictions.find(_.patterns.head.equals(sdr3)).map(_.confidence).getOrElse(0.0)
    println(f"Unwanted Pattern Activation (After): $unwantedActivationAfter%.2f")
    println(f"Correct Pattern Activation (After): $correctActivationAfter%.2f")
    
    // Verify learning recovery
    var recoveryTime = 0
    var recovered = false
    while(!recovered && recoveryTime < 10) {
      layer.processInput(sdr1, System.currentTimeMillis())
      layer.processInput(sdr2, System.currentTimeMillis())
      recoveryTime += 1
      
      layer.processInput(sdr1, System.currentTimeMillis())
      val p = layer.getPredictions(1)
      if(p.nonEmpty && p.head.patterns.head.equals(sdr2) && p.head.confidence > 0.5) {
        recovered = true
      }
    }
    println(s"Learning Recovery Time: $recoveryTime steps")
  }

  def run(): Unit = {
    println("Running Advanced Tests...")
    testPredictionAccuracy()
    testPerformance()
    testAdaptationCurve()
    testNegativeLearningEffectiveness()
    println("\nAdvanced Tests Complete.")
  }

  run()
}
