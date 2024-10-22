package http

import cats.effect.IO
import org.http4s.{HttpRoutes, Uri, Request}
import org.http4s.dsl.io._
import org.http4s.headers.{`Content-Type`, `Cache-Control`, `Accept`}
import org.http4s.CacheDirective._
import org.http4s.MediaType
import org.http4s.server.staticcontent._
import org.http4s.server.middleware._
import org.http4s.server.Router
import org.http4s.client.dsl.io._
import org.http4s.client.Client
import services.{GeocodingService, WeatherService}
import views.AltIndexView
import org.slf4j.LoggerFactory
import io.circe.Json
import org.http4s.circe._
import io.circe.syntax._

object AltRoutes {
  private val logger = LoggerFactory.getLogger(this.getClass)

  object CityQueryParamDecoderMatcher extends QueryParamDecoderMatcher[String]("city")
  object LatQueryParamDecoderMatcher extends QueryParamDecoderMatcher[String]("latitude")
  object LongQueryParamDecoderMatcher extends QueryParamDecoderMatcher[String]("longitude")
  object QueryParamDecoderMatcher extends QueryParamDecoderMatcher[String]("query")

  def routes(client: Client[IO]): HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root =>
      Ok(AltIndexView.render()).map(_.withContentType(`Content-Type`(MediaType.text.html).withCharset(org.http4s.Charset.`UTF-8`)))
    
    case GET -> Root / "weather" :? CityQueryParamDecoderMatcher(city) =>
      logger.info(s"Received request for weather with city: $city")
      for {
        coords <- GeocodingService.geocode(city, "")
        weatherResult <- WeatherService.fetchWeather(coords._1, coords._2)
        response <- weatherResult match {
          case (weatherJson, location) =>
            Ok(AltIndexView.render(Some(weatherJson), Some(location)))
              .map(_.withContentType(`Content-Type`(MediaType.text.html).withCharset(org.http4s.Charset.`UTF-8`)))
        }
      } yield response

    case GET -> Root / "weather" :? LatQueryParamDecoderMatcher(latitude) +& LongQueryParamDecoderMatcher(longitude) =>
      logger.info(s"Received request for weather with latitude: $latitude and longitude: $longitude")
      WeatherService.fetchWeather(latitude, longitude).flatMap { case (weatherJson, location) =>
        Ok(AltIndexView.render(Some(weatherJson), Some(location), Some(latitude), Some(longitude)))
          .map(_.withContentType(`Content-Type`(MediaType.text.html).withCharset(org.http4s.Charset.`UTF-8`)))
      }

    case GET -> Root / "reverse-geocode" :? LatQueryParamDecoderMatcher(latitude) +& LongQueryParamDecoderMatcher(longitude) =>
      logger.info(s"Received request for reverse geocoding with latitude: $latitude and longitude: $longitude")
      GeocodingService.reverseGeocode(latitude, longitude).flatMap { case (city, state) =>
        Ok(Json.obj("city" -> Json.fromString(city), "state" -> Json.fromString(state)))
          .map(_.withContentType(`Content-Type`(MediaType.application.json)))
      }

    case GET -> Root / "autocomplete" :? QueryParamDecoderMatcher(query) =>
      logger.info(s"Received autocomplete request with query: $query")
      val apiKey = "-tJM5y_DlqlMYwaa8gM6AvBF9BC2PD6Ol_xzq-rQRGI"
      val encodedQuery = Uri.encode(query)
      val uri = Uri.unsafeFromString(s"https://autocomplete.search.hereapi.com/v1/autocomplete?q=$encodedQuery&apiKey=$apiKey&limit=5")
      val request = Request[IO](GET, uri).withHeaders(`Accept`(MediaType.application.json))
      client.expect[String](request).flatMap { response =>
        Ok(response).map(_.withContentType(`Content-Type`(MediaType.application.json)))
      }
  }

  // Configure the static file service
  val staticRoutes: HttpRoutes[IO] = fileService[IO](FileService.Config("./src/main/resources/static"))

  // Middleware to disable caching
  val noCacheMiddleware: HttpRoutes[IO] => HttpRoutes[IO] = { routes =>
    routes.map { response =>
      response.putHeaders(`Cache-Control`(`no-store`))
    }
  }

  // Combine routes with middleware
  def allRoutes(client: Client[IO]): HttpRoutes[IO] = Router(
    "/" -> routes(client),
    "/static" -> noCacheMiddleware(staticRoutes)
  )
}