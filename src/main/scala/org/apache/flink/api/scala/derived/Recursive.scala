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
package api.scala.derived

import shapeless._

import scala.annotation.implicitNotFound

/** Type class witnessing that [[T]] is a recursive Algebraic Data Type (ADT). */
@implicitNotFound("could not prove that ${T} is a Recursive data type")
trait Recursive[T]

/** Implicit [[Recursive]] instances. */
object Recursive {
  // Since this type class has no methods, a singleton instance suffices.
  private val instance: Recursive[_] = new Recursive[Any] { }

  /** Summons an implicit [[Recursive]] instance in scope. */
  def apply[T: Recursive]: Recursive[T] = implicitly

  implicit def adt[T, R](
    implicit gen: Generic.Aux[T, R], emb: Embedded[R, T]
  ): Recursive[T] = instance.asInstanceOf[Recursive[T]]
}
