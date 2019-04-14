package io.github.aelesbao.yelp.tar

import java.io.{BufferedReader, InputStream, InputStreamReader}

import io.github.aelesbao.yelp.TransientLazyLogging
import org.apache.commons.compress.archivers.tar.{TarArchiveEntry, TarArchiveInputStream}
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.compress.utils.IOUtils

object TarGzExtractor extends TransientLazyLogging {
  def listEntries(in: InputStream): Stream[(String, Long)] = {
    val tar = new TarGzArchiveInputStream(in)
    Stream.continually(tar.getNextEntry)
      .takeWhile {
        case null => IOUtils.closeQuietly(tar); false
        case _ => true
      }
      .filter(!_.isDirectory)
      .filter(_.getSize > 0)
      .map(_.getName -> tar.getBytesRead)
  }

  def extractLines(in: InputStream, position: Long): Stream[String] = {
    val tar = new TarGzArchiveInputStream(in)
    try {
      tar.findEntry(position) match {
        case Some(entry) =>
          logger.debug(s"Extracting ${entry.getName} [size=${entry.getSize}]")
          val br = new BufferedReader(new InputStreamReader(tar))
          Stream.continually(br.readLine()).takeWhile(_ != null)
        case None =>
          Stream.empty
      }
    } finally {
      IOUtils.closeQuietly(tar)
    }
  }
}

private class TarGzArchiveInputStream(in: InputStream) extends TarArchiveInputStream(new GzipCompressorInputStream(in)) {
  def findEntry(position: Long): Option[TarArchiveEntry] = {
    if (position < 0L)
      throw new IndexOutOfBoundsException

    if (getBytesRead > 0L)
      throw new UnsupportedOperationException("tar file stream should be at starting position")

    val headerPosition = position - getRecordSize
    val skipped = IOUtils.skip(in, headerPosition)
    count(skipped)

    Option(getNextTarEntry)
  }
}
