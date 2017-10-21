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

import api.scala.derived.TypeInfo

import shapeless.Annotation

import scala.reflect.ClassTag

/** `TypeInformation` instances based on the `TypeInfo` annotation. */
trait MkTypeInfo7_Factory extends MkTypeInfo8_Inject {

  /** Creates `TypeInformation` for type `A` if it's annotated with `TypeInfo`. */
  implicit def mkFactoryTypeInfo[A](
    implicit ann: Annotation[TypeInfo[A], A], tag: ClassTag[A]
  ): MkTypeInfo[A] = MkTypeInfo {
    ann().factory.createTypeInfo(tag.runtimeClass, new java.util.HashMap)
  }
}