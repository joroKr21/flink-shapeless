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

import org.scalacheck._

/** Data definitions. Must be separate due to SI-7046. */
object ADTsBench {
  import Gen._

  /** Recursive product. */
  case class NTree[@specialized(Int) +E](value: E, children: List[NTree[E]]) {
    def size: Int = 1 + children.foldLeft(0)(_ + _.size)
  }

  object NTree {
    /** Reasonably sized [[Arbitrary]] [[NTree]]s. */
    implicit def arb[E](implicit element: Arbitrary[E]): Arbitrary[NTree[E]] = {
      lazy val tree: Gen[NTree[E]] = lzy(sized {
        size => for {
          value <- element.arbitrary
          children <- if (size <= 1) const(Nil) else for {
            n <- choose(0, size - 1)
            h <- resize(size % (n + 1), tree)
            t <- listOfN(n, resize(size / (n + 1), tree))
          } yield h :: t
        } yield NTree(value, children)
      })
      Arbitrary(tree)
    }
  }

  /** Recursive coproduct. */
  sealed trait BTree[@specialized(Int) +E] { def size: Int }
  case object BLeaf extends BTree[Nothing] { def size = 0 }
  case class BNode[@specialized(Int) +E](
    left: BTree[E], value: E, right: BTree[E]
  ) extends BTree[E] {
    def size = 1 + left.size + right.size
  }

  object BTree {
    /** Reasonably sized [[Arbitrary]] [[BTree]]s. */
    implicit def arb[E](implicit element: Arbitrary[E]): Arbitrary[BTree[E]] = {
      lazy val tree: Gen[BTree[E]] = lzy(sized { size =>
        if (size == 0) BLeaf else for {
          e <- element.arbitrary
          n <- choose(1, size)
          l <- resize(n - 1, tree)
          r <- resize(size - n, tree)
        } yield BNode(l, e, r)
      })
      Arbitrary(tree)
    }
  }
}
