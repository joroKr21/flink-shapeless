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

import api.common.ExecutionConfig
import api.common.typeinfo.TypeInformation
import api.scala.typeutils._
import api.scala.derived.typeutils._

import shapeless._
import shapeless.ops.hlist._
import shapeless.ops.record._

import scala.reflect.ClassTag

/** Holder of a generic `TypeInformation` instance. */
sealed trait MkGenericTypeInfo[A] extends (() => TypeInformation[A]) with Serializable

/** Implicit derivation of generic `TypeInformation`. */
object MkGenericTypeInfo {

  /** Creates `TypeInformation` for the (possibly recursive) product `P`. */
  implicit def mkProductTypeInfo[
    P <: Product, R <: HList, K <: HList, V <: HList
  ](implicit
    gen: LabelledGeneric.Aux[P, R],
    unzip: UnzipFields.Aux[R, K, V],
    record: ZipWithKeys.Aux[K, V, R],
    vector: ToTraversable.Aux[K, Vector, Symbol],
    infos: TypeInfos[V],
    list: AsList[R, Any],
    tag: ClassTag[P]
  ): MkGenericTypeInfo[P] = new MkGenericTypeInfo[P] with InductiveObject {
    val clazz = tag.runtimeClass.asInstanceOf[Class[P]]
    val names = for (k <- vector(unzip.keys)) yield k.name
    def apply = inductive(product)(caseClass)

    def product: TypeInformation[P] =
      new ProductTypeInfo(infos().toVector)(
        values => gen.from(values.foldRight[HList](HNil)(_ :: _).asInstanceOf[R]),
        record => list(gen.to(record))
      )

    def caseClass: TypeInformation[P] =
      new CaseClassTypeInfo[P](clazz, Array.empty, infos(), names) {
        def createSerializer(config: ExecutionConfig) = {
          val serializers = for (t <- types) yield t.createSerializer(config)
          new CaseClassSerializer[P](clazz, serializers) {
            def createInstance(fields: Array[AnyRef]) =
              gen.from(record(fields.foldRight[HList](HNil)(_ :: _).asInstanceOf[V]))
            override def toString = s"CaseClassSerializer(${
              names.indices.map(i => s"${names(i)}: ${serializers(i)}").mkString(", ")
            })"
          }
        }
      }
  }

  /** Creates `TypeInformation` for the (possibly recursive) coproduct `C`. */
  implicit def mkCoproductTypeInfo[C, R <: Coproduct](
    implicit
    gen: Generic.Aux[C, R],
    infos: TypeInfos[R],
    index: Which[R],
    tag: ClassTag[C]
  ): MkGenericTypeInfo[C] = new MkGenericTypeInfo[C] {
    def apply = new CoProductTypeInfo(infos().toVector)(index.compose(gen.to))
  }
}