import org.apache.spark.{SparkConf, SparkContext}

/**
  * This example is intended to be run on mesos. Launch it with this command on driver node.
  * spark-submit --executor-memory 50M --driver-memory 50M --conf spark.mesos.coarse=true --total-executor-cores 2 \
  * --deploy-mode client --class "MesosBeispiel" test-assembly-1.0.jar
  */
object MesosBeispiel extends App {

  // we set up spark
  val sparkConfig = new SparkConf().setAppName("SparkAtGoldschmiede")
  // uncomment this to run example locally instead
//    .setMaster("local[16]")
  val sparkContext = new SparkContext(sparkConfig)

  /* this is our data */
  // uncomment this to run example locally instead
//  val dataSet = sparkContext.textFile("file:///path/to/SparkAtGoldschmiede/src/main/resources/many/*")
  // comment this out to run example locally instead
  val dataSet = sparkContext.textFile("maprfs:///demo/*")

  val t1 = System.currentTimeMillis()

  // mapreduce example
  val countingTypes = dataSet
    .flatMap(entry => entry.trim.toLowerCase.split("""[\p{Punct}\s]+""")) // basic tokenization
    .filter(_.nonEmpty) // remove empty tokens
    .map(entry => entry -> 1) // add count to tokens
    .reduceByKey(_ + _) // reduce count (actually add up)
    .sortBy(_._2, ascending = true) // sort by count
    .collect() // action

  println(s"Done in ${System.currentTimeMillis() - t1}ms")

  // uncomment next two lines to actually see the output
//  println(s"\n\nI have counted these words:")
//  countingTypes.foreach(entry => println(s"found ${entry._2}x '${entry._1}'"))
}
