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

/** Type class for tagging the co-product type `C` with an integer index. */
@implicitNotFound("could not derive an indexing function for type ${C}")
sealed trait Which[C <: Coproduct] extends (C => Int) with Serializable

/** Implicit `Which` instances. */
object Which {
  private def apply[C <: Coproduct](index: C => Int) =
    new Which[C] { def apply(c: C) = index(c) }

  /** Available only for the compiler, never to be called. */
  implicit val cNil: Which[CNil] = apply(_ => -1)

  implicit def cCons[L, R <: Coproduct](
    implicit wR: Which[R]
  ): Which[L :+: R] = apply {
    _.eliminate(_ => 0, r => 1 + wR(r))
  }
}
