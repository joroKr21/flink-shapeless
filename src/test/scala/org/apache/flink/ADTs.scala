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

import api.common.typeinfo.TypeInformation

/** Data definitions. Must be separate due to SI-7046. */
object ADTs {

  /** [[Exception]] with structural equality. */
  case class Err(msg: String) extends Exception(msg)

  object WeekDay extends Enumeration {
    val Mon, Tue, Wed, Thu, Fri, Sat, Sun = Value
  }

  /** Non-recursive product */
  case class Account(name: String, money: BigInt)

  /** Recursive product. */
  case class Tree[@specialized(Int) +E](value: E, children: List[Tree[E]]) {
    def size: Int = 1 + children.foldLeft(0)(_ + _.size)
  }

  /** Non-recursive coproduct. */
  sealed trait Fruit
  case class Apple(color: (Int, Int, Int)) extends Fruit
  case class Banana(ripe: Boolean) extends Fruit

  /** Recursive coproduct. */
  sealed trait BTree[@specialized(Int) +E] { def size: Int }
  case object BLeaf extends BTree[Nothing] { def size = 0 }
  case class BNode[@specialized(Int) +E](
    left: BTree[E], value: E, right: BTree[E]
  ) extends BTree[E] {
    def size = 1 + left.size + right.size
  }

  /** Has a custom non-orphan [[TypeInformation]] instance. */
  case class Foo(x: Int)
  object Foo {
    implicit val info: TypeInformation[Foo] = null
  }
}
