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
package api.scala.derived.typeinfo

import api.java.typeutils.EnumTypeInfo
import api.scala.typeutils.EnumValueTypeInfo

import shapeless.Witness

import scala.reflect.ClassTag

/** `TypeInformation` instances for Java and Scala enums. */
private[typeinfo] abstract class MkTypeInfo1_Enum extends MkTypeInfo2_Option_Either_Try {

  /** Creates `TypeInformation` for the Java enum `E`. */
  implicit def mkEnumTypeInfo[E <: Enum[E]](implicit tag: ClassTag[E]): MkTypeInfo[E] =
    this(new EnumTypeInfo(tag.runtimeClass.asInstanceOf[Class[E]]))

  /** Creates `TypeInformation` for the Scala enum `E`. */
  implicit def mkEnumValueTypeInfo[E <: Enumeration](
    implicit enum: Witness.Aux[E]
  ): MkTypeInfo[E#Value] = this {
    new EnumValueTypeInfo(enum.value, classOf)
  }
}