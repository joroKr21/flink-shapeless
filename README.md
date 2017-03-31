# Flink-Shapeless
Flink-Shapeless replaces the default macro based implicit
provider for `TypeInformation[T]` in
[Apache Flink](https://flink.apache.org/)'s Scala API
with automatic type class derivation based on
[Shapeless](https://github.com/milessabin/shapeless).

[![Build Status](https://travis-ci.org/joroKr21/flink-shapeless.svg?branch=master)](https://travis-ci.org/joroKr21/flink-shapeless)
[![codecov](https://codecov.io/gh/joroKr21/flink-shapeless/branch/master/graph/badge.svg)](https://codecov.io/gh/joroKr21/flink-shapeless)

## Usage
The primary use case of Flink-Shapeless is to enable
custom implicit `TypeInformation` instances in scope
to override the default.

```scala
// Import Flink's Scala API as usual
import org.apache.flink.api.scala._
// Replace the macro-based TypeInformation provider
import Implicits._

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
There are a couple of advantages to automatic type class
derivation over the default macro based approach.

### Customizability
Automatic derivation uses a modified version of the
Scala implicit resolution mechanism with lowest priority.
Thus it can be overridden for specific types by providing
an implicit instance anywhere in scope, including in a
companion object as idiomatic in Scala.

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
[Recursive data types](https://en.wikipedia.org/wiki/Recursive_data_type)
or [Coproducts](https://en.wikipedia.org/wiki/Coproduct)
without the use of reflection based serializers like
[Kryo](https://github.com/EsotericSoftware/kryo).
Only product types (tuples and case classes) are handled
natively.

Flink-Shapeless extends the native Flink support to arbitrary
[Algebraic data types](https://en.wikipedia.org/wiki/Algebraic_data_type)
(ADTs) and will fail at compile time rather than default to
runtime reflection. In Scala ADTs are encoded as sealed
traits and case classes.

```scala
// Example: Recursive product
case class Tree[+A](v: A, children: List[Tree[A]])

// Example: Recursive coproduct
sealed trait BTree[+A]
case object BLeaf extends BTree[Nothing]
case class BNode[+A](l: BTree[A], v: A, r: BTree[A]) extends BTree[A]
```
## Limitations
There are a few well known limitations of automatic type
class derivation with Shapeless.

- Long compile times for large case classes and
  sealed trait hierarchies. Your mileage may vary.
- Due to [SI-7046](https://issues.scala-lang.org/browse/SI-7046)
  older versions of Scala may have problems with deriving
  `TypeInformation` for coproducts.
