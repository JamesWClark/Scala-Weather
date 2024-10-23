package http

import cats.effect._
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.circe._
import io.circe.Json
import io.circe.syntax._
import io.circe.generic.auto._
import services.{GeocodingService, WeatherService}
import views.IndexView
import org.slf4j.LoggerFactory
import org.http4s.headers.`Content-Type`

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

  def jsonRoutes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "latlong" / latLong =>
      val Array(lat, long) = latLong.split(",")
      WeatherService.fetchWeather(lat, long).attempt.flatMap {
        case Right((json, _)) =>
          Ok(json)
        case Left(ex) =>
          InternalServerError(s"An error occurred: ${ex.getMessage}")
      }

    case GET -> Root / "city" / city =>
      logger.info(s"Received request for weather with city: $city")
      (for {
        coords <- GeocodingService.geocode(city, "")
        weatherResult <- WeatherService.fetchWeather(coords._1, coords._2)
        response <- weatherResult match {
          case (weatherJson, _) =>
            Ok(weatherJson)
        }
      } yield response).handleErrorWith { error =>
        logger.error(s"Error fetching weather data: ${error.getMessage}")
        InternalServerError(s"An error occurred: ${error.getMessage}")
      }
  }

  def viewRoutes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "latlong" :? LatQueryParamDecoderMatcher(lat) +& LongQueryParamDecoderMatcher(long) =>
      logger.info(s"Received request for weather with lat: $lat, long: $long")
      WeatherService.fetchWeather(lat, long).attempt.flatMap {
        case Right((json, location)) =>
          Ok(IndexView.render(Some(json), Some(location), Some(lat), Some(long)))
            .map(_.withContentType(`Content-Type`(MediaType.text.html)))
        case Left(ex) =>
          Ok(IndexView.render(errorMessage = Some(s"An error occurred: ${ex.getMessage}")))
            .map(_.withContentType(`Content-Type`(MediaType.text.html)))
      }

    case GET -> Root / "city" :? CityQueryParamDecoderMatcher(city) =>
      logger.info(s"Received request for weather with city: $city")
      (for {
        coords <- GeocodingService.geocode(city, "")
        _ = logger.info(s"Geocoded city: $city to coords: ${coords._1}, ${coords._2}")
        weatherResult <- WeatherService.fetchWeather(coords._1, coords._2)
        response <- weatherResult match {
          case (weatherJson, location) =>
            Ok(IndexView.render(Some(weatherJson), Some(location)))
              .map(_.withContentType(`Content-Type`(MediaType.text.html)))
        }
      } yield response).handleErrorWith { error =>
        logger.error(s"Error fetching weather data: ${error.getMessage}")
        Ok(IndexView.render(errorMessage = Some(s"An error occurred: ${error.getMessage}")))
          .map(_.withContentType(`Content-Type`(MediaType.text.html)))
      }
  }

  object LatQueryParamDecoderMatcher extends QueryParamDecoderMatcher[String]("latitude")
  object LongQueryParamDecoderMatcher extends QueryParamDecoderMatcher[String]("longitude")
  object CityQueryParamDecoderMatcher extends QueryParamDecoderMatcher[String]("city")
}