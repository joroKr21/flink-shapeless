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

import api.common.typeinfo.TypeInformation
import api.scala.typeutils._

import scala.reflect.ClassTag
import scala.util.Try

/** `TypeInformation` instances for `Option`, `Either` and `Try`. */
trait MkTypeInfo2_Option_Either_Try extends MkTypeInfo3_Array {

  /** Creates a `TypeInformation` instance for `Option`. */
  implicit def mkOptionTypeInfo[O[a] <: Option[a], A](
    implicit tiA: TypeInformation[A]
  ): MkTypeInfo[O[A]] = MkTypeInfo {
    new OptionTypeInfo[A, O[A]](tiA)
  }

  /** Creates a `TypeInformation` instance for `Either`. */
  implicit def mkEitherTypeInfo[E[l, r] <: Either[l, r], L, R](
    implicit tiL: TypeInformation[L], tiR: TypeInformation[R], tag: ClassTag[E[L, R]]
  ): MkTypeInfo[E[L, R]] = MkTypeInfo {
    new EitherTypeInfo[L, R, E[L, R]](tag.runtimeClass.asInstanceOf[Class[E[L, R]]], tiL, tiR)
  }

  /** Creates a `TypeInformation` instance for `Try`. */
  implicit def mkTryTypeInfo[T[a] <: Try[a], A](
    implicit tiA: TypeInformation[A]
  ): MkTypeInfo[T[A]] = MkTypeInfo {
    new TryTypeInfo[A, T[A]](tiA)
  }
}