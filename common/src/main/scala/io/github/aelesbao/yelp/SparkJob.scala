package io.github.aelesbao.yelp

import com.typesafe.scalalogging.slf4j.LazyLogging
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession

/**
  * Base class for Spark jobs with default implementations of the SparkSession and SparkConf.
  */
abstract class SparkJob extends Runnable with LazyLogging {

  protected implicit lazy val spark: SparkSession = createSparkSession()

  protected implicit def sc: SparkContext = spark.sparkContext

  /**
    * Provides a default SparkConf that can be overridden in extending classes to customize its
    * behaviour.
    * @return a SparkConf with defaults from system properties and the classpath
    */
  protected def sparkConf: SparkConf = new SparkConf()

  private def createSparkSession(): SparkSession =
    SparkSession.builder()
      .config(sparkConf)
      .getOrCreate()
}
