package io.github.aelesbao.yelp.importer

import org.scalatest.{FlatSpec, Matchers}

class YelpDataImporterAppSpec extends FlatSpec with Matchers {
  behavior of "The YelpDataImporterApp"

  it should "throw an exception when triggered with missing arguments" in {
    an[IllegalArgumentException] should be thrownBy YelpDataImporterApp.main(Array.empty)
  }
}
