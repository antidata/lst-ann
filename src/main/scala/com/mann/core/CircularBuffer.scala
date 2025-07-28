package com.mann.core

import scala.reflect.ClassTag

class CircularBuffer[T: ClassTag](capacity: Int) {
  private val buffer = new Array[T](capacity)
  private var head = 0
  private var count = 0

  def add(item: T): Unit = {
    buffer(head) = item
    head = (head + 1) % capacity
    if (count < capacity) count += 1
  }

  def get(index: Int): T = {
    if (index < 0 || index >= count) throw new IndexOutOfBoundsException
    buffer((head - count + index + capacity) % capacity)
  }
  
  def toSeq: Seq[T] = (0 until count).map(get)
  def size: Int = count
  def clear(): Unit = {
    head = 0
    count = 0
  }
}
