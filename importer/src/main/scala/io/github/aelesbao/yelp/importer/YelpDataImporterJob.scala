package io.github.aelesbao.yelp.importer

import io.github.aelesbao.yelp.SparkJob
import io.github.aelesbao.yelp.Implicits._

private class YelpDataImporterJob(dataPath: String) extends SparkJob {
  override protected def appName: String = "Yelp Data Importer"

  override def run(): Unit = {
    logger.info(s"Reading Yelp Dataset from $dataPath")
    val files = sc.tarGzFiles(dataPath, fileNameFilter = Some(".*.json$"))
      .countByKey()
      .map(entry => s"${entry._1.fileName} (${entry._2} lines)")

    logger.info(s"Extracted ${files.size} files")
    logger.debug(s"Files extracted: ${files.reduce(String.join(", ", _, _))}")
  }
}
