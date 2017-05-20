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
package api.scala.typeinfo

import api.common.ExecutionConfig
import api.common.typeinfo._
import api.java.typeutils._
import api.scala.derived.MkTypeInfo
import api.scala.typeutils._
import types.Value
import shapeless._

import scala.collection.generic._
import scala.collection.mutable
import scala.reflect.ClassTag
import scala.util.Try

import java.math
import java.util.Date
import java.{lang => boxed}

/** Implicit [[TypeInformation]] instance. */
trait Instances extends Instances0_Basic {

  /** Summons an implicit [[TypeInformation]] instance in scope. */
  def typeInfo[A: TypeInfo]: TypeInfo[A] = implicitly

  // Shadow the default macro based TypeInformation providers.
  def createTypeInformation: Nothing = ???
  def createTuple2TypeInformation: Nothing = ???
  def scalaNothingTypeInfo: Nothing = ???
}

/** Basic (primitive) [[TypeInformation]] instances. */
trait Instances0_Basic extends Instances1_Enum {
  import BasicTypeInfo._

  // Boxed Java primitives
  implicit val boxedBooleanTypeInfo:  TypeInfo[boxed.Boolean]     = BOOLEAN_TYPE_INFO
  implicit val boxedByteTypeInfo:     TypeInfo[boxed.Byte]        = BYTE_TYPE_INFO
  implicit val boxedShortTypeInfo:    TypeInfo[boxed.Short]       = SHORT_TYPE_INFO
  implicit val boxedIntTypeInfo:      TypeInfo[boxed.Integer]     = INT_TYPE_INFO
  implicit val boxedLongTypeInfo:     TypeInfo[boxed.Long]        = LONG_TYPE_INFO
  implicit val boxedFloatTypeInfo:    TypeInfo[boxed.Float]       = FLOAT_TYPE_INFO
  implicit val boxedDoubleTypeInfo:   TypeInfo[boxed.Double]      = DOUBLE_TYPE_INFO
  implicit val boxedCharTypeInfo:     TypeInfo[boxed.Character]   = CHAR_TYPE_INFO
  implicit val javaBigIntTypeInfo:    TypeInfo[math.BigInteger]   = BIG_INT_TYPE_INFO
  implicit val javaBigDecTypeInfo:    TypeInfo[math.BigDecimal]   = BIG_DEC_TYPE_INFO

  // Scala primitives
  implicit val nothingTypeInfo: TypeInfo[Nothing]  = new ScalaNothingTypeInfo
  implicit val unitTypeInfo:    TypeInfo[Unit]     = new UnitTypeInfo
  implicit val boolTypeInfo:    TypeInfo[Boolean]  = getInfoFor(classOf)
  implicit val byteTypeInfo:    TypeInfo[Byte]     = getInfoFor(classOf)
  implicit val shortTypeInfo:   TypeInfo[Short]    = getInfoFor(classOf)
  implicit val intTypeInfo:     TypeInfo[Int]      = getInfoFor(classOf)
  implicit val longTypeInfo:    TypeInfo[Long]     = getInfoFor(classOf)
  implicit val floatTypeInfo:   TypeInfo[Float]    = getInfoFor(classOf)
  implicit val doubleTypeInfo:  TypeInfo[Double]   = getInfoFor(classOf)
  implicit val charTypeInfo:    TypeInfo[Char]     = getInfoFor(classOf)
  implicit val voidTypeInfo:    TypeInfo[Void]     = VOID_TYPE_INFO
  implicit val stringTypeInfo:  TypeInfo[String]   = STRING_TYPE_INFO
  implicit val dateTypeInfo:    TypeInfo[Date]     = DATE_TYPE_INFO

  // Injections

  implicit private val injectNull: Inject[Null, Unit] =
    Inject(_ => (), _ => null)

  implicit private val injectSymbol: Inject[Symbol, String] =
    Inject(_.name, Symbol.apply)

  implicit private val injectBigInt: Inject[BigInt, math.BigInteger] =
    Inject(_.bigInteger, BigInt.apply)

  implicit private val injectBigDec: Inject[BigDecimal, math.BigDecimal] =
    Inject(_.bigDecimal, BigDecimal.apply)

  implicit val nullTypeInfo:   TypeInfo[Null]       = injectTypeInfo
  implicit val symbolTypeInfo: TypeInfo[Symbol]     = injectTypeInfo
  implicit val bigIntTypeInfo: TypeInfo[BigInt]     = injectTypeInfo
  implicit val bigDecTypeInfo: TypeInfo[BigDecimal] = injectTypeInfo
}

