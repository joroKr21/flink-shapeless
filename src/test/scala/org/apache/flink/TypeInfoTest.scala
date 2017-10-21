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
import api.common.typeinfo._
import api.java.typeutils._
import api.scala._
import api.scala.typeutils._
import api.scala.derived.typeutils._
import core.memory._

import com.Ostermiller.util.CircularByteBuffer

import org.apache.commons.lang.SerializationUtils
import org.scalacheck._
import org.scalacheck.ScalacheckShapeless._
import org.scalatest._
import org.scalatest.prop.PropertyChecks

import scala.collection.mutable
import scala.reflect.ClassTag
import scala.util._

import java.awt.Color
import java.time.DayOfWeek
import java.util.Date

class TypeInfoTest extends FreeSpec with Matchers with PropertyChecks {
  import ADTsTest._
  import derived.auto._

  val buffer = new CircularByteBuffer(CircularByteBuffer.INFINITE_SIZE, false)
  val config = new ExecutionConfig

  def test[R: Arbitrary](implicit info: TypeInformation[R], tag: ClassTag[R]): Unit = {
    println(s"[info] Testing $info")
    // Test property consistency.
    val clazz = tag.runtimeClass
    info.getTypeClass.isAssignableFrom(clazz) || clazz.isPrimitive shouldBe true
    info.isBasicType && info.isTupleType shouldBe false
    info.isSortKeyType && !info.isKeyType shouldBe false
    info.getArity should be >= 0
    info.getTotalFields should be >= 0
    SerializationUtils.serialize(info) should not be empty
    val serializer = info.createSerializer(config)
    serializer.duplicate shouldEqual serializer
    val compat = serializer.ensureCompatibility(serializer.snapshotConfiguration)
    compat should not be 'requiresMigration
    try for { // Test copy, serialization and deserialization idempotency.
      input  <- resource.managed(new DataInputViewStreamWrapper(buffer.getInputStream))
      output <- resource.managed(new DataOutputViewStreamWrapper(buffer.getOutputStream))
    } forAll { record: R =>
      serializer.copy(record) shouldEqual record
      serializer.serialize(record, output)
      serializer.deserialize(input) shouldEqual record
      serializer.serialize(record, output)
      serializer.copy(input, output)
      serializer.deserialize(input) shouldEqual record
    } finally buffer.clear()
  }

  "Testing TypeInformation for" - {
    "Java primitives" in {
      test [java.lang.Boolean]
      test [java.lang.Byte]
      test [java.lang.Short]
      test [java.lang.Integer]
      test [java.lang.Long]
      test [java.lang.Float]
      test [java.lang.Double]
      test [java.lang.Character]
      test [java.math.BigInteger]
      test [java.math.BigDecimal]
    }

    "Scala primitives" in {
      // No instances exist.
      typeInfo [Nothing] shouldBe scalaNothingTypeInfo
      typeInfo [Void] shouldBe BasicTypeInfo.VOID_TYPE_INFO

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

    "Java primitive arrays" in {
      test [Array[java.lang.Boolean]]
      test [Array[java.lang.Byte]]
      test [Array[java.lang.Short]]
      test [Array[java.lang.Integer]]
      test [Array[java.lang.Long]]
      test [Array[java.lang.Float]]
      test [Array[java.lang.Double]]
      test [Array[java.lang.Character]]
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

    "Traversables" in {
      test [Array[DayOfWeek]]
      test [Seq[BigDecimal]]
      test [List[WeekDay.Value]]
      test [Set[Option[Unit]]]
      test [Vector[Try[Boolean]]]
      test [Map[Short, (Int, Long)]]
      test [Stream[Vector[Float]]]
    }

    "Case classes" in {
      // Recursive vs non-recursive.
      typeInfo [Account] shouldBe a [CaseClassTypeInfo[_]]
      typeInfo [Tree[Account]] shouldBe a [CaseClassTypeInfo[_]]

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

    "Unknown" in {
      "typeInfo[scala.concurrent.Future[String]]" shouldNot typeCheck
      "typeInfo[java.lang.Exception]" shouldNot typeCheck
    }

    "Injections" in {
      import scala.collection.JavaConversions._

      implicit val injectColor: Inject[Color, (Int, Int, Int, Int)] = Inject(
        col => (col.getRed, col.getGreen, col.getBlue, col.getAlpha),
        { case (r, g, b, alpha) => new Color(r, g, b, alpha) })

      implicit def injectList[A] = Inject[java.util.List[A], mutable.Buffer[A]]
      implicit def injectSet[A] = Inject[java.util.Set[A], mutable.Set[A]]
      implicit def injectMap[K, V] = Inject[java.util.Map[K, V], mutable.Map[K, V]]

      test [Color]
      test [java.util.List[Int]]
      test [java.util.Set[DayOfWeek]]
      test [java.util.Map[String, Long]]
    }

    "Factory" in {
      typeInfo [Ann] shouldBe a [InjectTypeInfo[_, _]]
      test [Ann]
    }

    "Fallback" in {
      import api.scala.{createTypeInformation => fallbackTypeInfo}

      typeInfo [Pojo] shouldBe a [PojoTypeInfo[_]]
      typeInfo [Throwable] shouldBe a [GenericTypeInfo[_]]

      test [Pojo]
      test [Throwable]
    }
  }
}
