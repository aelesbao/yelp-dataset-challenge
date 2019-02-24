package io.github.aelesbao.yelp

import io.github.aelesbao.yelp.Implicits._
import org.apache.spark.sql.test.SharedSparkSession
import org.scalatest.{FlatSpec, Matchers}

class TarGzExtractorSpec extends FlatSpec with Matchers with SharedSparkSession {
  behavior of "A TarGz extractor"

  val filePath: String = getClass.getResource("/test.tar.gz").getPath

  it should "extract files from .tar.gz" in {
    val files = sparkContext
      .tarGzFiles(filePath)
      .groupByKey()
      .collect()
    files should have size 3
  }

  it should "expand compressed file names" in {
    val fileNames = sparkContext
      .tarGzFiles(filePath)
      .groupByKey()
      .map(_._1.fileName)
      .collect()

    fileNames should contain only(
      "words",
      "results.json",
      "characters.json"
    )
  }

  it should "extract file content lines" in {
    val fileContent = sparkContext
      .tarGzFiles(filePath, fileNameFilter = Some("words"))
      .groupByKey()
      .flatMap(_._2)
      .collect()

    fileContent should contain only(
      "this",
      "is",
      "a",
      "test"
    )
  }

  it should "filter file name" in {
    val fileNames = sparkContext
      .tarGzFiles(filePath, fileNameFilter = Some(".*.json$"))
      .groupByKey()
      .map(_._1.fileName)
      .collect()

    fileNames should contain only(
      "results.json",
      "characters.json"
    )
  }

  it should "decode lines with UTF-8" in {
    val fileContents = sparkContext
      .tarGzFiles(filePath, fileNameFilter = Some("results.json"))
      .groupByKey()
      .flatMap(_._2)
      .filter(_ != "")
      .collect()

    val jsonPattern = """[^,:{}\[\]0-9.\-+Eaeflnr-u \n\r\t]"""
    every (fileContents) should include regex jsonPattern
    atLeast (1, fileContents) should include regex """"three of a kind", "5♣""""
  }
}
