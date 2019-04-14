package io.github.aelesbao.yelp

import org.apache.spark.SparkContext
import org.apache.spark.input.PortableDataStream
import org.apache.spark.rdd.RDD

object Implicits {
  implicit class TarGzSparkContext(private val sc: SparkContext) {
    /**
      * Get an RDD for a Hadoop-readable .tar.gz containing one row per line on each extracted file
      *
      * For example, if you have the following files:
      * {{{
      *   hdfs://a-hdfs-path/part-00000.tar.gz
      *   hdfs://a-hdfs-path/part-00001.tar.gz
      * }}}
      *
      * Do
      * `val rdd = sparkContext.tarGzFiles("hdfs://a-hdfs-path")`,
      *
      * then `rdd` contains
      * {{{
      *   ((a-hdfs-path/part-00000.tar.gz, fileA.txt), first content line)
      *   ((a-hdfs-path/part-00000.tar.gz, fileA.txt), second content line)
      *   ((a-hdfs-path/part-00000.tar.gz, fileB.json), {"x": 1})
      *   ((a-hdfs-path/part-00001.tar.gz, records.csv), "A","1")
      *   ((a-hdfs-path/part-00001.tar.gz, records.csv), "B","2")
      *   ((a-hdfs-path/part-00001.tar.gz, records.csv), "C","3")
      * }}}
      *
      * @param path Directory to the input data files, the path can be comma separated paths as the
      *             list of inputs.
      * @param minPartitions A suggestion value of the minimal splitting number for input data.
      * @param fileNameFilter Extension used to filter files inside the tar file (default: .json)
      * @return RDD representing tuples of file path, extracted tar file name and corresponding
      *         file content as lines
      *
      * @see For more information, check [[SparkContext.binaryFiles]] documentation
      */
    def tarGzFiles(path: String,
                   minPartitions: Int = sc.defaultMinPartitions): RDD[((String, PortableDataStream), TarArchiveEntry)] = {
      sc.binaryFiles(path, minPartitions)
        .flatMap {
          case (name: String, content: PortableDataStream) =>
            TarGzExtractor.listEntries(name, content)
        }
    }

    def tarGzFiles2(path: String,
                    minPartitions: Int = sc.defaultMinPartitions): RDD[((String, PortableDataStream), (String, Long))] = {
      sc.binaryFiles(path, minPartitions)
        .flatMap {
          case (name: String, data: PortableDataStream) =>
            io.github.aelesbao.yelp.tar.TarGzExtractor
              .listEntries(data.open)
              .map(name -> data -> _)
        }
    }
  }

  implicit class TarFileRDDFunctions(private val rdd: RDD[((String, PortableDataStream), TarArchiveEntry)]) {
    def extractLines(): RDD[(TarFileKey, String)] =
      rdd.flatMap {
        case ((file: String, data: PortableDataStream), entry: TarArchiveEntry) =>
          TarGzExtractor.extractLines(file, data, entry)
      }

    def extractLines(entry: TarArchiveEntry): RDD[(TarFileKey, String)] =
      rdd.filterByEntry(entry).extractLines()

    def filterByFileName(filter: String): RDD[((String, PortableDataStream), TarArchiveEntry)] =
      rdd.filter(_._2.name.matches(filter))

    def filterByEntry(entry: TarArchiveEntry): RDD[((String, PortableDataStream), TarArchiveEntry)] =
      rdd.filter(_._2 == entry)
  }

  implicit class TarFileRDDFunctions2(private val rdd: RDD[((String, PortableDataStream), (String, Long))]) {
    def filterByName(filter: String): RDD[((String, PortableDataStream), (String, Long))] =
      rdd.filter {
        case ((_, _), (name, _)) => name.matches(filter)
      }

    def extractLines(): RDD[(String, String)] =
      rdd.flatMap {
        case ((_, data), (name, position)) =>
          io.github.aelesbao.yelp.tar.TarGzExtractor
            .extractLines(data.open, position)
            .map(name -> _)
      }

    def extractLines(filter: String): RDD[(String, String)] =
      rdd.filterByName(filter).extractLines()
  }
}
