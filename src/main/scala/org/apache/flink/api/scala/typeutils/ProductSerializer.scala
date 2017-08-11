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
case class ProductSerializer[P](var fields: Seq[TypeSerializer[Any]] = Seq.empty)
    (from: Seq[Any] => P, to: P => Seq[Any])
    extends TypeSerializer[P] with InductiveObject {

  def isImmutableType: Boolean = inductive(true) {
    fields.forall(_.isImmutableType)
  }

  lazy val getLength: Int = inductive(-1) {
    if (fields.exists(_.getLength <= 0)) -1
    else fields.map(_.getLength).sum
  }

  def duplicate: TypeSerializer[P] = inductive(this) {
    val serializer = ProductSerializer()(from, to)
    serializer.fields = for (f <- fields) yield f.duplicate
    serializer
  }

  def createInstance: P =
    from(for (f <- fields) yield f.createInstance)

  def copy(record: P, reuse: P): P =
    copy(record)

  def copy(record: P): P =
    from(for ((f, v) <- fields zip to(record)) yield f.copy(v))

  def copy(source: DataInputView, target: DataOutputView): Unit =
    for (f <- fields) f.copy(source, target)

  def serialize(record: P, target: DataOutputView): Unit =
    for ((f, v) <- fields zip to(record)) f.serialize(v, target)

  def deserialize(reuse: P, source: DataInputView): P =
    deserialize(source)

  def deserialize(source: DataInputView): P =
    from(for (f <- fields) yield f.deserialize(source))

  override def hashCode: Int =
    inductive(0)(31 * fields.##)

  override def toString: String = inductive("this") {
    s"ProductSerializer(${fields.mkString(", ")})"
  }
}
