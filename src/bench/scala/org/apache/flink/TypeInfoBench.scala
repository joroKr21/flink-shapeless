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

import api.common.ExecutionConfig
import api.common.typeinfo.TypeInformation
import api.scala._
import core.memory._

import org.scalacheck.Arbitrary
import org.scalacheck.Gen._
import org.scalacheck.rng.Seed
import org.scalameter.api._
import org.scalameter.picklers.Implicits._

import java.io._

object TypeInfoBench extends Bench.OnlineRegressionReport {
  import ADTsBench._

  val config = new ExecutionConfig
  val sizes  = for {
    n <- Gen.range("# Trees")(100, 500, 100)
    s <- Gen.enumeration("# Nodes")(50, 100)
  } yield (n, s)

  def bench[R](implicit arb: Arbitrary[R], info: TypeInformation[R]) = {
    val tpe = info.getTypeClass.getSimpleName
    val serializer = info.createSerializer(config)
    println(s"[info] Benchmarking $serializer")
    using(sizes) curve tpe in { case (n, s) =>
      val data = listOfN(n, resize(s, arb.arbitrary))
        .pureApply(Parameters.default, Seed.random)

      for { // Serialize all records to a byte array and read them back.
        os <- resource.managed(new ByteArrayOutputStream)
        ov <- resource.managed(new DataOutputViewStreamWrapper(os))
        _ = for (record <- data) serializer.serialize(record, ov)
        is <- resource.managed(new ByteArrayInputStream(os.toByteArray))
        iv <- resource.managed(new DataInputViewStreamWrapper(is))
      } for (_ <- 1 to n) serializer.deserialize(iv)
    }
  }

  performance of "Flink-Shapeless" in {
    import FlinkShapeless._
    bench [NTree[Int]]
    bench [BTree[Int]]
  }

  performance of "Kryo serializer" in {
    bench [NTree[Int]]
    bench [BTree[Int]]
  }
}
