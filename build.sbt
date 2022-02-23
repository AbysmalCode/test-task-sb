import Dependencies._

scalaVersion := "2.11.8"  
 
resolvers += Resolver.bintrayIvyRepo("com.eed3si9n", "sbt-plugins")
 
libraryDependencies += "org.apache.spark" %% "spark-core" % "2.4.5"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.4.5"
libraryDependencies += "org.apache.spark" %% "spark-graphx" % "2.4.5"
// libraryDependencies += "com.lihaoyi" % "ammonite_2.11.7" % "1.6.7-2-c28002d"



// sourceGenerators in Test += Def.task {
//   val file = (sourceManaged in Test).value / "amm.scala"
//   IO.write(file, """object amm extends App { ammonite.AmmoniteMain.main(args) }""")
//   Seq(file)
// }.taskValue

// (fullClasspath in Test) ++= {
//   (updateClassifiers in Test).value
//     .configurations
//     .find(_.configuration.name == Test.name)
//     .get
//     .modules
//     .flatMap(_.artifacts)
//     .collect{case (a, f) if a.classifier == Some("sources") => f}
// }


// ThisBuild / scalaVersion     := "2.12.8"
// ThisBuild / version          := "0.1.0-SNAPSHOT"
// ThisBuild / organization     := "com.example"
// ThisBuild / organizationName := "example"

// lazy val root = (project in file("."))
//   .settings(
//     name := "swissBorg",
//     libraryDependencies += scalaTest % Test,
//     libraryDependencies += "org.apache.spark" %% "spark-core" % "1.5.2",
//     libraryDependencies += "org.apache.spark" %% "spark-graphx" % "1.5.2",
//     libraryDependencies += "org.apache.spark" %% "spark-sql" % "1.5.2" % "provided",
//   )