/** [[TypeInformation]] instances for Java and Scala enumerations. */
trait Instances1_Enum extends Instances2_Option_Either_Try {
  implicit def enumTypeInfo[E <: Enum[E]: ClassTag]: TypeInfo[E] =
    new EnumTypeInfo(classFor)

  implicit def enumValueTypeInfo[E <: Enumeration](
    implicit enum: Witness.Aux[E]
  ): TypeInfo[E#Value] = new EnumValueTypeInfo(enum.value, classOf)
}

/** [[TypeInformation]] instances for [[Option]], [[Either]], [[Try]] and subclasses. */
trait Instances2_Option_Either_Try extends Instances3_Array {
  implicit val noneTypeInfo: TypeInfo[None.type] =
    new OptionTypeInfo[Nothing, None.type](new ScalaNothingTypeInfo)

  implicit def optionTypeInfo[O[a] <: Option[a], A: TypeInfo]: TypeInfo[O[A]] =
    new OptionTypeInfo[A, O[A]](implicitly)

  implicit def eitherTypeInfo[
    E[l, r] <: Either[l, r], L: TypeInfo, R: TypeInfo
  ](implicit tag: ClassTag[E[L, R]]): TypeInfo[E[L, R]] =
    new api.scala.typeutils.EitherTypeInfo[L, R, E[L, R]](classFor, implicitly, implicitly)

  implicit def tryTypeInfo[T[a] <: Try[a], A: TypeInfo]: TypeInfo[T[A]] =
    new TryTypeInfo[A, T[A]](implicitly)
}

/** [[TypeInformation]] instances for basic (primitive) arrays. */
trait Instances3_Array extends Instances4_Traversable {
  import BasicArrayTypeInfo._
  import PrimitiveArrayTypeInfo._

  // Boxed Java primitives
  implicit val boxedBooleanArrayTypeInfo: TypeInfo[Array[boxed.Boolean]]   = BOOLEAN_ARRAY_TYPE_INFO
  implicit val boxedByteArrayTypeInfo:    TypeInfo[Array[boxed.Byte]]      = BYTE_ARRAY_TYPE_INFO
  implicit val boxedShortArrayTypeInfo:   TypeInfo[Array[boxed.Short]]     = SHORT_ARRAY_TYPE_INFO
  implicit val boxedIntArrayTypeInfo:     TypeInfo[Array[boxed.Integer]]   = INT_ARRAY_TYPE_INFO
  implicit val boxedLongArrayTypeInfo:    TypeInfo[Array[boxed.Long]]      = LONG_ARRAY_TYPE_INFO
  implicit val boxedFloatArrayTypeInfo:   TypeInfo[Array[boxed.Float]]     = FLOAT_ARRAY_TYPE_INFO
  implicit val boxedDoubleArrayTypeInfo:  TypeInfo[Array[boxed.Double]]    = DOUBLE_ARRAY_TYPE_INFO
  implicit val boxedCharArrayTypeInfo:    TypeInfo[Array[boxed.Character]] = CHAR_ARRAY_TYPE_INFO

  // Scala primitives
  implicit val booleanArrayTypeInfo:  TypeInfo[Array[Boolean]]  = BOOLEAN_PRIMITIVE_ARRAY_TYPE_INFO
  implicit val byteArrayTypeInfo:     TypeInfo[Array[Byte]]     = BYTE_PRIMITIVE_ARRAY_TYPE_INFO
  implicit val shortArrayTypeInfo:    TypeInfo[Array[Short]]    = SHORT_PRIMITIVE_ARRAY_TYPE_INFO
  implicit val intArrayTypeInfo:      TypeInfo[Array[Int]]      = INT_PRIMITIVE_ARRAY_TYPE_INFO
  implicit val longArrayTypeInfo:     TypeInfo[Array[Long]]     = LONG_PRIMITIVE_ARRAY_TYPE_INFO
  implicit val floatArrayTypeInfo:    TypeInfo[Array[Float]]    = FLOAT_PRIMITIVE_ARRAY_TYPE_INFO
  implicit val doubleArrayTypeInfo:   TypeInfo[Array[Double]]   = DOUBLE_PRIMITIVE_ARRAY_TYPE_INFO
  implicit val charArrayTypeInfo:     TypeInfo[Array[Char]]     = CHAR_PRIMITIVE_ARRAY_TYPE_INFO
  implicit val stringArrayTypeInfo:   TypeInfo[Array[String]]   = STRING_ARRAY_TYPE_INFO
}

/** [[TypeInformation]] instances for [[Array]], [[Traversable]] and [[Map]]. */
trait Instances4_Traversable extends Instances5_Value {
  private implicit def injectMap[K, V]: Inject[Map[K, V], Seq[(K, V)]] =
    Inject(_.toSeq, _.toMap)

