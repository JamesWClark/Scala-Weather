package http

import cats.effect.IO
import org.http4s.{HttpRoutes, Uri, Request}
import org.http4s.dsl.io._
import org.http4s.headers.{`Content-Type`, `Cache-Control`}
import org.http4s.CacheDirective._
import org.http4s.MediaType
import org.http4s.server.staticcontent._
import org.http4s.server.middleware._
import org.http4s.server.Router
import org.http4s.client.Client
import services.{GeocodingService, WeatherService}
import views.IndexView
import org.slf4j.LoggerFactory
import io.circe.Json
import io.circe.syntax._
import org.http4s.circe._

object Routes {
  private val logger = LoggerFactory.getLogger(this.getClass)

  def routes(client: Client[IO]): HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root =>
      Ok(IndexView.render()).map(_.withContentType(`Content-Type`(MediaType.text.html).withCharset(org.http4s.Charset.`UTF-8`)))
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
    "/static" -> noCacheMiddleware(staticRoutes),
    "/geocoding" -> GeocodingRoutes.routes(client),
    "/weather" -> WeatherRoutes.routes
  )
}