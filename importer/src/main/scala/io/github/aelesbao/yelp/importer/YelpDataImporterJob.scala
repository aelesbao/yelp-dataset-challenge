package io.github.aelesbao.yelp.importer

import io.github.aelesbao.yelp.{SparkJob, TarFileKey}
import io.github.aelesbao.yelp.Implicits._
import org.apache.spark.rdd.RDD
import org.apache.spark.storage.StorageLevel

private class YelpDataImporterJob(dataPath: String) extends SparkJob {

  import spark.implicits._

  override protected def appName: String = "Yelp Data Importer"

  override def run(): Unit = {
    logger.info(s"Reading Yelp Dataset from $dataPath")
    val files = sc.tarGzFiles(dataPath, fileNameFilter = Some(".*.json$"))
      .groupByKey()
      .persist(StorageLevel.DISK_ONLY)

    files.keys.collect().foreach(writeToCassandra(files, _))

    files.unpersist()
  }

  private def writeToCassandra(files: RDD[(TarFileKey, Iterable[String])],
                               tarKey: TarFileKey): Unit = {
    val tableName = tarKey.fileName.replaceAll("\\.[^.]*$", "")
    val lines = files.filter(_._1 == tarKey).flatMap(_._2).toDS()
    val df = spark.read.json(lines)
    new CassandraWriter(tableName, df).write()
  }
}
