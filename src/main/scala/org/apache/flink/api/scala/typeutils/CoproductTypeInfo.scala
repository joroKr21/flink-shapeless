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

import api.common.ExecutionConfig
import api.common.typeinfo.TypeInformation
import api.common.typeutils.TypeSerializer

import scala.reflect.ClassTag

/** [[TypeInformation]] for recursive coproduct types (sealed traits). */
case class CoproductTypeInfo[T](variants: Stream[TypeInformation[_]])
    (which: T => Int)(implicit tag: ClassTag[T])
    extends TypeInformation[T] with InductiveObject {

  private var serializer: TypeSerializer[T] = _

  def isBasicType = false
  def isKeyType = false
  def isTupleType = false
  def getArity = 1
  def getTotalFields = 1

  def getTypeClass =
    tag.runtimeClass.asInstanceOf[Class[T]]

  // Handle cycles in the object graph.
  def createSerializer(config: ExecutionConfig) = inductive(serializer) {
    val vs = for (v <- variants) yield if (v == null) null
      else v.createSerializer(config).asInstanceOf[TypeSerializer[T]]

    serializer = CoproductSerializer(vs)(which)
    vs.force
    serializer
  }

  override lazy val hashCode =
    inductive(0)(31 * variants.##)

  override def toString = inductive("this") {
    s"${getTypeClass.getTypeName}(${variants.tail.mkString(", ")})"
  }
}
