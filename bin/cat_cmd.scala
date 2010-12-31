import scala.io._
import java.io.File

if (args.isEmpty) {
  // from stdin
  var line = readLine
  while (line != null) {
    println(line)
    line = readLine
  }
} else {
  // from files
  for (arg <- args) {
    for (line <- Source.fromFile(new File(arg))(Codec.UTF8).getLines()) {
      println(line)
    }
  }
}
