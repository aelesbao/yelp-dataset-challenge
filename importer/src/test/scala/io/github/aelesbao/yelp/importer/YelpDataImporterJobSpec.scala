package io.github.aelesbao.yelp.importer

import org.apache.spark.sql.test.SharedSparkSession
import org.scalatest.{FlatSpec, Matchers}

class YelpDataImporterJobSpec extends FlatSpec with Matchers with SharedSparkSession {
  behavior of "The YelpDataImporter job"

  val sampleDataPath: String = getClass.getResource("/sample_yelp_dataset.tar.gz").getPath

  it should "run the job" in {
    val job = new YelpDataImporterJob(sampleDataPath)
    job.run()
  }
}
