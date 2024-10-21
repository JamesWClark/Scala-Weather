import cats.effect.{IO, IOApp}
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import http.Routes.allRoutes

object Main extends IOApp.Simple {
  val httpApp = allRoutes.orNotFound

  override def run: IO[Unit] = {
    BlazeServerBuilder[IO]
      .bindHttp(8080, "localhost")
      .withHttpApp(httpApp)
      .serve
      .compile
      .drain
      .map(_ => println("Server started."))
  }
}