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

import api.common.ExecutionConfig
import api.common.typeinfo.TypeInformation
import api.scala._
import core.memory._

import org.scalacheck.Shapeless._
import org.scalacheck._
import org.scalatest._
import org.scalatest.prop.PropertyChecks
import resource._

import scala.util._

import java.io._
import java.math
import java.time.DayOfWeek
import java.util.Date
import java.{lang => boxed}

class TypeInfoTest extends FreeSpec with Matchers with PropertyChecks {
  import ADTs._
  import Arbitraries._
  import Implicits._

  val config = new ExecutionConfig

  def test[R: Arbitrary](implicit info: TypeInformation[R]) = {
    // Test `.toString` (no StackOverflow).
    println(s"Testing $info")

    // Test property consistency.
    info.isBasicType && info.isTupleType shouldBe false
    info.isSortKeyType && !info.isKeyType shouldBe false
    info.getArity should be >= 0
    info.getTotalFields should be >= 0

    val serializer = info.createSerializer(config)
    serializer.duplicate shouldEqual serializer

    // Test copy, serialization and deserialization idempotency.
    forAll { record: R =>
      serializer.copy(record) shouldEqual record

      for {
        outStream <- managed(new ByteArrayOutputStream)
        outView   <- managed(new DataOutputViewStreamWrapper(outStream))
        _ = serializer.serialize(record, outView)
        inStream  <- managed(new ByteArrayInputStream(outStream.toByteArray))
        inView    <- managed(new DataInputViewStreamWrapper(inStream))
      } serializer.deserialize(inView) shouldEqual record
    }
  }

  "Deriving TypeInformation for" - {
    "Boxed Java primitives" in {
      test [boxed.Boolean]
      test [boxed.Byte]
      test [boxed.Short]
      test [boxed.Integer]
      test [boxed.Long]
      test [boxed.Float]
      test [boxed.Double]
      test [boxed.Character]
      test [math.BigInteger]
      test [math.BigDecimal]
    }

    "Scala primitives" in {
      // No instances exist.
      typeInfo [Nothing] shouldBe nothingTypeInfo
      typeInfo [Void] shouldBe voidTypeInfo

      test [Null]
      test [Unit]
      test [Boolean]
      test [Byte]
      test [Short]
      test [Int]
      test [Long]
      test [Float]
      test [Double]
      test [Char]
      test [String]
      test [Symbol]
      test [Date]
      test [BigInt]
      test [BigDecimal]
    }

    "Enums" in {
      test [WeekDay.Value]
      test [DayOfWeek]
    }

    "Option" in {
      test [Option[Unit]]
      test [Some[Boolean]]
      test [None.type]
    }

    "Either" in {
      test [Either[Byte, Short]]
      test [Left[Int, Long]]
      test [Right[Float, Double]]
    }

    "Try" in {
      test [Try[Char]]
      test [Success[String]]
      test [Failure[BigInt]]
    }

    "Java boxed arrays" in {
      test [Array[boxed.Boolean]]
      test [Array[boxed.Byte]]
      test [Array[boxed.Short]]
      test [Array[boxed.Integer]]
      test [Array[boxed.Long]]
      test [Array[boxed.Float]]
      test [Array[boxed.Double]]
      test [Array[boxed.Character]]
    }

    "Scala primitive arrays" in {
      test [Array[Boolean]]
      test [Array[Byte]]
      test [Array[Short]]
      test [Array[Int]]
      test [Array[Long]]
      test [Array[Float]]
      test [Array[Double]]
      test [Array[Char]]
      test [Array[String]]
    }

    "Generic arrays" in {
      test [Array[DayOfWeek]]
    }

    "Traversables" in {
      test [Seq[BigDecimal]]
      test [List[WeekDay.Value]]
      test [Set[Option[Unit]]]
      test [Vector[Try[Boolean]]]
      test [Map[Short, (Int, Long)]]
      test [Stream[Vector[Float]]]
    }

    "Case classes" in {
      test [(Double, Char)]
      test [Nil.type]
      test [Account]
      test [Tree[Account]]
    }

    "Sealed traits" in {
      test [Fruit]
      test [BTree[Fruit]]
    }

    "Custom" in {
      a [NullPointerException] should be thrownBy test [Foo]
      a [NullPointerException] should be thrownBy test [List[Foo]]
      a [NullPointerException] should be thrownBy test [(Foo, Long)]
    }
  }
}
