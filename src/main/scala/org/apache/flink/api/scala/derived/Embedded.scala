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

/** Type class witnessing that type [[E]] is embedded in type [[T]]. */
@implicitNotFound("could not prove that type ${E} is embedded in type ${T}")
trait Embedded[T, E] extends Serializable

/** Implicit [[Embedded]] instances. */
object Embedded extends Embedded0_Head {

  /** Summons an implicit [[Embedded]] instance in scope. */
  def apply[T, E](implicit emb: Embedded[T, E]): Embedded[T, E] = emb

  /** The default case. */
  implicit def reflexive[T]: Embedded[T, T] = instance

  /** Algebraic data types. */
  implicit def adt[A, R, E](
    implicit gen: Generic.Aux[A, R], repr: Embedded[R, E]
  ): Embedded[A, E] = instance
}

/** [[Embedded]] instances for product and coproduct head. */
trait Embedded0_Head extends Embedded1_Tail {
  implicit def hHead[H, T <: HList, E](
    implicit head: Strict[Embedded[H, E]]
  ): Embedded[H :: T, E] = instance

  implicit def cHead[H, T <: Coproduct, E](
    implicit head: Strict[Embedded[H, E]]
  ): Embedded[H :+: T, E] = instance
}

/** [[Embedded]] instances for product and coproduct tail. */
trait Embedded1_Tail extends Embedded3_HKT {
  implicit def hTail[H, T <: HList, E](
    implicit tail: Embedded[T, E]
  ): Embedded[H :: T, E] = instance

  implicit def cTail[H, T <: Coproduct, E](
    implicit tail: Embedded[T, E]
  ): Embedded[H :+: T, E] = instance
}

/** [[Embedded]] instances for higher kinded types (HKTs). */
trait Embedded3_HKT extends EmbeddedZ {
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
trait EmbeddedZ {
  // Since this type class has no methods, a singleton instance suffices.
  private val singleton: Embedded[_, _] = new Embedded[Any, Any] { }
  protected def instance[T, E]: Embedded[T, E] = singleton.asInstanceOf[Embedded[T, E]]
}
