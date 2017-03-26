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

import api.common.typeinfo.TypeInformation

import shapeless._

import scala.annotation.implicitNotFound

/**
 * Equivalent to `ToTraversable.Aux[LiftAll[TypeInformation, A]#Out, Stream, TypeInformation[_]]`,
 * but lazy and more efficient.
 */
@implicitNotFound("could not lift TypeInformation to type ${A}")
trait TypeInfos[A] extends (() => Stream[TypeInformation[_]]) with Serializable

/** [[TypeInfos]] instances. */
object TypeInfos {
  def apply[A: TypeInfos]: TypeInfos[A] = implicitly

  implicit val hnil: TypeInfos[HNil] = new TypeInfos[HNil] {
    def apply = Stream.empty
  }

  implicit val cnil: TypeInfos[CNil] = new TypeInfos[CNil] {
    def apply = Stream.empty
  }

  implicit def hcons[H, T <: HList](
    implicit head: Lazy[TypeInformation[H]], tail: TypeInfos[T]
  ): TypeInfos[H :: T] = new TypeInfos[H :: T] {
    def apply = head.value #:: tail()
  }

  implicit def ccons[H, T <: Coproduct](
    implicit head: Lazy[TypeInformation[H]], tail: TypeInfos[T]
  ): TypeInfos[H :+: T] = new TypeInfos[H :+: T] {
    def apply = head.value #:: tail()
  }
}
