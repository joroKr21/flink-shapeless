# Flink-Shapeless
Flink-Shapeless replaces the default macro based implicit provider for
`TypeInformation[T]` in [Apache Flink](https://flink.apache.org/)'s Scala API
with automatic type class derivation based on
[Shapeless](https://github.com/milessabin/shapeless).

[![Build Status](https://travis-ci.org/joroKr21/flink-shapeless.svg?branch=master)](https://travis-ci.org/joroKr21/flink-shapeless)
[![codecov](https://codecov.io/gh/joroKr21/flink-shapeless/branch/master/graph/badge.svg)](https://codecov.io/gh/joroKr21/flink-shapeless)

## Usage
The primary use case of Flink-Shapeless is to enable custom implicit
`TypeInformation` instances in scope to override the default.

```scala
// Import Flink's Scala API as usual
import org.apache.flink.api.scala._
// Replace the macro-based TypeInformation provider
import FlinkShapeless._

// Override TypeInformation[String]
implicit val strTypeInfo = MyASCIIStringTypeInfo

// Strings below are serialized with ASCII encoding,
// even when nested in tuples, data structures, etc.
val env = ExecutionEnvironment.getExecutionEnvironment
val text = env.readTextFile("/path/to/file")
val counts = text
  .flatMap(_.toLowerCase.split("\\W+"))
  .filter(_.nonEmpty).map(_ -> 1)
  .groupBy(0).sum(1)
```

## Advantages
There are a couple of advantages to automatic type class derivation over the default macro based approach.

### Customizability
Automatic derivation uses a modified version of the Scala implicit resolution mechanism with lowest priority. Thus it can be overridden for specific types by providing an implicit instance anywhere in scope, including in a companion object as idiomatic in Scala.

```scala
case class Foo(x: Int)
object Foo {
  implicit val info: TypeInformation[Foo] =
    MyOptimizedFooTypeInfo
}

case class Bar(foo: Foo, y: Double)

// All instances below use the optimized version.
implicitly[TypeInformation[Foo]]
implicitly[TypeInformation[List[Foo]]]
implicitly[TypeInformation[(Foo, Long)]]
implicitly[TypeInformation[Bar]]
```

### Recursive ADTs
The default macro based implementation cannot handle
[Recursive data types](https://en.wikipedia.org/wiki/Recursive_data_type) or
[Coproducts](https://en.wikipedia.org/wiki/Coproduct) without the use of reflection based serializers like
[Kryo](https://github.com/EsotericSoftware/kryo). Only product types (tuples and case classes) are handled natively.

Flink-Shapeless extends the native Flink support to arbitrary
[Algebraic data types](https://en.wikipedia.org/wiki/Algebraic_data_type)
(ADTs) and will fail at compile time rather than default to runtime reflection. In Scala ADTs are encoded as sealed traits and case classes.

```scala
// Example: Recursive product
case class NTree[+A](v: A, children: List[NTree[A]])

// Example: Recursive coproduct
sealed trait BTree[+A]
case object BLeaf extends BTree[Nothing]
case class BNode[+A](l: BTree[A], v: A, r: BTree[A]) extends BTree[A]
```

## Benchmarks
Checkout the `TypeSerializer` [microbenchmarks](https://jorokr21.github.io/flink-shapeless/docs/benchmarks/report/index.html#config=%7B%22filterConfig%22%3A%7B%22curves%22%3A%5B%22-1%22%2C%220%22%2C%221%22%2C%222%22%2C%223%22%5D%2C%22order%22%3A%5B%22param-%23+Trees%22%2C%22param-%23+Nodes%22%2C%22date%22%5D%2C%22filters%22%3A%5B%5B%22100%22%2C%22200%22%2C%22300%22%2C%22400%22%2C%22500%22%5D%2C%5B%22100%22%2C%2250%22%5D%2C%5B%221491588448000%22%5D%5D%7D%2C%22chartConfig%22%3A%7B%22type%22%3A0%2C%22showCI%22%3Atrue%7D%7D) comparing the default (Kryo) with the derived (via Shapeless) serializer on the `NTree` and `BTree` examples above. Flink-Shapeless achieves up to 10x speedup for `NTree` and up to 3x speedup for `BTree`.

More details about the setup:
- Single threaded bulk serialization -> deserialization roundtrip
- Random data generated with [ScalaCheck](https://www.scalacheck.org/)
- Varying number of trees (100-500) and number of nodes per tree (50-100)
- Run on my development laptop with [ScalaMeter](http://scalameter.github.io/)

## Limitations
There are a few well known limitations of automatic type class derivation with Shapeless.

- Long compile times for large case classes and sealed trait hierarchies. Your mileage may vary.
- Due to [SI-7046](https://issues.scala-lang.org/browse/SI-7046) older versions of Scala may have problems with deriving `TypeInformation` for coproducts.
