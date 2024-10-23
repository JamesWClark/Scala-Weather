package http

import cats.effect._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._
import io.circe.Json
import io.circe.syntax._
import io.circe.generic.auto._
import services.{GeocodingService, WeatherService}
import org.slf4j.LoggerFactory

object WeatherRoutes {
  private val logger = LoggerFactory.getLogger(this.getClass)

  case class WeatherResponse(
    shortForecast: String,
    temperature: Int,
    dayTemperature: Int,
    nightTemperature: Int,
    currentTemperature: Int,
    characterization: String,
    icon: String
  )

  implicit val weatherResponseEncoder: EntityEncoder[IO, WeatherResponse] = jsonEncoderOf[IO, WeatherResponse]

  def routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "latlong" / latLong =>
      val Array(lat, long) = latLong.split(",")
      WeatherService.fetchWeather(lat, long).attempt.flatMap {
        case Right((json, location)) =>
          val weatherResponse = parseWeatherResponse(json)
          Ok(weatherResponse.asJson)
        case Left(ex) =>
          InternalServerError(s"An error occurred: ${ex.getMessage}")
      }

    case GET -> Root / "city" / city =>
      logger.info(s"Received request for weather with city: $city")
      (for {
        coords <- GeocodingService.geocode(city, "")
        weatherResult <- WeatherService.fetchWeather(coords._1, coords._2)
        response <- weatherResult match {
          case (weatherJson, location) =>
            val weatherResponse = parseWeatherResponse(weatherJson)
            Ok(weatherResponse.asJson)
        }
      } yield response).handleErrorWith { error =>
        logger.error(s"Error fetching weather data: ${error.getMessage}")
        InternalServerError(s"An error occurred: ${error.getMessage}")
      }
  }

  private def parseWeatherResponse(json: Json): WeatherResponse = {
    val cursor = json.hcursor
    WeatherResponse(
      shortForecast = cursor.downField("shortForecast").as[String].getOrElse("Unknown"),
      temperature = cursor.downField("temperature").as[Int].getOrElse(0),
      dayTemperature = cursor.downField("dayTemperature").as[Int].getOrElse(0),
      nightTemperature = cursor.downField("nightTemperature").as[Int].getOrElse(0),
      currentTemperature = cursor.downField("currentTemperature").as[Int].getOrElse(0),
      characterization = cursor.downField("characterization").as[String].getOrElse("Unknown"),
      icon = cursor.downField("icon").as[String].getOrElse("")
    )
  }
}