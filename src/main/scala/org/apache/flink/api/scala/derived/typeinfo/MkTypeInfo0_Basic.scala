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
trait MkTypeInfo0_Basic extends MkTypeInfo1_Enum {
  private val unitTypeInfo = new UnitTypeInfo

  // Java primitives

  implicit val mkJavaBooleanTypeInfo: MkTypeInfo[java.lang.Boolean] =
    MkTypeInfo(BOOLEAN_TYPE_INFO)

  implicit val mkJavaByteTypeInfo: MkTypeInfo[java.lang.Byte] =
    MkTypeInfo(BYTE_TYPE_INFO)

  implicit val mkJavaShortTypeInfo: MkTypeInfo[java.lang.Short] =
    MkTypeInfo(SHORT_TYPE_INFO)

  implicit val mkJavaIntTypeInfo: MkTypeInfo[java.lang.Integer] =
    MkTypeInfo(INT_TYPE_INFO)

  implicit val mkJavaLongTypeInfo: MkTypeInfo[java.lang.Long] =
    MkTypeInfo(LONG_TYPE_INFO)

  implicit val mkJavaFloatTypeInfo: MkTypeInfo[java.lang.Float] =
    MkTypeInfo(FLOAT_TYPE_INFO)

  implicit val mkJavaDoubleTypeInfo: MkTypeInfo[java.lang.Double] =
    MkTypeInfo(DOUBLE_TYPE_INFO)

  implicit val mkJavaCharTypeInfo: MkTypeInfo[java.lang.Character] =
    MkTypeInfo(CHAR_TYPE_INFO)

  implicit val mkJavaBigIntTypeInfo: MkTypeInfo[java.math.BigInteger] =
    MkTypeInfo(BIG_INT_TYPE_INFO)

  implicit val mkJavaBigDecTypeInfo: MkTypeInfo[java.math.BigDecimal] =
    MkTypeInfo(BIG_DEC_TYPE_INFO)

  implicit val mkVoidTypeInfo: MkTypeInfo[java.lang.Void] =
    MkTypeInfo(VOID_TYPE_INFO)

  implicit val mkDateTypeInfo: MkTypeInfo[java.util.Date] =
    MkTypeInfo(DATE_TYPE_INFO)

  // Scala primitives

  implicit val mkNothingTypeInfo: MkTypeInfo[Nothing] =
    MkTypeInfo[Nothing](new ScalaNothingTypeInfo)

  implicit val mkUnitTypeInfo: MkTypeInfo[Unit] =
    MkTypeInfo(unitTypeInfo)

  implicit val mkBooleanTypeInfo: MkTypeInfo[Boolean] =
    MkTypeInfo(getInfoFor(classOf[Boolean]))

  implicit val mkByteTypeInfo: MkTypeInfo[Byte] =
    MkTypeInfo(getInfoFor(classOf[Byte]))

  implicit val mkShortTypeInfo: MkTypeInfo[Short] =
    MkTypeInfo(getInfoFor(classOf[Short]))

  implicit val mkIntTypeInfo: MkTypeInfo[Int] =
    MkTypeInfo(getInfoFor(classOf[Int]))

  implicit val mkLongTypeInfo: MkTypeInfo[Long] =
    MkTypeInfo(getInfoFor(classOf[Long]))

  implicit val mkFloatTypeInfo: MkTypeInfo[Float] =
    MkTypeInfo(getInfoFor(classOf[Float]))

  implicit val mkDoubleTypeInfo: MkTypeInfo[Double] =
    MkTypeInfo(getInfoFor(classOf[Double]))

  implicit val mkCharTypeInfo: MkTypeInfo[Char] =
    MkTypeInfo(getInfoFor(classOf[Char]))

  implicit val mkStringTypeInfo: MkTypeInfo[String] =
    MkTypeInfo(STRING_TYPE_INFO)

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
