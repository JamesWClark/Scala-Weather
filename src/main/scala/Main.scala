import cats.effect.{IO, IOApp}
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.blaze.client.BlazeClientBuilder
import http.Routes
import scala.concurrent.ExecutionContext.global

object Main extends IOApp.Simple {
  override def run: IO[Unit] = {
    BlazeClientBuilder[IO](global).resource.use { client =>
      val httpApp = Routes.allRoutes(client).orNotFound

      BlazeServerBuilder[IO]
        .bindHttp(8080, "0.0.0.0") 
        .withHttpApp(httpApp)
        .serve
        .compile
        .drain
    }
  }
}