package io.github.aelesbao.yelp

import com.typesafe.scalalogging.slf4j.Logger
import com.typesafe.scalalogging.Logging
import org.slf4j.LoggerFactory

/**
  * Defines `logger` as a transient lazy value initialized with an underlying [[org.slf4j.Logger]]
  * named like the class into which this trait is mixed.
  */
trait TransientLazyLogging extends Logging {

  @transient
  override protected lazy val logger: Logger =
    Logger(LoggerFactory getLogger getClass.getName)

}
