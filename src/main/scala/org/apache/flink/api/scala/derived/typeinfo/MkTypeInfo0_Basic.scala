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
package api.scala.derived.typeinfo

import api.common.typeinfo.BasicTypeInfo._
import api.scala.typeutils._
import api.scala.derived.typeutils._

import scala.reflect.classTag

/** Basic (primitive) `TypeInformation` instances. */
private[typeinfo] abstract class MkTypeInfo0_Basic extends MkTypeInfo1_Enum {
  private val unitTypeInfo = new UnitTypeInfo

  // Java primitives

  implicit val mkJavaBooleanTypeInfo: MkTypeInfo[java.lang.Boolean] =
    this(BOOLEAN_TYPE_INFO)

  implicit val mkJavaByteTypeInfo: MkTypeInfo[java.lang.Byte] =
    this(BYTE_TYPE_INFO)

  implicit val mkJavaShortTypeInfo: MkTypeInfo[java.lang.Short] =
    this(SHORT_TYPE_INFO)

  implicit val mkJavaIntTypeInfo: MkTypeInfo[java.lang.Integer] =
    this(INT_TYPE_INFO)

  implicit val mkJavaLongTypeInfo: MkTypeInfo[java.lang.Long] =
    this(LONG_TYPE_INFO)

  implicit val mkJavaFloatTypeInfo: MkTypeInfo[java.lang.Float] =
    this(FLOAT_TYPE_INFO)

  implicit val mkJavaDoubleTypeInfo: MkTypeInfo[java.lang.Double] =
    this(DOUBLE_TYPE_INFO)

  implicit val mkJavaCharTypeInfo: MkTypeInfo[java.lang.Character] =
    this(CHAR_TYPE_INFO)

  implicit val mkJavaBigIntTypeInfo: MkTypeInfo[java.math.BigInteger] =
    this(BIG_INT_TYPE_INFO)

  implicit val mkJavaBigDecTypeInfo: MkTypeInfo[java.math.BigDecimal] =
    this(BIG_DEC_TYPE_INFO)

  implicit val mkVoidTypeInfo: MkTypeInfo[java.lang.Void] =
    this(VOID_TYPE_INFO)

  implicit val mkDateTypeInfo: MkTypeInfo[java.util.Date] =
    this(DATE_TYPE_INFO)

  // Scala primitives

  implicit val mkNothingTypeInfo: MkTypeInfo[Nothing] =
    apply[Nothing](new ScalaNothingTypeInfo)

  implicit val mkUnitTypeInfo: MkTypeInfo[Unit] =
    this(unitTypeInfo)

  implicit val mkBooleanTypeInfo: MkTypeInfo[Boolean] =
    this(getInfoFor(classOf[Boolean]))

  implicit val mkByteTypeInfo: MkTypeInfo[Byte] =
    this(getInfoFor(classOf[Byte]))

  implicit val mkShortTypeInfo: MkTypeInfo[Short] =
    this(getInfoFor(classOf[Short]))

  implicit val mkIntTypeInfo: MkTypeInfo[Int] =
    this(getInfoFor(classOf[Int]))

  implicit val mkLongTypeInfo: MkTypeInfo[Long] =
    this(getInfoFor(classOf[Long]))

  implicit val mkFloatTypeInfo: MkTypeInfo[Float] =
    this(getInfoFor(classOf[Float]))

  implicit val mkDoubleTypeInfo: MkTypeInfo[Double] =
    this(getInfoFor(classOf[Double]))

  implicit val mkCharTypeInfo: MkTypeInfo[Char] =
    this(getInfoFor(classOf[Char]))

  implicit val mkStringTypeInfo: MkTypeInfo[String] =
    this(STRING_TYPE_INFO)

  // Injections

  implicit val mkNullTypeInfo: MkTypeInfo[Null] = mkInjectTypeInfo[Null, Unit](
    Inject(_ => (), _ => null), unitTypeInfo, classTag
  )

  implicit val mkSymbolTypeInfo: MkTypeInfo[Symbol] = mkInjectTypeInfo[Symbol, String](
    Inject(_.name, Symbol.apply), STRING_TYPE_INFO, classTag
  )

  implicit val mkBigIntTypeInfo: MkTypeInfo[BigInt] =
    mkInjectTypeInfo[BigInt, java.math.BigInteger](
      Inject(_.bigInteger, BigInt.apply), BIG_INT_TYPE_INFO, classTag
    )

  implicit val mkBigDecTypeInfo: MkTypeInfo[BigDecimal] =
    mkInjectTypeInfo[BigDecimal, java.math.BigDecimal](
      Inject(_.bigDecimal, BigDecimal.apply), BIG_DEC_TYPE_INFO, classTag
    )
}
