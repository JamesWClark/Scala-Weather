package services

import cats.effect.IO
import io.circe.parser._
import io.circe.{Json, HCursor}
import scala.io.Source
import java.net.URL

object WeatherService {
  private val urlTemplate = "https://api.weather.gov/points/%s,%s"

  def fetchWeather(lat: String, long: String): IO[String] = {
    for {
      metadata <- fetchMetadata(lat, long)
      forecastUrl <- extractForecastUrl(metadata)
      forecast <- fetchForecast(forecastUrl)
    } yield forecast
  }

  private def fetchMetadata(lat: String, long: String): IO[String] = IO {
    val url = urlTemplate.format(lat, long)
    Source.fromURL(new URL(url)).mkString
  }

  private def extractForecastUrl(metadata: String): IO[String] = IO {
    val json: Json = parse(metadata).getOrElse(Json.Null)
    val cursor: HCursor = json.hcursor
    cursor.downField("properties").get[String]("forecast").getOrElse("")
  }

  private def fetchForecast(forecastUrl: String): IO[String] = IO {
    Source.fromURL(new URL(forecastUrl)).mkString
  }
}