  private implicit def injectMutableMap[K, V]: Inject[mutable.Map[K, V], Seq[(K, V)]] =
    Inject(_.toSeq, mutable.Map(_: _*))

  implicit def arrayTypeInfo[E](implicit element: TypeInfo[E]): TypeInfo[Array[E]] =
    ObjectArrayTypeInfo.getInfoFor(element)

  implicit def traversableTypeInfo[
    T[e] <: Traversable[e], E: TypeInfo
  ](implicit
    tag: ClassTag[T[E]],
    cbf: CanBuild[E, T[E]],
    ev: T[E] <:< GenericTraversableTemplate[E, T]
  ): TypeInfo[T[E]] = new TraversableTypeInfo[T[E], E](classFor, implicitly) {
    val empty = cbf().result()
    def createSerializer(config: ExecutionConfig) =
      new TraversableSerializer[T[E], E](elementTypeInfo.createSerializer(config)) {
        override def toString = s"TraversableSerializer[$elementSerializer]"
        def getCbf = new CanBuildFrom[T[E], E, T[E]] {
          def apply(from: T[E]) = ev(from).genericBuilder
          def apply() = apply(empty)
        }
      }
  }

  implicit def mapTypeInfo[K, V](
    implicit kv: TypeInfo[(K, V)]
  ): TypeInfo[Map[K, V]] = injectTypeInfo

  implicit def mutableMapTypeInfo[K, V](
    implicit kv: TypeInfo[(K, V)]
  ): TypeInfo[mutable.Map[K, V]] = injectTypeInfo
}

/** [[TypeInformation]] instances for [[Value]] types. */
trait Instances5_Value extends Instances6_Singleton {
  implicit def valueTypeInfo[V <: Value: ClassTag]: TypeInfo[V] =
    new ValueTypeInfo(classFor)
}

/** [[TypeInformation]] instances for [[Singleton]] objects. */
trait Instances6_Singleton extends Instances7_Injected {
  implicit def singletonTypeInfo[S: ClassTag](
    implicit singleton: Witness.Aux[S]
  ): TypeInfo[S] = injectTypeInfo[S, Unit](
    Inject(_ => (), _ => singleton.value), new UnitTypeInfo, implicitly)
}

/** [[TypeInformation]] instances for [[Inject]]ed types. */
trait Instances7_Injected extends Instances8_Derived {
  /** Creates [[TypeInformation]] for type [[A]] based on an injection into [[B]]. */
  implicit def injectTypeInfo[A, B](
    implicit inj: Inject[A, B], underlying: TypeInfo[B], tag: ClassTag[A]
  ): TypeInfo[A] = InjectTypeInfo[A, B](underlying)(inj)
}

/** Automatically derived [[TypeInformation]] instances. */
trait Instances8_Derived extends InstancesZ {

  /**
   * If type [[A]] is a (possibly recursive) Algebraic Data Type (ADT), automatically derives a
   * [[TypeInformation]] instance for it.
   *
   * Other implicit instances in scope take higher priority except those provided by
   * [[api.scala.createTypeInformation]] (the macro based approach), because it has a default
   * catch-all case based on runtime reflection.
   *
   * @param lp Evidence that no other implicit instance of `TypeInformation[A]` is in scope.
   * @param mk The derived [[TypeInformation]] provider ([[Strict]] helps avoid divergence).
   * @tparam A A (possibly recursive) Algebraic Data Type (ADT).
   * @return The derived [[TypeInformation]] instance.
   */
  // Derive only when no other implicit instance is in scope.
  implicit def derivedTypeInfo[A](
    implicit
    lp: LowPriority.Ignoring[Witness.`"createTypeInformation"`.T],
    mk: Strict[MkTypeInfo[A]]
  ): TypeInfo[A] = mk.value()
}

/** Lowest priority [[TypeInformation]] instances. */
trait InstancesZ {
  type TypeInfo[T] = TypeInformation[T]

  protected def classFor[A](implicit tag: ClassTag[A]): Class[A] =
    tag.runtimeClass.asInstanceOf[Class[A]]
}
