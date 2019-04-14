package io.github.aelesbao.yelp.importer

import io.github.aelesbao.yelp.{SparkJob, TarArchiveEntry}
import io.github.aelesbao.yelp.Implicits._
import org.apache.spark.sql.{Dataset, SaveMode}

private class YelpDataImporterJob(dataPath: String) extends SparkJob {

  import spark.implicits._

  override protected def appName: String = "Yelp Data Importer"

  private val database: String = "yelp"

  override def run(): Unit = {
    createDatabase()

    logger.info(s"Reading Yelp Dataset from $dataPath")
    val files = sc.tarGzFiles(dataPath)
      .filterByFileName(".*.json$")
      .cache()

    files.values.collect()
      .foreach { entry =>
        val table = getTableName(entry)
        val lines = files.extractLines(entry).values.toDS()
        writeToHive(table, lines)
      }
  }

  private def createDatabase(): Unit = {
    if (!spark.catalog.databaseExists(database))
      spark.sql(s"create database $database")
  }

  private def writeToHive(table: String, lines: Dataset[String]): Unit = {
    logger.info(s"Saving table $database.$table")

    spark.read.json(lines.cache())
      .write
      .format("parquet")
      .mode(SaveMode.Overwrite)
      .saveAsTable(s"$database.$table")
    logger.info(s"Saved records to $database.$table")

    lines.unpersist()
  }

  private def getTableName: TarArchiveEntry => String = _.name.replaceAll("\\.[^.]*$", "")
}
