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

/** A [[TypeSerializer]] for recursive product types (case classes). */
case class ProductSerializer[P](fields: Stream[TypeSerializer[Any]])
    (from: Seq[Any] => P, to: P => Seq[Any])
    extends TypeSerializer[P] with InductiveObject {

  // Head is `null` to work around strictness.
  private def fs = fields.tail

  lazy val isImmutableType =
    inductive(true)(fs.forall(_.isImmutableType))

  lazy val getLength = inductive(-1) {
    if (fs.exists(_.getLength <= 0)) -1
    else fs.map(_.getLength).sum
  }

  def duplicate = inductive(this) {
    ProductSerializer(fields.map { f =>
      if (f == null) null else f.duplicate
    }.force)(from, to)
  }

  def createInstance =
    from(for (f <- fs) yield f.createInstance)

  def copy(record: P, reuse: P) =
    copy(record)

  def copy(record: P) =
    from(for ((f, v) <- fs zip to(record)) yield f.copy(v))

  def copy(source: DataInputView, target: DataOutputView) =
    for (f <- fs) f.copy(source, target)

  def serialize(record: P, target: DataOutputView) =
    for ((f, v) <- fs zip to(record)) f.serialize(v, target)

  def deserialize(reuse: P, source: DataInputView) =
    deserialize(source)

  def deserialize(source: DataInputView) =
    from(for (f <- fs) yield f.deserialize(source))

  override lazy val hashCode =
    inductive(0)(31 * fields.##)

  override def toString = inductive("this") {
    s"ProductSerializer(${fs.mkString(", ")})"
  }
}
