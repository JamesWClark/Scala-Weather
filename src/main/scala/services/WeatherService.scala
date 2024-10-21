package services

import cats.effect.IO
import io.circe.parser._
import io.circe.syntax._
import io.circe.{Json, HCursor, Encoder, JsonObject}
import scala.io.Source
import java.net.URL
import java.time.{LocalDate, ZonedDateTime}
import java.time.format.DateTimeFormatter
import org.slf4j.LoggerFactory

object WeatherService {
  private val logger = LoggerFactory.getLogger(this.getClass)
  private val urlTemplate = "https://api.weather.gov/points/%s,%s"

  def fetchWeather(lat: String, long: String): IO[Json] = {
    for {
      metadata <- fetchMetadata(lat, long)
      forecastUrl <- extractForecastUrl(metadata)
      forecast <- fetchForecast(forecastUrl)
      todayForecast <- extractTodayForecast(forecast)
      temperatureCharacterization = characterizeTemperature(todayForecast._2)
    } yield Json.obj(
      "shortForecast" -> Json.fromString(todayForecast._1),
      "temperature" -> Json.fromInt(todayForecast._2),
      "characterization" -> Json.fromString(temperatureCharacterization),
      "icon" -> Json.fromString(todayForecast._3)
    )
  }

  private def fetchMetadata(lat: String, long: String): IO[String] = IO {
    val url = urlTemplate.format(lat, long)
    val metadata = Source.fromURL(new URL(url)).mkString
    logger.info(s"Fetched metadata: $metadata")
    metadata
  }

  private def extractForecastUrl(metadata: String): IO[String] = IO {
    val json: Json = parse(metadata).getOrElse(Json.Null)
    val cursor: HCursor = json.hcursor
    val forecastUrl = cursor.downField("properties").get[String]("forecast").getOrElse("")
    logger.info(s"Extracted forecast URL: $forecastUrl")
    forecastUrl
  }

  private def fetchForecast(forecastUrl: String): IO[String] = IO {
    val forecast = Source.fromURL(new URL(forecastUrl)).mkString
    logger.info(s"Fetched forecast: $forecast")
    forecast
  }

  private def extractTodayForecast(forecast: String): IO[(String, Int, String)] = IO {
    val json: Json = parse(forecast).getOrElse(Json.Null)
    val cursor: HCursor = json.hcursor
    val periods = cursor.downField("properties").downField("periods").as[List[Json]].getOrElse(List.empty)
    logger.info(s"Periods: $periods")
    val today = LocalDate.now()
    val todayPeriod = periods.find { period =>
      val startTime = period.hcursor.downField("startTime").as[String].getOrElse("")
      val endTime = period.hcursor.downField("endTime").as[String].getOrElse("")
      val startDate = ZonedDateTime.parse(startTime).toLocalDate
      val endDate = ZonedDateTime.parse(endTime).toLocalDate
      logger.info(s"Period Start: $startDate, End: $endDate")
      startDate.isEqual(today) || endDate.isEqual(today)
    }.getOrElse(Json.Null)
    logger.info(s"Today Period: $todayPeriod")
    val shortForecast = todayPeriod.hcursor.downField("shortForecast").as[String].getOrElse("Unknown")
    val temperature = todayPeriod.hcursor.downField("temperature").as[Int].getOrElse(0)
    val icon = todayPeriod.hcursor.downField("icon").as[String].getOrElse("")
    (shortForecast, temperature, icon)
  }

  private def characterizeTemperature(temperature: Int): String = {
    if (temperature >= 85) "hot"
    else if (temperature <= 60) "cold"
    else "moderate"
  }
}