package io.github.aelesbao.yelp

import java.io.{BufferedReader, InputStreamReader}

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.spark.input.PortableDataStream

case class TarFileKey(archivePath: String, name: String)

case class TarArchiveEntry(name: String, start: Long, size: Long)

private object TarGzExtractor extends Serializable with TransientLazyLogging {
  def listEntries(file: String, data: PortableDataStream): Stream[((String, PortableDataStream), TarArchiveEntry)] = {
    logger.debug(s"Listing entries for $file")
    val tar = new TarArchiveInputStream(new GzipCompressorInputStream(data.open))
    Stream.continually(tar.getNextEntry)
      .takeWhile {
        case null => tar.close(); false
        case _ => true
      }
      .filter(!_.isDirectory)
      .filter(_.getSize > 0)
      .map(entry => TarArchiveEntry(entry.getName, tar.getBytesRead, entry.getSize))
      .map(file -> data -> _)
  }

  def extractLines(file: String, data: PortableDataStream, entry: TarArchiveEntry): Seq[(TarFileKey, String)] = {
    logger.debug(s"Scanning $file to extract ${entry.name}")
    val tar = new TarArchiveInputStream(new GzipCompressorInputStream(data.open))
    Stream.continually(tar.getNextEntry)
      .takeWhile {
        case null => tar.close(); false
        case _ => true
      }
      .filter(tarEntry => tarEntry.getName == entry.name)
      .flatMap { _ =>
        logger.debug(s"Extracting ${entry.name} [start=${entry.start}, size=${entry.size}]")
        val br = new BufferedReader(new InputStreamReader(tar))
        Stream.continually(br.readLine())
          .takeWhile(_ != null)
          .map(TarFileKey(file, entry.name) -> _)
      }
  }
}
