import cats.effect.{IO, IOApp}
import org.http4s.HttpRoutes
import org.http4s.MediaType
import org.http4s.dsl.io._
import org.http4s.headers.`Content-Type`
import org.http4s.headers.`Cache-Control`
import org.http4s.CacheDirective._
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.staticcontent._
import org.http4s.server.middleware._
import org.http4s.server.Router
import scala.io.Source
import java.net.URL
import services.WeatherService
import views.IndexView

object Main extends IOApp.Simple {
  val httpRoutes = HttpRoutes.of[IO] {
    case GET -> Root =>
      Ok(IndexView.render()).map(_.withContentType(`Content-Type`(MediaType.text.html).withCharset(org.http4s.Charset.`UTF-8`)))
    case GET -> Root / "weather" :? LatQueryParamDecoderMatcher(lat) +& LongQueryParamDecoderMatcher(long) =>
      for {
        weather <- WeatherService.fetchWeather(lat, long)
        response <- Ok(weather)
      } yield response
  }

  object LatQueryParamDecoderMatcher extends QueryParamDecoderMatcher[String]("lat")
  object LongQueryParamDecoderMatcher extends QueryParamDecoderMatcher[String]("long")

  // Configure the static file service
  val staticRoutes = fileService[IO](FileService.Config("./src/main/resources/static"))

  // Middleware to disable caching
  val noCacheMiddleware: HttpRoutes[IO] => HttpRoutes[IO] = { routes =>
    routes.map { response =>
      response.putHeaders(`Cache-Control`(`no-store`))
    }
  }

  // Combine routes with middleware
  val httpApp = Router(
    "/" -> httpRoutes,
    "/static" -> noCacheMiddleware(staticRoutes)
  ).orNotFound

  override def run: IO[Unit] = {
    BlazeServerBuilder[IO]
      .bindHttp(8080, "localhost")
      .withHttpApp(httpApp)
      .serve
      .compile
      .drain
  }
}