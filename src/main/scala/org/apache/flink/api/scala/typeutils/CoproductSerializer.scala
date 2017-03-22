/*
 * Copyright 2017 Georgi Krastev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.flink
package api.scala.typeutils

import api.common.typeutils.TypeSerializer
import core.memory.DataInputView
import core.memory.DataOutputView

/** A [[TypeSerializer]] for recursive coproduct types (sealed traits). */
case class CoproductSerializer[T](variants: Stream[TypeSerializer[T]])
    (which: T => Int) extends TypeSerializer[T] with InductiveObject {

  // Head is `null` to work around strictness.
  private def vs = variants.tail

  def getLength = -1

  lazy val isImmutableType =
    inductive(true)(vs.forall(_.isImmutableType))

  def duplicate = inductive(this) {
    CoproductSerializer(variants.map { v =>
      if (v == null) null else v.duplicate
    }.force)(which)
  }

  def createInstance =
    vs.head.createInstance

  def copy(record: T, reuse: T) =
    copy(record)

  def copy(record: T) =
    vs(which(record)).copy(record)

  def copy(source: DataInputView, target: DataOutputView) = {
    val i = source.readInt()
    target.writeInt(i)
    vs(i).copy(source, target)
  }

  def serialize(record: T, target: DataOutputView) = {
    val i = which(record)
    target.writeInt(i)
    vs(i).serialize(record, target)
  }

  def deserialize(reuse: T, source: DataInputView) =
    deserialize(source)

  def deserialize(source: DataInputView) =
    vs(source.readInt()).deserialize(source)

  override lazy val hashCode =
    inductive(0)(31 * variants.##)

  override def toString = inductive("this") {
    s"CoproductSerializer(${vs.mkString(", ")})"
  }
}
