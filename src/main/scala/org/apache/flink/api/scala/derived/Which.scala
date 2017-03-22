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

/** Type class for tagging the coproduct type [[C]] with an integer index. */
@implicitNotFound("could not derive an indexing function for type ${C}")
trait Which[C <: Coproduct] extends DepFn1[C] with (C => Int) {
  type Out = Int
}

/** [[Which]] instances. */
object Which {
  def apply[C <: Coproduct: Which]: Which[C] = implicitly

  /** Available only for the compiler, never to be called. */
  implicit val cnil: Which[CNil] = new Which[CNil] {
    def apply(nil: CNil) = ??? // impossible
  }

  implicit def ccons[H, T <: Coproduct](
    implicit tailIndex: Which[T]
  ): Which[H :+: T] = new Which[H :+: T] {
    def apply(cons: H :+: T) = cons match {
      case Inl(_) => 0
      case Inr(t) => 1 + tailIndex(t)
    }
  }
}
