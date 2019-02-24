package io.github.aelesbao.yelp.importer

object YelpDataImporterApp extends App {
  val usage =
    s"""
       |spark-submit --class ${getClass.getName.dropRight(1)} <jar> YELP_DATA
       |
       |YELP_DATA     path to Yelp JSON Dataset in any Hadoop supported file system
    """.stripMargin

  args.headOption match {
    case Some(dataPath) =>
      new YelpDataImporterJob(dataPath).run()

    case None =>
      println(usage)
      throw new IllegalArgumentException("YELP_DATA_TAR")
  }
}
