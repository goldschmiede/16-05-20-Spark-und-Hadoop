import sbt.Keys._
import sbt._

object AssemblySettings {
  import sbtassembly.AssemblyKeys._
  import sbtassembly._

  def assemblySettings: Seq[Setting[_]] = Seq(
    target in assembly <<= target,
    assemblyMergeStrategy in assembly := {
      // works around the horrible idea of spark developers to inline guava classes
      case PathList("com", "google", "common", "base", xs@_*) => new IncludeFromJar("guava-14.0.1.jar")
      case PathList("org", "apache", "spark", "unused", "UnusedStubClass.class") => MergeStrategy.first
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    },
    assemblyExcludedJars in assembly <<= (fullClasspath in assembly) map { cp =>
      val excludes: Set[String] = Set(
        "minlog-1.2.jar"
        , "commons-beanutils-core-1.8.0.jar"
        , "commons-beanutils-1.7.0.jar"
        , "hadoop-yarn-api-2.2.0.jar"
        , "jasper-compiler-5.5.23.jar"
        , "jasper-runtime-5.5.23.jar"
      )

      cp filter (excludes contains _.data.getName)
    }
  )

  // helper class for own merge strategy
  private class IncludeFromJar(val jarName: String) extends MergeStrategy {

    val name = "includeFromJar"

    override def apply(tmp: File, path: String, files: Seq[File]): Either[String, Seq[(File, String)]] = {
      val includedFiles = files.flatMap { f =>
        val (source, _, _, isFromJar) = sbtassembly.AssemblyUtils.sourceOfFileForMerge(tmp, f)
        if (isFromJar && source.getName == jarName) Some(f -> path) else None
      }
      Right(includedFiles)
    }
  }
}