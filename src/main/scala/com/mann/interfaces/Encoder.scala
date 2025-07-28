package com.mann.interfaces

import com.mann.core.SDR

trait Encoder[T] extends Serializable {
  def inputType: Class[T]
  def outputSize: Int

  /**
   * Encode single input to SDR
   */
  def encode(input: T): SDR

  /**
   * Batch encode multiple inputs
   */
  def encodeSequence(inputs: Seq[T]): Seq[SDR] = inputs.map(encode)

  /**
   * Get configuration for persistence
   */
  def getConfig: Map[String, Any]
}
