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

import api.common.typeinfo.TypeInformation

import shapeless._

import scala.annotation.implicitNotFound

/**
 * Equivalent to `ToList[LiftAll[TypeInformation, A], TypeInformation[_]]`,
 * but lazy and more efficient.
 */
@implicitNotFound("could not lift TypeInformation to type ${A}")
sealed trait LazyTypeInfos[A] extends (() => List[TypeInformation[_]]) with Serializable

/** Implicit `LazyTypeInfos` instances. */
object LazyTypeInfos {
  private def apply[A](infos: => List[TypeInformation[_]]) =
    new LazyTypeInfos[A] { def apply = infos }

  implicit val hNil: LazyTypeInfos[HNil] = apply(Nil)
  implicit val cNil: LazyTypeInfos[CNil] = apply(Nil)

  implicit def hCons[H, T <: HList](
    implicit tiH: Lazy[TypeInformation[H]], tiT: LazyTypeInfos[T]
  ): LazyTypeInfos[H :: T] = apply(tiH.value :: tiT())

  implicit def cCons[L, R <: Coproduct](
    implicit tiL: Lazy[TypeInformation[L]], tiR: LazyTypeInfos[R]
  ): LazyTypeInfos[L :+: R] = apply(tiL.value :: tiR())
}
