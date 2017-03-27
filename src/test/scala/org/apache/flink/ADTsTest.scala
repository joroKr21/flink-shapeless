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

import api.common.typeinfo.TypeInformation

import org.scalacheck._

import scala.util._

import java.{lang => boxed}
import java.math

/** Data definitions. Must be separate due to SI-7046. */
object ADTsTest {
  import Arbitrary.{arbitrary => arb}
  import Gen._

  /** [[Exception]] with structural equality. */
  case class Err(msg: String) extends Exception(msg)

  object WeekDay extends Enumeration {
    val Mon, Tue, Wed, Thu, Fri, Sat, Sun = Value
  }

  /** Non-recursive product */
  case class Account(name: String, money: BigInt)

  /** Recursive product. */
  case class Tree[+E](value: E, children: List[Tree[E]])

  /** Non-recursive coproduct. */
  sealed trait Fruit
  case class Apple(color: (Int, Int, Int)) extends Fruit
  case class Banana(ripe: Boolean) extends Fruit

  /** Recursive coproduct. */
  sealed trait BTree[+E]
  case object BLeaf extends BTree[Nothing] { def size = 0 }
  case class BNode[+E](left: BTree[E], value: E, right: BTree[E]) extends BTree[E]

  /** Has a custom non-orphan [[TypeInformation]] instance. */
  case class Foo(x: Int)
  object Foo {
    implicit val info: TypeInformation[Foo] = null
  }

  // Arbitrary Java primitives
  implicit val arbNull:         Arbitrary[Null]            = Arbitrary(const(null))
  implicit val arbBoxedBoolean: Arbitrary[boxed.Boolean]   = Arbitrary(arb[Boolean].map(b => b))
  implicit val arbBoxedByte:    Arbitrary[boxed.Byte]      = Arbitrary(arb[Byte].map(b => b))
  implicit val arbBoxedShort:   Arbitrary[boxed.Short]     = Arbitrary(arb[Short].map(s => s))
  implicit val arbBoxedInt:     Arbitrary[boxed.Integer]   = Arbitrary(arb[Int].map(i => i))
  implicit val arbBoxedLong:    Arbitrary[boxed.Long]      = Arbitrary(arb[Long].map(l => l))
  implicit val arbBoxedFloat:   Arbitrary[boxed.Float]     = Arbitrary(arb[Float].map(f => f))
  implicit val arbBoxedDouble:  Arbitrary[boxed.Double]    = Arbitrary(arb[Double].map(d => d))
  implicit val arbBoxedChar:    Arbitrary[boxed.Character] = Arbitrary(arb[Char].map(c => c))

  implicit val arbJavaBigInt: Arbitrary[math.BigInteger] =
    Arbitrary(for (int <- arb[BigInt]) yield int.bigInteger)

  implicit val arbJavaBigDec: Arbitrary[math.BigDecimal] =
    Arbitrary(for (dec <- arb[BigDecimal]) yield dec.bigDecimal)

  /** [[Exception]] with structural equality. */
  implicit val arbThrowable: Arbitrary[Throwable] =
    Arbitrary(arb[String].map(Err.apply))

  /** [[Try]] with structural equality. */
  implicit def arbTry[A: Arbitrary]: Arbitrary[Try[A]] =
    Arbitrary(oneOf(arb[A].map(Success.apply), arb[Throwable].map(Failure.apply)))
}
