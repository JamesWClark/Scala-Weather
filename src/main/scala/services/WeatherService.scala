package services

import cats.effect.IO
import io.circe.parser._
import io.circe.syntax._
import io.circe.{Json, HCursor, Encoder, JsonObject}
import scala.io.Source
import java.net.URL
import java.time.{LocalDate, ZonedDateTime, ZoneOffset}
import java.time.format.DateTimeFormatter
import org.slf4j.LoggerFactory
import services.GeocodingService

object WeatherService {
  private val logger = LoggerFactory.getLogger(this.getClass)
  private val urlTemplate = "https://api.weather.gov/points/%s,%s"
  // ex: https://api.weather.gov/points/38.885924,-104.848246

  def fetchWeather(lat: String, long: String): IO[(Json, String)] = {
    for {
      metadata <- fetchMetadata(lat, long).handleErrorWith { error =>
        logger.error(s"Failed to fetch metadata: ${error.getMessage}")
        IO.raiseError(new Exception("Failed to fetch metadata"))
      }
      forecastUrl <- extractForecastUrl(metadata).handleErrorWith { error =>
        logger.error(s"Failed to extract forecast URL: ${error.getMessage}")
        IO.raiseError(new Exception("Failed to extract forecast URL"))
      }
      forecast <- fetchForecast(forecastUrl).handleErrorWith { error =>
        logger.error(s"Failed to fetch forecast: ${error.getMessage}")
        IO.raiseError(new Exception("Failed to fetch forecast"))
      }
      todayForecast <- extractTodayForecast(forecast).handleErrorWith { error =>
        logger.error(s"Failed to extract today's forecast: ${error.getMessage}")
        IO.raiseError(new Exception("Failed to extract today's forecast"))
      }
      stationId <- extractStationId(metadata).handleErrorWith { error =>
        logger.error(s"Failed to extract station ID: ${error.getMessage}")
        IO.raiseError(new Exception("Failed to extract station ID"))
      }
      currentObservation <- fetchCurrentObservation(stationId).handleErrorWith { error =>
        logger.error(s"Failed to fetch current observation: ${error.getMessage}")
        IO.raiseError(new Exception("Failed to fetch current observation"))
      }
      currentTemperature <- extractCurrentTemperatureOrFallback(currentObservation, forecastUrl)
      temperatureCharacterization = characterizeTemperature(currentTemperature)
      locationTuple <- GeocodingService.reverseGeocode(lat, long).handleErrorWith { error =>
        logger.error(s"Failed to reverse geocode: ${error.getMessage}")
        IO.raiseError(new Exception("Failed to reverse geocode"))
      }
      location = s"${locationTuple._1}, ${locationTuple._2}"
    } yield (
      Json.obj(
        "shortForecast" -> Json.fromString(todayForecast._1),
        "temperature" -> Json.fromInt(todayForecast._2),
        "dayTemperature" -> Json.fromInt(todayForecast._3),
        "nightTemperature" -> Json.fromInt(todayForecast._4),
        "currentTemperature" -> Json.fromInt(currentTemperature),
        "characterization" -> Json.fromString(temperatureCharacterization),
        "icon" -> Json.fromString(todayForecast._5)
      ),
      location
    )
  }

  private def fetchMetadata(lat: String, long: String): IO[String] = IO {
    val url = urlTemplate.format(lat, long)
    val metadata = Source.fromURL(new URL(url)).mkString
    logger.debug(s"Metadata URL: $url")
    logger.info(s"Fetched metadata: $metadata")
    metadata
  }

  private def extractForecastUrl(metadata: String): IO[String] = IO {
    val json: Json = parse(metadata).getOrElse(Json.Null)
    val cursor: HCursor = json.hcursor
    val forecastUrl = cursor.downField("properties").get[String]("forecast").getOrElse("")
    logger.debug(s"Extracted forecast URL: $forecastUrl")
    forecastUrl
  }

  private def fetchForecast(forecastUrl: String): IO[String] = IO {
    val forecast = Source.fromURL(new URL(forecastUrl)).mkString
    logger.info(s"Fetched forecast: $forecast")
    forecast
  }

  private def extractTodayForecast(forecast: String): IO[(String, Int, Int, Int, String)] = IO {
    val json: Json = parse(forecast).getOrElse(Json.Null)
    val cursor: HCursor = json.hcursor
    val periods = cursor.downField("properties").downField("periods").as[List[Json]].getOrElse(List.empty)
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

    val dayTemperature = periods.find(_.hcursor.downField("isDaytime").as[Boolean].getOrElse(false)).flatMap(_.hcursor.downField("temperature").as[Int].toOption).getOrElse(0)
    val nightTemperature = periods.find(!_.hcursor.downField("isDaytime").as[Boolean].getOrElse(true)).flatMap(_.hcursor.downField("temperature").as[Int].toOption).getOrElse(0)

    (shortForecast, temperature, dayTemperature, nightTemperature, icon)
  }

