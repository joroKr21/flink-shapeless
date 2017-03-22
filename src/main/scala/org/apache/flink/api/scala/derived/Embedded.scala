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
import scala.language.higherKinds

/** Type class witnessing that type [[E]] is embedded in type [[T]]. */
@implicitNotFound("could not prove that type ${E} is embedded in type ${T}")
trait Embedded[T, E]

/** Implicit [[Embedded]] instances. */
object Embedded extends EmbeddedADTs {

  /** Summons an implicit [[Embedded]] instance in scope. */
  def apply[T, E](implicit emb: Embedded[T, E]): Embedded[T, E] = emb

  /** The default case. */
  implicit def reflexive[T]: Embedded[T, T] = instance

  implicit def hhead[H, T <: HList, E](
    implicit head: Strict[Embedded[H, E]]
  ): Embedded[H :: T, E] = instance

  implicit def chead[H, T <: Coproduct, E](
    implicit head: Strict[Embedded[H, E]]
  ): Embedded[H :+: T, E] = instance
}

/** [[Embedded]] instances for Algebraic Data Types (ADTs). */
trait EmbeddedADTs extends EmbeddedHKTs {
  implicit def htail[H, T <: HList, E](
    implicit tail: Embedded[T, E]
  ): Embedded[H :: T, E] = instance

  implicit def ctail[H, T <: Coproduct, E](
    implicit tail: Embedded[T, E]
  ): Embedded[H :+: T, E] = instance

  implicit def adt[A, R, E](
    implicit gen: Generic.Aux[A, R], repr: Embedded[R, E]
  ): Embedded[A, E] = instance
}

/** [[Embedded]] instances for higher kinded types (HKTs). */
trait EmbeddedHKTs extends EmbeddedLP {
  implicit def kind1[F[_], A, E](
    implicit lp: LowPriority, a: Embedded[A, E]
  ): Embedded[F[A], E] = instance

  implicit def kind2a[F[_, _], A, B, E](
    implicit lp: LowPriority, a: Embedded[A, E]
  ): Embedded[F[A, B], E] = instance

  implicit def kind2b[F[_, _], A, B, E](
    implicit lp: LowPriority, b: Embedded[B, E]
  ): Embedded[F[A, B], E] = instance

  implicit def kind3a[F[_, _, _], A, B, C, E](
    implicit lp: LowPriority, a: Embedded[A, E]
  ): Embedded[F[A, B, C], E] = instance

  implicit def kind3b[F[_, _, _], A, B, C, E](
    implicit lp: LowPriority, b: Embedded[B, E]
  ): Embedded[F[A, B, C], E] = instance

  implicit def kind3c[F[_, _, _], A, B, C, E](
    implicit lp: LowPriority, c: Embedded[C, E]
  ): Embedded[F[A, B, C], E] = instance
}

/** Lowest priority [[Embedded]] instances. */
trait EmbeddedLP {
  // Since this type class has no methods, a singleton instance suffices.
  private val singleton: Embedded[_, _] = new Embedded[Any, Any] { }
  protected def instance[T, E]: Embedded[T, E] = singleton.asInstanceOf[Embedded[T, E]]
}
