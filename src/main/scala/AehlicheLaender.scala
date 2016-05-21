import breeze.linalg._
import breeze.numerics._
import com.typesafe.config.ConfigFactory
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkContext, SparkConf}

/**
  * Based on sql data, this example attempts to find similar countries.
  * Question is - on what is the similarity based on?
  */
object AehlicheLaender extends App {
  // we will use configuration to read sql url from config file
  val config = ConfigFactory.load()

  // we set up spark
  val sparkConfig = new SparkConf().setAppName("SparkAtGoldschmiede").setMaster("local[8]")
  val sparkContext = new SparkContext(sparkConfig)
  val sqlContext = new SQLContext(sparkContext)

  // produce countries data frame from sql
  val countries = sqlContext.load("jdbc", Map(
    "url" -> config.getString("sqlUrl"),
    "dbtable" -> "country"))

  // select features
  val features = countries.select("name", "surfacearea", "population", "lifeexpectancy", "gnp", "gnpold")

  // quantify features
  val quantifiedFeatures: RDD[(String, Vector[Double])] = features.flatMap { case row =>
    row.anyNull match {
      // ignore rows with incomplete data
      case true => None
      case false =>
        val surface = row.getDouble(1)
        val population = row.getInt(2).toDouble
        val lifeExpectancy = row.getDouble(3)
        val gnp = row.getDecimal(4).doubleValue()
        val gnpOld = row.getDecimal(5).doubleValue()

        // gnp per capita importance is increased and surface importance decreased
        val featureArray = Array[Double]((gnp / population) * 1000, lifeExpectancy, log(surface))

        // we could optionally use normalization, how does it effect results
        val normalizedFeatures: Vector[Double] = DenseVector[Double](featureArray).toVector

        // return tuple with country name and feature vector
        Option(row.getString(0) -> normalizedFeatures)
    }
  }

  /*****************************************************************/
  /* KMeans is clustering algorithm provided by Spark's MLLib.     */
  /* Try it out and compare results with the self-made similarity. */
  /*****************************************************************/

//  import org.apache.spark.mllib.clustering.KMeans
//  import org.apache.spark.mllib.linalg.Vectors
//
//  // drop country name and convert vector to mllib vector that is required by kmeans
//  val trainingData = quantifiedFeatures.map { case (_, countryFeatures) =>
//    Vectors.dense(countryFeatures.toArray)
//  }.cache()
//
//  val kmeansModel = new KMeans().setK(30)
//    .setMaxIterations(100)
//    .setInitializationMode(KMeans.K_MEANS_PARALLEL)
//    .setEpsilon(1e-60)
//    .setInitializationSteps(5)
//    .run(trainingData)
//
//  val clusteredCountries = quantifiedFeatures.map(country =>
//    kmeansModel.predict(Vectors.dense(country._2.toArray)) -> s"'${country._1}'")
//
//  clusteredCountries.reduceByKey((a, b) => s"$a, $b").foreach(cluster => println(s"These countries belong together: ${cluster._2}"))

  /************************************************************************************************/
  /* A primitive self-made similarity algorithm.                                                  */
  /* You can compare results with Spark MLLib kmeans clustering by uncommenting code block above. */
  /************************************************************************************************/

  // broadcast features so we can use them inside transformations on every node
  val quantifiedFeaturesB = sparkContext.broadcast(quantifiedFeatures.collect)

  // calculate similarity
  val similarities = quantifiedFeatures
    .cartesian(quantifiedFeatures) // produce cartesian product (full including relation with self)
    .map { case ((leftCountryName, leftCountryFeatures), (rightCountryName, rightCountryFeatures)) =>
      val similarity = if (leftCountryName != rightCountryName) {
        // apply cosine similarity, maybe other similarity models are better
        CosineSimilarity.calc(leftCountryFeatures, rightCountryFeatures)
      } else 0d // self similarity is 0 so that it can be removed

      // the key is left country, the value tupple from right country and
      // the similarity between both countries
      leftCountryName -> (rightCountryName -> similarity)
    }
    .filter(_._2._2 != 0d) // remove 0 similarities
    // using aggregate instead of reduce, because we want to change return type
    .aggregateByKey(Array[(String, Double)]())({ case (seq, countrySim) => seq :+ countrySim }, { case (left, right) => left ++ right })
    // cleaning up the values by sorting them by similarity and taking only top 5 matches
    .mapValues { case sims =>
      sims.sortBy(-_._2).take(5)
    }

  // output similar countries
  similarities
    // filter list for my favorites
    .filter(e => e._1 == "United States" || e._1 == "Germany" || e._1 == "Latvia" || e._1 == "China" || e._1 == "Russian Federation" || e._1 == "Norway")
    .foreach(sim => println(s"Für ${sim._1} die top 5 ähnlichsten sind ${sim._2.map(_._1).mkString("'", "', '", "'")}"))
}

/**
  * Quick and dirty cosine similarity
  */
object CosineSimilarity {
  def calc(left: Vector[Double], right: Vector[Double]): Double = {
    // the dot product is sum over element-wise multiplication of both vectors
    val dotProd: Double = sum(left :* right)
    // magnitude is a square root of multiplication of dot products of both vectors with themselves
    val magnitude: Double = sqrt(sum(left :* left)) * sqrt(sum(right :* right))
    // then cosine similarity is dot product divided by magnitude
    dotProd / magnitude
  }
}
