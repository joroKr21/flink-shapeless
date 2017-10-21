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
import api.java.typeutils.ObjectArrayTypeInfo
import api.scala.typeutils._
import api.scala.derived.typeutils._

import scala.collection.SortedMap
import scala.collection.generic._
import scala.collection.immutable
import scala.collection.mutable
import scala.reflect._

/** `TypeInformation` instances for `Array`, `Traversable` and `Map`. */
trait MkTypeInfo4_Traversable extends MkTypeInfo5_Value {

  /** Creates `TypeInformation` for a (non-primitive) [[Array]]. */
  implicit def mkArrayTypeInfo[E](implicit tiE: TypeInformation[E]): MkTypeInfo[Array[E]] =
    MkTypeInfo(ObjectArrayTypeInfo.getInfoFor(tiE))

  /** Creates `TypeInformation` for a `Traversable` collection. */
  implicit def mkTraversableTypeInfo[T[e] <: Traversable[e], E](
    implicit
    tiE: TypeInformation[E],
    tag: ClassTag[T[E]],
    cbf: CanBuild[E, T[E]],
    gen: T[E] <:< GenericTraversableTemplate[E, T]
  ): MkTypeInfo[T[E]] = MkTypeInfo {
    // Workaround `CanBuildFrom` not being `Serializable`.
    new TraversableTypeInfo[T[E], E](tag.runtimeClass.asInstanceOf[Class[T[E]]], tiE) {
      val empty = cbf().result()
      def createSerializer(config: ExecutionConfig) =
        new TraversableSerializer[T[E], E](elementTypeInfo.createSerializer(config)) {
          override def toString = s"TraversableSerializer[$elementSerializer]"
          def getCbf = new CanBuildFrom[T[E], E, T[E]] {
            def apply(from: T[E]) = gen(from).genericBuilder
            def apply() = apply(empty)
          }
        }
    }
  }

  /** Creates `TypeInformation` for an (immutable) `Map`. */
  implicit def mkMapTypeInfo[K, V](
    implicit tiKV: TypeInformation[(K, V)]
  ): MkTypeInfo[Map[K, V]] = mkInjectTypeInfo(
    Inject(_.toSeq, (seq: Seq[(K, V)]) => seq.toMap), seqTypeInfo, classTag
  )

  /** Creates `TypeInformation` for a `mutable.Map`. */
  implicit def mkMutableMapTypeInfo[K, V](
    implicit tiKV: TypeInformation[(K, V)]
  ): MkTypeInfo[mutable.Map[K, V]] = mkInjectTypeInfo(
    Inject(_.toSeq, (seq: Seq[(K, V)]) => mutable.Map(seq: _*)), seqTypeInfo, classTag
  )

  /** Creates `TypeInformation` for a `SortedMap`. */
  implicit def mkSortedMapTypeInfo[K, V](
    implicit tiKV: TypeInformation[(K, V)], ord: Ordering[K]
  ): MkTypeInfo[SortedMap[K, V]] = mkInjectTypeInfo(
    Inject(_.toSeq, (seq: Seq[(K, V)]) => SortedMap(seq: _*)), seqTypeInfo, classTag
  )

  /** Creates `TypeInformation` for an `immutable.TreeMap`. */
  implicit def mkImmutableTreeMapTypeInfo[K, V](
    implicit tiKV: TypeInformation[(K, V)], ord: Ordering[K]
  ): MkTypeInfo[immutable.TreeMap[K, V]] = mkInjectTypeInfo(
    Inject(_.toSeq, (seq: Seq[(K, V)]) => immutable.TreeMap(seq: _*)), seqTypeInfo, classTag
  )

  /** Creates `TypeInformation` for an `immutable.HashMap`. */
  implicit def mkImmutableHashMapTypeInfo[K, V](
    implicit tiKV: TypeInformation[(K, V)]
  ): MkTypeInfo[immutable.HashMap[K, V]] = mkInjectTypeInfo(
    Inject(_.toSeq, (seq: Seq[(K, V)]) => immutable.HashMap(seq: _*)), seqTypeInfo, classTag
  )

  /** Creates `TypeInformation` for a `mutable.HashMap`. */
  implicit def mkMutableHashMapTypeInfo[K, V](
    implicit tiKV: TypeInformation[(K, V)]
  ): MkTypeInfo[mutable.HashMap[K, V]] = mkInjectTypeInfo(
    Inject(_.toSeq, (seq: Seq[(K, V)]) => mutable.HashMap(seq: _*)), seqTypeInfo, classTag
  )

  /** Used to inject `Map` types into `Seq`. */
  private def seqTypeInfo[E](implicit tiE: TypeInformation[E]): TypeInformation[Seq[E]] =
    mkTraversableTypeInfo[Seq, E].apply()
}