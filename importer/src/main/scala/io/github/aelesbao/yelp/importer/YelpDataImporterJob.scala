package io.github.aelesbao.yelp.importer

import io.github.aelesbao.yelp.SparkJob

private class YelpDataImporterJob(dataPath: String) extends SparkJob {
  override protected def appName: String = "Yelp Data Importer"

  override def run(): Unit = {
    println(s"Spark version: ${spark.version}")
  }
}
