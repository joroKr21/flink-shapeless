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

import api.scala.typeutils._
import api.scala.derived.typeutils._

import shapeless.Witness

import scala.reflect.ClassTag

/** `TypeInformation` instances for `Singleton` objects. */
private[typeinfo] abstract class MkTypeInfo6_Singleton extends MkTypeInfo7_Factory {

  /** Creates `TypeInformation` for the singleton object `S`. */
  implicit def mkSingletonTypeInfo[S](
    implicit sng: Witness.Aux[S], tag: ClassTag[S]
  ): MkTypeInfo[S] = mkInjectTypeInfo[S, Unit](
    Inject(_ => (), _ => sng.value), new UnitTypeInfo, tag
  )
}