import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkContext, SparkConf}
import org.scalatest.{BeforeAndAfter, GivenWhenThen, FlatSpec, Matchers}

/***
  * Basic configuration test. Tests if sbt has downloaded all dependencies,
  * spark context creation is ok and sql server is accessible.
  */
class ConfigurationTest extends FlatSpec with Matchers with GivenWhenThen with BeforeAndAfter {

  val config = ConfigFactory.load()
  var sparkContext: SparkContext = null

  before {
    val sparkConfig = new SparkConf().setAppName("SparkAtGoldschmiede").setMaster("local[1]")
    sparkContext = new SparkContext(sparkConfig)
  }

  after {
    sparkContext.stop()
  }

  it should "instantiate spark context and test if execution is possible" in {
    Given("data")
    val data = Seq("apple:fruit:food", "carrot:vegetable:food", "cheese:milkproduct:food", "bread::food",
      "hare:animal:vegetarian", "lion:animal:carnivore", "owl:animal:universal", "bear:animal:universal")

    When("instantiating spark context")

    And("parallelizing data with spark")
    val dataSet = sparkContext.parallelize(data)

    Then("RDD operations should succeed")
    dataSet.count() should be (8)
  }

  it should "instantiate spark context and test if previously created sql data can ne accessed" in {
    Given("previously created sql data")

    When("instantiating spark sql context")
    val sqlContext = new SQLContext(sparkContext)

    And("creating data frame from sql table city")
    val dataFrame = sqlContext.read
      .format("jdbc")
      .option("url", config.getString("sqlUrl"))
      .option("dbtable", "city")
      .load()

    Then("RDD operation should succeed")
    dataFrame.map(row => row.getAs[String]("name")).count() should be > 0L
  }
}
