import org.apache.spark.{SparkConf, SparkContext}

/**
  * Here we look at what RDDs are in more detail. Particularly, we look at
  * <br>- immutability</span>
  * <br>- partitioning</span>
  * <br>- coarse-grained operations
  * <br>- lazy evaluation
  * <br>- persistence.
  *
  * <br>We don't look at fault tolerance in the code example, since its hard to show.
  */
object WasSindRDDs extends App {

  // this is our data
  val data = Seq(
    "the quick brown fox jumped over the fence",
    "the ninja fox jumped over the sleeping dog",
    "the dog nibbled the bone"
  )

  // setting up spark
  val sparkConfig = new SparkConf().setAppName("SparkAtGoldschmiede").setMaster("local[8]")
  val sc = new SparkContext(sparkConfig)

  /** immutability **/
  // produce rdd from data, the produce rdd is immutable
  val dataSet = sc.parallelize(data, numSlices = 3)

  // doing simple transformation, uncomment the println to observe when it is being called
  val tokens = dataSet.flatMap{entry =>
    //println(s"I am being evaluated")
    entry.split(" ").map(element => element -> 1)
  }

  /** persistence **/
  // uncomment to observe effect of persisting rdd
  //.persist()

  /** transformations & actions **/
  // in combination with uncommented print statement in flat map and persist above will demonstrate
  // difference between transformations and actions
  println(s"Collecting tokens first time")
  println(s"Got these tokens: ${tokens.collect().mkString(" : ")}")
  println(s"Collecting tokens second time")
  println(s"Got these tokens occuring more than once: ${tokens.reduceByKey(_ + _).filter{case (token, occurence) => occurence > 1}.collect().mkString(" : ")}")

  // good practice is unpersist persisted rdd as soon as you don't need it anymore
  //tokens.unpersist()

  /** partitioning **/
  // re-partition data into 6 partitions
  val differentPartitioning = tokens.coalesce(6, shuffle = true)
  differentPartitioning.foreachPartition(elements => println(s"Partition contains this: ${elements.mkString}"))

  /** coarse-grained operations **/
  tokens
  // uncomment separately to demonstrate different transformations such as map, distinct and filter
//    .map { case (word, count) =>
//      count -> word
//    }
//    .distinct()
//    .filter { case (word, count) =>
//      word(0) >= 'o'
//    }
    .collect()
    .foreach(e => println(s"Found word '${e._1}'"))

  /** lazy evaluation **/
  // print statement will never be executed because it is transformation and there is no action after it. Test it.
  tokens.map { case (word, count) =>
    println(s"Found word '$word'")
    word -> count
  }
}
