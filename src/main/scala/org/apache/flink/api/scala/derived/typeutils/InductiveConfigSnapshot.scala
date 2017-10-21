package org.apache.flink
package api.scala.derived.typeutils

import api.common.ExecutionConfig
import api.common.typeutils._
import api.java.typeutils.runtime.kryo.KryoSerializer
import core.memory._

/** Configuration snapshot for recursive case classes (products). */
class InductiveConfigSnapshot(
  var components: Seq[(TypeSerializer[_], TypeSerializerConfigSnapshot)] = Seq.empty
) extends TypeSerializerConfigSnapshot with InductiveObject {
  import InductiveConfigSnapshot._

  def getVersion: Int = version

  // This is a cyclic object so for now we delegate serialization to Kryo.
  override def write(out: DataOutputView): Unit = {
    super.write(out)
    kryo.serialize(this, out)
  }

  // This is a cyclic object so for now we delegate deserialization to Kryo.
  override def read(in: DataInputView): Unit = {
    super.read(in)
    components = kryo.deserialize(in).components
  }

  def canEqual(that: Any): Boolean =
    that.isInstanceOf[InductiveConfigSnapshot]

  override def equals(other: Any): Boolean = other match {
    case that: InductiveConfigSnapshot =>
      (this eq that) || (that canEqual this) && this.components == that.components
    case _ => false
  }

  override def hashCode: Int =
    inductive(0)(31 * components.##)
}

object InductiveConfigSnapshot {
  val version = 1
  val kryo = new KryoSerializer(classOf[InductiveConfigSnapshot], new ExecutionConfig)
}
