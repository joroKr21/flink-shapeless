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
import api.scala.typeutils._

import shapeless._
import shapeless.ops.hlist._
import shapeless.ops.record._

import scala.annotation.implicitNotFound
import scala.reflect.ClassTag

/** An automatically derived [[TypeInformation]] provider. */
@implicitNotFound("could not automatically derive TypeInformation[${A}]")
trait MkTypeInfo[A] extends (() => TypeInformation[A])

/** Implicit [[MkTypeInfo]] instances. */
object MkTypeInfo extends MkTypeInfo0_ADT {

  /** Summons an implicit [[MkTypeInfo]] instance in scope. */
  def apply[A: MkTypeInfo]: MkTypeInfo[A] = implicitly
}

/** [[MkTypeInfo]] instances for (possibly recursive) Algebraic Data Types (ADTs). */
trait MkTypeInfo0_ADT extends MkTypeInfo1_CaseClass {
  implicit def product[P <: Product: Recursive: ClassTag, R <: HList, T <: HList](
    implicit gen: Generic.Aux[P, R], fields: TypeInfos[R], list: AsList[R, Any]
  ): MkTypeInfo[P] = mk {
    new ProductTypeInfo(fields().toVector)(
      values => gen.from(values.foldRight[HList](HNil)(_ :: _).asInstanceOf[R]),
      record => list(gen.to(record)))
  }

  implicit def coproduct[C: ClassTag, R <: Coproduct](
    implicit gen: Generic.Aux[C, R], variants: TypeInfos[R], index: Which[R]
  ): MkTypeInfo[C] = mk {
    new CoproductTypeInfo(variants().toVector)(index.compose(gen.to))
  }
}

/** [[MkTypeInfo]] instances for non-recursive case classes. */
trait MkTypeInfo1_CaseClass extends MkTypeInfoZ {
  implicit def caseClass[P <: Product, R <: HList, K <: HList, V <: HList, T <: HList](
    implicit
    tag: ClassTag[P],
    gen: LabelledGeneric.Aux[P, R],
    unzip: UnzipFields.Aux[R, K, V],
    record: ZipWithKeys.Aux[K, V, R],
    infos: LiftAll.Aux[TypeInformation, V, T],
    vector: ToTraversable.Aux[K, Vector, Symbol],
    array: ToArray[T, TypeInformation[_]]
  ): MkTypeInfo[P] = {
    val clazz  = tag.runtimeClass.asInstanceOf[Class[P]]
    val names  = for (k <- vector(unzip.keys)) yield k.name
    val fields = array(infos.instances)
    mk(new CaseClassTypeInfo[P](clazz, Array.empty, fields, names) {
      def createSerializer(config: ExecutionConfig) = {
        val serializers = for (f <- fields) yield f.createSerializer(config)
        new CaseClassSerializer[P](clazz, serializers) {
          def createInstance(fields: Array[AnyRef]) =
            gen.from(record(fields.foldRight[HList](HNil)(_ :: _).asInstanceOf[V]))
          override def toString = s"CaseClassSerializer(${
            names.indices.map(i => s"${names(i)}: ${serializers(i)}").mkString(", ")
          })"
        }
      }
    })
  }
}

/** Lowest priority [[MkTypeInfo]] instances. */
trait MkTypeInfoZ {
  protected def mk[A](instance: => TypeInformation[A]): MkTypeInfo[A] =
    new MkTypeInfo[A] { def apply = instance }
}
