package io.github.aelesbao.yelp

import java.io.{BufferedReader, InputStreamReader}

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.spark.input.PortableDataStream

case class TarFileKey(path: String, fileName: String)

private class TarGzExtractor(fileNameFilter: Option[String])
  extends Serializable with TransientLazyLogging {

  def extract: (String, PortableDataStream) => Stream[(TarFileKey, String)] = {
    case (name: String, content: PortableDataStream) =>
      logger.debug(s"Extracting tar.gz file $name")
      val tar = new TarArchiveInputStream(new GzipCompressorInputStream(content.open))
      Stream.continually(tar.getNextEntry)
        .takeWhile {
          case null => tar.close(); false
          case _ => true
        }
        .filter(entry => fileNameFilter.forall(entry.getName.matches(_)))
        .flatMap { entry =>
          logger.debug(s"Extracting ${entry.getName} [size=${entry.getSize}]")
          val br = new BufferedReader(new InputStreamReader(tar))
          Stream.continually(br.readLine())
            .takeWhile(_ != null)
            .map(TarFileKey(name, entry.getName) -> _)
        }
  }
}
