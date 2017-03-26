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

object TypeInfoBench extends Bench.OfflineReport {
  import ADTs._
  import Arbitraries._

  override def executor = LocalExecutor(warmer, aggregator, measurer)
  override def persistor = SerializationPersistor()

  val config = new ExecutionConfig
  val sizes  = for {
    n <- Gen.range("Number of records")(100, 500, 100)
    s <- Gen.enumeration("Record size")(50, 100)
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

  performance of "derived TypeSerializer" in {
    import Implicits._
    bench [Tree[Int]]
    bench [BTree[Int]]
  }

  performance of "default TypeSerializer" in {
    bench [Tree[Int]]
    bench [BTree[Int]]
  }
}
