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
      .collect()
    files should have size 3
  }

  it should "expand compressed file names" in {
    val fileNames = sparkContext
      .tarGzFiles(filePath)
      .map(_._2.name)
      .collect()

    fileNames should contain only(
      "words",
      "results.json",
      "characters.json"
    )
  }

  it should "filter file name using regular expressions" in {
    val fileNames = sparkContext
      .tarGzFiles(filePath)
      .filterByFileName(".*.json$")
      .map(_._2.name)
      .collect()

    fileNames should contain only(
      "results.json",
      "characters.json"
    )
  }

  it should "extract file content lines" in {
    val fileContent = sparkContext
      .tarGzFiles(filePath)
      .filterByFileName("words")
      .extractLines()
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

  it should "decode lines with UTF-8" in {
    val fileContents = sparkContext
      .tarGzFiles(filePath)
      .filterByFileName("results.json")
      .extractLines()
      .groupByKey()
      .flatMap(_._2)
      .filter(_ != "")
      .collect()

    val jsonPattern = """[^,:{}\[\]0-9.\-+Eaeflnr-u \n\r\t]"""
    every (fileContents) should include regex jsonPattern
    atLeast (1, fileContents) should include regex """"three of a kind", "5♣""""
  }
}
