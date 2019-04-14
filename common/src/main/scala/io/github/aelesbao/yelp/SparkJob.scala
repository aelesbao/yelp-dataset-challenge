package io.github.aelesbao.yelp

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession

/**
  * Base class for Spark jobs with default implementations of the SparkSession and SparkConf.
  */
abstract class SparkJob extends Runnable with Serializable with TransientLazyLogging {

  protected implicit lazy val spark: SparkSession = createSparkSession()

  protected implicit def sc: SparkContext = spark.sparkContext

  /**
    * The name of your application. This will appear in the UI and in log data.
    */
  protected def appName: String

  /**
    * Provides a default SparkConf that can be overridden in extending classes to customize its
    * behaviour.
    * @return a SparkConf with defaults from system properties and the classpath
    */
  protected def sparkConf: SparkConf = new SparkConf()
    .setAppName(appName)

  private def createSparkSession(): SparkSession =
    SparkSession.builder()
      .config(sparkConf)
      .enableHiveSupport()
      .getOrCreate()
}
