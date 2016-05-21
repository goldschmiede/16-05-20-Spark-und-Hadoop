import org.apache.spark.{SparkConf, SparkContext}

/**
  * Basic map-reduce example. We look at what does 'map' and what does 'reduce' do.
  */
object MapReduceMitRDDs extends App {

  // this is our data, sentences represent the natural partitioning
  val data = Seq(
    "the quick brown fox jumped over the fence",
    "the ninja fox jumped over the sleeping dog",
    "the dog nibbled the bone"
  )

  // set up spark - local[3] means that the context will run locally and will possibly use parallelism of 3
  val sparkConfig = new SparkConf().setAppName("SparkAtGoldschmiede").setMaster("local[3]")
  val sparkContext = new SparkContext(sparkConfig)

  // introduce data to spark context... by producing RDD
  val dataSet = sparkContext.parallelize(data)

  val sortedNames = dataSet
    .map(entry => entry.split(" ")) // map
    .flatMap(e => e) // flastten out
    .map(entry => entry -> 1) // add count
    .reduceByKey { case (a, b) => a + b } // reduce count
    .sortBy(e => e._2, ascending = false) // sort by count descending
    .take(3) // take top 3

  println(s"\nI have counted these words:")
  sortedNames.foreach(entry => println(s"I have ${entry._2} of '${entry._1}'"))
  println(s"\n")
}
