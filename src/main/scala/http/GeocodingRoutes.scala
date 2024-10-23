package http

import cats.effect.IO
import org.http4s.{HttpRoutes, Uri, Request}
import org.http4s.dsl.io._
import org.http4s.headers.{`Content-Type`, `Accept`}
import org.http4s.MediaType
import org.http4s.client.Client
import services.GeocodingService
import org.slf4j.LoggerFactory
import io.circe.Json
import io.circe.syntax._
import org.http4s.circe._

object GeocodingRoutes {
  private val logger = LoggerFactory.getLogger(this.getClass)

  object LatQueryParamDecoderMatcher extends QueryParamDecoderMatcher[String]("latitude")
  object LongQueryParamDecoderMatcher extends QueryParamDecoderMatcher[String]("longitude")
  object QueryParamDecoderMatcher extends QueryParamDecoderMatcher[String]("query")

  def routes(client: Client[IO]): HttpRoutes[IO] = HttpRoutes.of[IO] {
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
}