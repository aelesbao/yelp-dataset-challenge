package io.github.aelesbao.yelp.importer

import io.github.aelesbao.yelp.TransientLazyLogging
import org.apache.spark.sql.DataFrame

private class CassandraWriter(tableName: String, private val df: DataFrame)
  extends TransientLazyLogging {

  def write(): Unit = {
    logger.info(s"Writing $tableName table")
    df.printSchema()
    logger.info(s"Saved ${df.count()} records to table $tableName")
  }
}
