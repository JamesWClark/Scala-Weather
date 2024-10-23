import cats.effect._
import org.http4s.server.blaze._
import org.http4s.implicits._
import org.http4s.blaze.client._
import http.Routes
import scala.concurrent.ExecutionContext.global

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    val clientResource = BlazeClientBuilder[IO](global).resource

    clientResource.use { client =>
      val httpApp = Routes.allRoutes(client).orNotFound

      BlazeServerBuilder[IO](global)
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(httpApp)
        .serve
        .compile
        .drain
        .as(ExitCode.Success)
    }
  }
}