import scala.io.Source
import java.net.URL

object Main extends App {
  val url = "https://api.weather.gov/gridpoints/MPX/107,69/forecast"
  val response = Source.fromURL(new URL(url)).mkString
  println(response)
}