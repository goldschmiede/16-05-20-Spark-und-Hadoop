import AssemblySettings._

name := "SparkAtGoldschmiede"

version := "1.0"

scalaVersion := "2.10.6"

assemblySettings

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "1.3.1" % "provided",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test",
  "org.postgresql" % "postgresql" % "9.4.1208.jre7",
  "org.apache.spark" % "spark-sql_2.10" % "1.3.1" % "provided",
  "org.apache.spark" % "spark-mllib_2.10" % "1.3.1" % "provided",
  "org.apache.hadoop" % "hadoop-common" % "2.5.1-mapr-1503",
  "org.apache.hadoop" % "hadoop-hdfs" % "2.5.1-mapr-1503",
  "com.mapr.hadoop" % "maprfs" % "4.1.0-mapr"
).map(_.excludeAll(Seq(ExclusionRule("commons-logging"), ExclusionRule("javax.servlet")): _*))


