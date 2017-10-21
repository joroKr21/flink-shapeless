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

import api.common.typeinfo._
import api.scala._
import api.scala.derived
import api.scala.derived.typeutils._

import org.scalacheck._

import scala.util._
import scala.collection.JavaConverters._
import scala.collection.mutable

import java.awt.Color
import java.lang.reflect.Type

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

  /** A POJO (no `Generic` instance). */
  class Pojo {
    var x: Int = _
    var b: Boolean = _

    def canEqual(other: Any): Boolean =
      other.isInstanceOf[Pojo]

    override def equals(other: Any): Boolean = other match {
      case that: Pojo => (that canEqual this) &&
        this.x == that.x && this.b == that.b
      case _ => false
    }
  }

  object Pojo {
    implicit val arbitrary: Arbitrary[Pojo] =
      Arbitrary(for {
        x <- arb[Int]
        b <- arb[Boolean]
        pojo = new Pojo
      } yield {
        pojo.x = x
        pojo.b = b
        pojo
      })
  }

  @derived.TypeInfo[Ann](AnnFactory)
  case class Ann(i: Int, s: String)
  object AnnFactory extends TypeInfoFactory[Ann] {
    import derived.semiauto._
    def createTypeInfo(tpe: Type, params: java.util.Map[String, TypeInformation[_]]) =
      InjectTypeInfo(typeInfo[(Int, String)])(Inject(ann => ann.i -> ann.s, Ann.tupled))
  }

  // Arbitrary Java primitives

  implicit val arbNull: Arbitrary[Null] =
    Arbitrary(const(null))

  implicit val arbJavaBoolean: Arbitrary[java.lang.Boolean] =
    Arbitrary(arb[Boolean].map(b => b))

  implicit val arbJavaByte: Arbitrary[java.lang.Byte] =
    Arbitrary(arb[Byte].map(b => b))

  implicit val arbJavaShort: Arbitrary[java.lang.Short] =
    Arbitrary(arb[Short].map(s => s))

  implicit val arbJavaInt: Arbitrary[java.lang.Integer] =
    Arbitrary(arb[Int].map(i => i))

  implicit val arbJavaLong: Arbitrary[java.lang.Long] =
    Arbitrary(arb[Long].map(l => l))

  implicit val arbJavaFloat: Arbitrary[java.lang.Float] =
    Arbitrary(arb[Float].map(f => f))

  implicit val arbJavaDouble: Arbitrary[java.lang.Double] =
    Arbitrary(arb[Double].map(d => d))

  implicit val arbJavaChar: Arbitrary[java.lang.Character] =
    Arbitrary(arb[Char].map(c => c))

  implicit val arbColor: Arbitrary[Color] = {
    val shade = choose(0, 255)
    Arbitrary(for (r <- shade; g <- shade; b <- shade; a <- shade)
      yield new Color(r, g, b, a))
  }

  implicit val arbJavaBigInt: Arbitrary[java.math.BigInteger] =
    Arbitrary(for (int <- arb[BigInt]) yield int.bigInteger)

  implicit val arbJavaBigDec: Arbitrary[java.math.BigDecimal] =
    Arbitrary(for (dec <- arb[BigDecimal]) yield dec.bigDecimal)

  implicit def arbJavaList[A: Arbitrary]: Arbitrary[java.util.List[A]] =
    Arbitrary(arb[mutable.Buffer[A]].map(_.asJava))

  implicit def arbJavaSet[A: Arbitrary]: Arbitrary[java.util.Set[A]] =
    Arbitrary(arb[mutable.Set[A]].map(_.asJava))

  implicit def arbJavaMap[K: Arbitrary, V: Arbitrary]: Arbitrary[java.util.Map[K, V]] =
    Arbitrary(arb[mutable.Map[K, V]].map(_.asJava))

  /** [[Exception]] with structural equality. */
  implicit val arbThrowable: Arbitrary[Throwable] =
    Arbitrary(arb[String].map(Err.apply))

  /** [[Try]] with structural equality. */
  implicit def arbTry[A: Arbitrary]: Arbitrary[Try[A]] =
    Arbitrary(oneOf(arb[A].map(Success.apply), arb[Throwable].map(Failure.apply)))
}
