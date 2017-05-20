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

/** A [[TypeSerializer]] for [[A]] based on an injection into [[B]]. */
case class InjectSerializer[A, B](underlying: TypeSerializer[B])
    (inj: Inject[A, B]) extends TypeSerializer[A] {

  def isImmutableType =
    underlying.isImmutableType

  def getLength =
    underlying.getLength

  def duplicate =
    InjectSerializer(underlying.duplicate)(inj)

  def createInstance =
    inj.invert(underlying.createInstance)

  def copy(record: A) =
    inj.invert(underlying.copy(inj(record)))

  def copy(record: A, reuse: A) =
    copy(record)

  def copy(source: DataInputView, target: DataOutputView) =
    underlying.copy(source, target)

  def serialize(record: A, target: DataOutputView) =
    underlying.serialize(inj(record), target)

  def deserialize(source: DataInputView) =
    inj.invert(underlying.deserialize(source))

  def deserialize(reuse: A, source: DataInputView) =
    deserialize(source)
}
