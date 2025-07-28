package com.mann.prediction

import com.mann.core.SDR
import java.util.UUID

case class Prediction(patterns: Seq[SDR], confidence: Double, sourceLayerId: UUID, timestamp: Long)
