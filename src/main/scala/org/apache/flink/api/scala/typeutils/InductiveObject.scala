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
 * A recursive object supporting the definition of inductive methods which don't cause a
 * StackOverflow. Cycles in the object graph are detected and handled explicitly.
 */
trait InductiveObject {

  // Marker for detecting cycles in the object graph.
  private var cycle = false

  /**
   * Avoids StackOverflow whenever a cycle in the object graph is expected.
   * @param base Induction base (invoked when a cycle is detected).
   * @param step Induction step (invoked until a cycle is reached).
   * @tparam A The type of recursive computation.
   * @return The result of replacing cyclic references with `base` inside `step`.
   */
  protected def inductive[A](base: => A)(step: => A): A =
    if (cycle) base else {
      cycle = true
      val result = step
      cycle = false
      result
    }
}