  private def extractStationId(metadata: String): IO[String] = {
    val json: Json = parse(metadata).getOrElse(Json.Null)
    val cursor: HCursor = json.hcursor
    val observationStationsUrl = cursor.downField("properties").get[String]("observationStations").getOrElse("")
    logger.debug(s"Extracted observation stations URL: $observationStationsUrl")

    if (observationStationsUrl.isEmpty) {
      IO.raiseError(new Exception("Observation stations URL is empty"))
    } else {
      IO {
        val stationsJson = Source.fromURL(new URL(observationStationsUrl)).mkString
        val stationsCursor = parse(stationsJson).getOrElse(Json.Null).hcursor
        val stationIds = stationsCursor.downField("features").as[List[Json]].getOrElse(List.empty).flatMap { feature =>
          feature.hcursor.downField("properties").get[String]("stationIdentifier").toOption
        }
        val stationId = stationIds.headOption.getOrElse("")
        logger.info(s"Extracted station ID: $stationId")
        stationId
      }
    }
  }

  private def fetchCurrentObservation(stationId: String): IO[String] = {
    if (stationId.isEmpty) {
      IO.raiseError(new Exception("Station ID is empty"))
    } else {
      IO {
        val url = s"https://api.weather.gov/stations/$stationId/observations/latest"
        val observation = Source.fromURL(new URL(url)).mkString
        logger.debug(s"Current observation URL: $url")
        logger.info(s"Fetched current observation: $observation")
        observation
      }
    }
  }.handleErrorWith { error =>
    logger.error(s"Failed to fetch current observation: ${error.getMessage}")
    IO.pure("")
  }

  private def extractCurrentTemperature(observation: String): Option[Int] = {
    if (observation.isEmpty) {
      logger.warn("Observation data is empty")
      None
    } else {
      val json: Json = parse(observation).getOrElse(Json.Null)
      val cursor: HCursor = json.hcursor
      val temperatureCelsius = cursor.downField("properties").downField("temperature").downField("value").as[Option[Double]].getOrElse {
        logger.warn("Failed to extract temperature from observation data")
        None
      }
      temperatureCelsius.map { temp =>
        val temperatureFahrenheit = (temp * 9 / 5) + 32
        logger.info(s"Extracted current temperature: $temp°C / $temperatureFahrenheit°F")
        temperatureFahrenheit.toInt
      }
    }
  }

  private def extractCurrentTemperatureOrFallback(observation: String, forecastUrl: String): IO[Int] = {
    extractCurrentTemperature(observation) match {
      case Some(temp) => IO.pure(temp)
      case None =>
        logger.warn("Current temperature is null, falling back to hourly forecast")
        fetchHourlyForecast(forecastUrl)
    }
  }

  private def fetchHourlyForecast(forecastUrl: String): IO[Int] = {
    val hourlyForecastUrl = forecastUrl.replace("forecast", "forecast/hourly")
    IO {
      val url = new URL(hourlyForecastUrl)
      val forecast = Source.fromURL(url).mkString
      logger.debug(s"Hourly forecast URL: $hourlyForecastUrl")
      logger.info(s"Fetched hourly forecast: $forecast")
  
      val json: Json = parse(forecast).getOrElse(Json.Null)
      val cursor: HCursor = json.hcursor
      val periods = cursor.downField("properties").downField("periods").as[List[Json]].getOrElse(List.empty)
      val now = ZonedDateTime.now(ZoneOffset.UTC)
  
      val nextPeriod = periods.find { period =>
        val startTime = ZonedDateTime.parse(period.hcursor.downField("startTime").as[String].getOrElse(""))
        val endTime = ZonedDateTime.parse(period.hcursor.downField("endTime").as[String].getOrElse(""))
        now.isAfter(startTime) && now.isBefore(endTime)
      }.getOrElse(Json.Null)
  
      val temperature = nextPeriod.hcursor.downField("temperature").as[Int].getOrElse(0)
      logger.info(s"Fetched hourly forecast temperature: $temperature°F")
      temperature
    }.handleErrorWith { error =>
      logger.error(s"Failed to fetch hourly forecast: ${error.getMessage}")
      IO.pure(0)
    }
  }

  private def characterizeTemperature(temperature: Int): String = {
    if (temperature >= 85) "hot"
    else if (temperature <= 60) "cold"
    else "moderate"
  }
}