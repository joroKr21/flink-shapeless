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
package api.scala.typeutils

/**
 * Interface for an injective (i.e. invertible) function.
 * Can be used to provide a `TypeInformation` instance for `A`
 * by injecting it into a `TypeInformation` instance for `B`.
 */
trait Inject[A, B] extends (A => B) with Serializable {
  def invert(b: B): A
}

/** [[Inject]] instances. */
object Inject {
  /** Creates an `Inject[A, B]` instance, given implicit conversions between `A` and `B`. */
  def apply[A, B](implicit f: A => B, g: B => A): Inject[A, B] = new Inject[A, B] {
    def apply(a: A) = f(a)
    def invert(b: B) = g(b)
  }
}
