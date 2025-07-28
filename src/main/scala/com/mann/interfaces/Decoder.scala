package com.mann.interfaces

import com.mann.core.SDR

trait Decoder[T] extends Serializable {
  def outputType: Class[T]
  def inputSize: Int

  /**
   * Decode SDR to original type
   */
  def decode(sdr: SDR): T

  /**
   * Decode with confidence scores
   */
  def decodeWithConfidence(sdr: SDR): Seq[(T, Double)]

  /**
   * Batch decode
   */
  def decodeSequence(sdrs: Seq[SDR]): Seq[T] = sdrs.map(decode)

  /**
   * Get configuration for persistence
   */
  def getConfig: Map[String, Any]
}
