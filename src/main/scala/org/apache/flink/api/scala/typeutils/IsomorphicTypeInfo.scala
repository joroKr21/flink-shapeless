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

import scala.reflect.ClassTag

/** [[TypeInformation]] for type [[B]] based on isomorphism with type [[A]]. */
case class IsomorphicTypeInfo[A, B](underlying: TypeInformation[A])
    (from: A => B, to: B => A)(implicit tag: ClassTag[B])
    extends TypeInformation[B] {

  def isBasicType =
    underlying.isBasicType

  def isKeyType =
    underlying.isKeyType

  def isTupleType =
    underlying.isTupleType

  def getArity =
    underlying.getArity

  def getTotalFields =
    underlying.getTotalFields

  def getTypeClass =
    tag.runtimeClass.asInstanceOf[Class[B]]

  def createSerializer(config: ExecutionConfig) =
    IsomorphicSerializer(underlying.createSerializer(config))(from, to)

  override def toString =
    getTypeClass.getTypeName
}
