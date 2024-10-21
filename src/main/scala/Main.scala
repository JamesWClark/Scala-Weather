import cats.effect.{IO, IOApp}
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.blaze.client.BlazeClientBuilder
import http.AltRoutes
import scala.concurrent.ExecutionContext.global

object Main extends IOApp.Simple {
  override def run: IO[Unit] = {
    BlazeClientBuilder[IO](global).resource.use { client =>
      val httpApp = AltRoutes.allRoutes(client).orNotFound

      BlazeServerBuilder[IO]
        .bindHttp(8080, "localhost")
        .withHttpApp(httpApp)
        .serve
        .compile
        .drain
    }
  }
}