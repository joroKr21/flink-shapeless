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
class CoproductTypeInfo[T](vs: => Seq[TypeInformation[_]])
    (which: T => Int)(implicit tag: ClassTag[T])
    extends TypeInformation[T] with InductiveObject {

  lazy val variants = vs
  @transient private var serializer: CoproductSerializer[T] = _

  def isBasicType = false
  def isKeyType = false
  def isTupleType = false
  def getArity = 1
  def getTotalFields = 1

  def getTypeClass =
    tag.runtimeClass.asInstanceOf[Class[T]]

  // Handle cycles in the object graph.
  def createSerializer(config: ExecutionConfig) = inductive(serializer) {
    serializer = CoproductSerializer()(which)
    serializer.variants = for (v <- variants)
      yield v.createSerializer(config).asInstanceOf[TypeSerializer[T]]
    serializer
  }

  def canEqual(that: Any) =
    that.isInstanceOf[CoproductTypeInfo[_]]

  override def equals(other: Any) = other match {
    case that: CoproductTypeInfo[_] =>
      (this eq that) || (that canEqual this) && this.variants == that.variants
    case _ => false
  }

  override def hashCode =
    inductive(0)(31 * variants.##)

  override def toString = inductive("this") {
    s"${getTypeClass.getTypeName}(${variants.mkString(", ")})"
  }
}
