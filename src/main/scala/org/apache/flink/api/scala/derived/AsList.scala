package org.apache.flink
package api.scala.derived

import shapeless._

import scala.annotation.implicitNotFound

/** Equivalent to `ToList[T, E]`, but serializable and more efficient. */
@implicitNotFound("could not convert ${T} to a List[${E}]")
trait AsList[T <: HList, E] extends (T => List[E]) with Serializable

/** [[AsList]] instances. */
object AsList {
  def apply[T <: HList, E](implicit instance: AsList[T, E]): AsList[T, E] = instance

  implicit def hnil[E]: AsList[HNil, E] = new AsList[HNil, E] {
    def apply(nil: HNil) = Nil
  }

  implicit def hcons[H, T <: HList, E](
    implicit list: AsList[T, E], ev: H <:< E
  ): AsList[H :: T, E] = new AsList[H :: T, E] {
    def apply(cons: H :: T) = cons.head :: list(cons.tail)
  }
}
