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

import api.common.ExecutionConfig
import api.common.typeinfo._
import api.java.typeutils._
import api.scala.typeutils._
import types.Value

import shapeless._

import scala.Function.const
import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds
import scala.reflect.ClassTag
import scala.util.Try

import java.{lang => boxed}
import java.math
import java.util.Date

/** Implicit [[TypeInformation]] instance. */
trait TypeInfoInstances extends BasicTypeInfoInstances {

  /** Summons an implicit [[TypeInformation]] instance in scope. */
  def typeInfo[A: TypeInfo]: TypeInfo[A] = implicitly

  /** Creates [[TypeInformation]] for type [[B]] based on isomorphism with type [[A]]. */
  def isoTypeInfo[A: TypeInfo, B: ClassTag](from: A => B, to: B => A): TypeInfo[B] =
    IsomorphicTypeInfo[A, B](implicitly)(from, to)

  // Shadow the default macro based TypeInformation providers.
  def createTypeInformation = ???
  def createTuple2TypeInformation = ???
  def scalaNothingTypeInfo = ???
}

/** Basic (primitive) [[TypeInformation]] instances. */
trait BasicTypeInfoInstances extends EnumTypeInfoInstances {
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

  // Isomorphisms

  implicit val nullTypeInfo: TypeInfo[Null] =
    IsomorphicTypeInfo(unitTypeInfo)(const(null), const(()))

  implicit val symbolTypeInfo: TypeInfo[Symbol] =
    IsomorphicTypeInfo(stringTypeInfo)(Symbol.apply, _.name)

  implicit val bigIntTypeInfo: TypeInfo[BigInt] =
    IsomorphicTypeInfo(javaBigIntTypeInfo)(BigInt.apply, _.bigInteger)

  implicit val bigDecTypeInfo: TypeInfo[BigDecimal] =
    IsomorphicTypeInfo(javaBigDecTypeInfo)(BigDecimal.apply, _.bigDecimal)
}

/** [[TypeInformation]] instances for Java and Scala enumerations. */
trait EnumTypeInfoInstances extends OptionTypeInfoInstances {
  implicit def enumTypeInfo[E <: boxed.Enum[E]: ClassTag]: TypeInfo[E] =
    new EnumTypeInfo(classFor)

  implicit def enumValueTypeInfo[E <: Enumeration](
    implicit enum: Witness.Aux[E]
  ): TypeInfo[E#Value] = new EnumValueTypeInfo(enum.value, classOf)
}

/** [[TypeInformation]] instances for [[Option]] and subclasses. */
trait OptionTypeInfoInstances extends EitherTypeInfoInstances {
  implicit val noneTypeInfo: TypeInfo[None.type] =
    new OptionTypeInfo[Nothing, None.type](new ScalaNothingTypeInfo)

  implicit def optionTypeInfo[O[a] <: Option[a], A: TypeInfo]: TypeInfo[O[A]] =
    new OptionTypeInfo[A, O[A]](implicitly)
}

/** [[TypeInformation]] instances for [[Either]] and subclasses. */
trait EitherTypeInfoInstances extends TryTypeInfoInstances {
  implicit def eitherTypeInfo[
    E[l, r] <: Either[l, r], L: TypeInfo, R: TypeInfo
  ](implicit tag: ClassTag[E[L, R]]): TypeInfo[E[L, R]] =
    new api.scala.typeutils.EitherTypeInfo[L, R, E[L, R]](classFor, implicitly, implicitly)
}

/** [[TypeInformation]] instances for [[Try]] and subclasses. */
trait TryTypeInfoInstances extends BasicArrayTypeInfoInstances {
  implicit def tryTypeInfo[T[a] <: Try[a], A: TypeInfo]: TypeInfo[T[A]] =
    new TryTypeInfo[A, T[A]](implicitly)
}

/** [[TypeInformation]] instances for basic (primitive) arrays. */
trait BasicArrayTypeInfoInstances extends GenericArrayTypeInfoInstances {
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

/** [[TypeInformation]] instances for generic arrays. */
trait GenericArrayTypeInfoInstances extends TraversableTypeInfoInstances {
  implicit def arrayTypeInfo[E](implicit element: TypeInfo[E]): TypeInfo[Array[E]] =
    ObjectArrayTypeInfo.getInfoFor(element)
}

/** [[TypeInformation]] instances for [[Traversable]] and [[Map]]. */
trait TraversableTypeInfoInstances extends ValueTypeInfoInstances {
  implicit def traversableTypeInfo[T[e] <: TraversableOnce[e], E: TypeInfo](
    implicit tag: ClassTag[T[E]], factory: CanBuildFrom[T[E], E, T[E]]
  ): TypeInfo[T[E]] = new TraversableTypeInfo[T[E], E](classFor, implicitly) {
    def createSerializer(config: ExecutionConfig) =
      new TraversableSerializer[T[E], E](elementTypeInfo.createSerializer(config)) {
        def getCbf = factory
      }
  }

  implicit def mapTypeInfo[M[k, v] <: Map[k, v], K, V](
    implicit
    kv: TypeInfo[(K, V)],
    tag: ClassTag[M[K, V]],
    factory: CanBuildFrom[M[K, V], (K, V), M[K, V]]
  ): TypeInfo[M[K, V]] = new TraversableTypeInfo[M[K, V], (K, V)](classFor, kv) {
    def createSerializer(config: ExecutionConfig) =
      new TraversableSerializer[M[K, V], (K, V)](kv.createSerializer(config)) {
        def getCbf = factory
      }
  }
}

/** [[TypeInformation]] instances for [[Value]] types. */
trait ValueTypeInfoInstances extends DefaultTypeInfoInstances {
  implicit def valueTypeInfo[V <: Value: ClassTag]: TypeInfo[V] =
    new ValueTypeInfo(classFor)
}

/** Lowest priority [[TypeInformation]] instances. */
trait DefaultTypeInfoInstances {
  type TypeInfo[T] = TypeInformation[T]

  protected def classFor[A](implicit tag: ClassTag[A]): Class[A] =
    tag.runtimeClass.asInstanceOf[Class[A]]
}
