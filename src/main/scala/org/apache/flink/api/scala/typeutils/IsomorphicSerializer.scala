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

/** A [[TypeSerializer]] for [[B]] based on isomorphism with type [[A]]. */
case class IsomorphicSerializer[A, B](underlying: TypeSerializer[A])
    (from: A => B, to: B => A) extends TypeSerializer[B] {

  def isImmutableType =
    underlying.isImmutableType

  def getLength =
    underlying.getLength

  def duplicate =
    IsomorphicSerializer(underlying.duplicate)(from, to)

  def createInstance =
    from(underlying.createInstance)

  def copy(record: B) =
    from(underlying.copy(to(record)))

  def copy(record: B, reuse: B) =
    copy(record)

  def copy(source: DataInputView, target: DataOutputView) =
    underlying.copy(source, target)

  def serialize(record: B, target: DataOutputView) =
    underlying.serialize(to(record), target)

  def deserialize(source: DataInputView) =
    from(underlying.deserialize(source))

  def deserialize(reuse: B, source: DataInputView) =
    deserialize(source)
}
