package io.github.aelesbao.yelp.importer

import org.apache.spark.sql.test.SharedSparkSession
import org.scalatest.{FlatSpec, Matchers}

class YelpDataImporterJobSpec extends FlatSpec with Matchers with SharedSparkSession {
  behavior of "The YelpDataImporter job"

  it should "run the job" in {
    val dataPath = "/path/to/file"
    val job = new YelpDataImporterJob(dataPath)
    job.run()
  }
}
