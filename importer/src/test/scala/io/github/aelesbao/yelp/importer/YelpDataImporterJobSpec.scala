package io.github.aelesbao.yelp.importer

import java.io.File

import org.apache.spark.sql.test.SharedSparkSession
import org.apache.spark.SparkConf
import org.scalatest.{FlatSpec, Matchers}

class YelpDataImporterJobSpec extends FlatSpec with Matchers with SharedSparkSession {
  behavior of "The YelpDataImporter job"

  val sampleDataPath: String = getClass.getResource("/sample_yelp_dataset.tar.gz").getPath

  it should "create 'yelp' database" in {
    val job = new YelpDataImporterJob(sampleDataPath)
    job.run()

    spark.catalog.databaseExists("yelp") shouldBe true
  }

  it should "creates one Hive table for each json file" in {
    val job = new YelpDataImporterJob(sampleDataPath)
    job.run()

    val tables = spark.catalog.listTables("yelp").collect()
    tables.map(_.name) should contain only(
      "business",
      "user",
      "checkin",
      "tip",
      "review",
      "photo"
    )
  }

  protected override def afterEach(): Unit = {
    super.afterEach()
    spark.sql("drop database if exists yelp cascade")
  }

  override protected def sparkConf: SparkConf = super.sparkConf
    .set("spark.sql.warehouse.dir", new File("spark-warehouse").getAbsolutePath)
    .set("spark.sql.catalogImplementation", "hive")
}
