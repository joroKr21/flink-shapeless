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
package api.scala.derived.typeutils

import shapeless._

import scala.annotation.implicitNotFound

/** Equivalent to `ToList[T, E]`, but serializable and more efficient. */
@implicitNotFound("could not convert ${L} to a List[${E}]")
sealed trait AsList[L <: HList, E] extends (L => List[E]) with Serializable

/** Implicit `AsList` instances. */
object AsList {
  private def apply[L <: HList, E](as: L => List[E]) =
    new AsList[L, E] { def apply(l: L) = as(l) }

  implicit def hNil[E]: AsList[HNil, E] =
    apply(_ => Nil)

  implicit def hCons[H, T <: HList, E](
    implicit alT: AsList[T, E], ev: H <:< E
  ): AsList[H :: T, E] = apply {
    case h :: t => h :: alT(t)
  }
}
