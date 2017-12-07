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

import api.common.typeinfo._

/** `TypeInformation` instances for basic (primitive) arrays. */
private[typeinfo] abstract class MkTypeInfo3_Array extends MkTypeInfo4_Traversable {
  import BasicArrayTypeInfo._
  import PrimitiveArrayTypeInfo._

  // Java primitives

  implicit val mkJavaBooleanArrayTypeInfo: MkTypeInfo[Array[java.lang.Boolean]] =
    this(BOOLEAN_ARRAY_TYPE_INFO)

  implicit val mkJavaByteArrayTypeInfo: MkTypeInfo[Array[java.lang.Byte]] =
    this(BYTE_ARRAY_TYPE_INFO)

  implicit val mkJavaShortArrayTypeInfo: MkTypeInfo[Array[java.lang.Short]] =
    this(SHORT_ARRAY_TYPE_INFO)

  implicit val mkJavaIntArrayTypeInfo: MkTypeInfo[Array[java.lang.Integer]] =
    this(INT_ARRAY_TYPE_INFO)

  implicit val mkJavaLongArrayTypeInfo: MkTypeInfo[Array[java.lang.Long]] =
    this(LONG_ARRAY_TYPE_INFO)

  implicit val mkJavaFloatArrayTypeInfo: MkTypeInfo[Array[java.lang.Float]] =
    this(FLOAT_ARRAY_TYPE_INFO)

  implicit val mkJavaDoubleArrayTypeInfo: MkTypeInfo[Array[java.lang.Double]] =
    this(DOUBLE_ARRAY_TYPE_INFO)

  implicit val mkJavaCharArrayTypeInfo: MkTypeInfo[Array[java.lang.Character]] =
    this(CHAR_ARRAY_TYPE_INFO)

  // Scala primitives

  implicit val mkBooleanArrayTypeInfo: MkTypeInfo[Array[Boolean]] =
    this(BOOLEAN_PRIMITIVE_ARRAY_TYPE_INFO)

  implicit val mkByteArrayTypeInfo: MkTypeInfo[Array[Byte]] =
    this(BYTE_PRIMITIVE_ARRAY_TYPE_INFO)

  implicit val mkShortArrayTypeInfo: MkTypeInfo[Array[Short]] =
    this(SHORT_PRIMITIVE_ARRAY_TYPE_INFO)

  implicit val mkIntArrayTypeInfo: MkTypeInfo[Array[Int]] =
    this(INT_PRIMITIVE_ARRAY_TYPE_INFO)

  implicit val mkLongArrayTypeInfo: MkTypeInfo[Array[Long]] =
    this(LONG_PRIMITIVE_ARRAY_TYPE_INFO)

  implicit val mkFloatArrayTypeInfo: MkTypeInfo[Array[Float]] =
    this(FLOAT_PRIMITIVE_ARRAY_TYPE_INFO)

  implicit val mkDoubleArrayTypeInfo: MkTypeInfo[Array[Double]] =
    this(DOUBLE_PRIMITIVE_ARRAY_TYPE_INFO)

  implicit val mkCharArrayTypeInfo: MkTypeInfo[Array[Char]] =
    this(CHAR_PRIMITIVE_ARRAY_TYPE_INFO)

  implicit val mkStringArrayTypeInfo: MkTypeInfo[Array[String]] =
    this(STRING_ARRAY_TYPE_INFO)
}