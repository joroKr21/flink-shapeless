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

import org.scalacheck._

import scala.util._

import java.{lang => boxed}
import java.math

/** Missing [[Arbitrary]] definitions. */
object Arbitraries {
  import Arbitrary.{arbitrary => arb}
  import ADTs._
  import Gen._

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

  /** Reasonably sized [[Arbitrary]] [[Tree]]s. */
  implicit def arbTree[E: Arbitrary]: Arbitrary[Tree[E]] = {
    lazy val tree: Gen[Tree[E]] = lzy(sized {
      size => for {
        element <- arb[E]
        children <- if (size <= 1) const(Nil) else for {
          n <- choose(0, size - 1)
          h <- resize(size % (n + 1), tree)
          t <- listOfN(n, resize(size / (n + 1), tree))
        } yield h :: t
      } yield Tree(element, children)
    })
    Arbitrary(tree)
  }

  /** Reasonably sized [[Arbitrary]] [[BTree]]s. */
  implicit def arbBTree[E: Arbitrary]: Arbitrary[BTree[E]] = {
    lazy val tree: Gen[BTree[E]] = lzy(sized { size =>
      if (size == 0) BLeaf else for {
        e <- arb[E]
        n <- choose(1, size)
        l <- resize(n - 1, tree)
        r <- resize(size - n, tree)
      } yield BNode(l, e, r)
    })
    Arbitrary(tree)
  }
}
