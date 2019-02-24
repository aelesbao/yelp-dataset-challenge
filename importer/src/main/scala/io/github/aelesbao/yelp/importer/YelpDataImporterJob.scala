package io.github.aelesbao.yelp.importer

import io.github.aelesbao.yelp.SparkJob

private class YelpDataImporterJob(dataPath: String) extends SparkJob {
  override def run(): Unit = {
    println(s"Spark version: ${spark.version}")
  }
}
