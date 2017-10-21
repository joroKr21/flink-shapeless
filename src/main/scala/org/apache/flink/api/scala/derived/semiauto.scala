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

import api.common.typeinfo.TypeInformation
import api.scala.derived.typeinfo.MkTypeInfo

import shapeless._

/** Implicit [[TypeInformation]] instances. */
object semiauto {

  /**
   * If type [[A]] is a (possibly recursive) Algebraic Data Type (ADT), automatically derives a
   * [[TypeInformation]] instance for it.
   *
   * Other implicit instances in scope take higher priority except those provided by
   * [[api.scala.createTypeInformation]] (the macro based approach), because it has a default
   * catch-all case based on runtime reflection.
   *
   * @param mk The derived [[TypeInformation]] provider ([[Strict]] helps avoid divergence).
   * @tparam A A (possibly recursive) Algebraic Data Type (ADT).
   * @return The derived [[TypeInformation]] instance.
   */
  // Derive only when no other implicit instance is in scope.
  def typeInfo[A](implicit mk: Strict[MkTypeInfo[A]]): TypeInformation[A] = mk.value()
}
