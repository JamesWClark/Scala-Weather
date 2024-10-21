// package http

// import cats.effect.IO
// import org.http4s.HttpRoutes
// import org.http4s.dsl.io._
// import org.http4s.headers.`Content-Type`
// import org.http4s.headers.`Cache-Control`
// import org.http4s.CacheDirective._
// import org.http4s.MediaType
// import org.http4s.server.staticcontent._
// import org.http4s.server.middleware._
// import org.http4s.server.Router
// import services.{WeatherService, GeocodingService}
// import views.{IndexView, WeatherView}
// import org.slf4j.LoggerFactory

// object Routes {
//   private val logger = LoggerFactory.getLogger(this.getClass)

//   object LatQueryParamDecoderMatcher extends QueryParamDecoderMatcher[String]("lat")
//   object LongQueryParamDecoderMatcher extends QueryParamDecoderMatcher[String]("long")
//   object CityQueryParamDecoderMatcher extends QueryParamDecoderMatcher[String]("city")
//   object StateQueryParamDecoderMatcher extends QueryParamDecoderMatcher[String]("state")

//   val httpRoutes: HttpRoutes[IO] = HttpRoutes.of[IO] {
//     case GET -> Root =>
//       Ok(IndexView.render()).map(_.withContentType(`Content-Type`(MediaType.text.html).withCharset(org.http4s.Charset.`UTF-8`)))
//     case GET -> Root / "weather" :? LatQueryParamDecoderMatcher(lat) +& LongQueryParamDecoderMatcher(long) =>
//       logger.info(s"Received request for weather with lat: $lat, long: $long")
//       for {
//         weather <- WeatherService.fetchWeather(lat, long)
//         response <- Ok(IndexView.render(Some(weather)))
//       } yield response
//     case GET -> Root / "weather" :? CityQueryParamDecoderMatcher(city) +& StateQueryParamDecoderMatcher(state) =>
//       logger.info(s"Received request for weather with city: $city, state: $state")
//       for {
//         coords <- GeocodingService.geocode(city, state)
//         weather <- WeatherService.fetchWeather(coords._1, coords._2)
//         response <- Ok(IndexView.render(Some(weather)))
//       } yield response
//   }

//   // Configure the static file service
//   val staticRoutes: HttpRoutes[IO] = fileService[IO](FileService.Config("./src/main/resources/static"))

//   // Middleware to disable caching
//   val noCacheMiddleware: HttpRoutes[IO] => HttpRoutes[IO] = { routes =>
//     routes.map { response =>
//       response.putHeaders(`Cache-Control`(`no-store`))
//     }
//   }

//   // Combine routes with middleware
//   val allRoutes: HttpRoutes[IO] = Router(
//     "/" -> httpRoutes,
//     "/static" -> noCacheMiddleware(staticRoutes)
//   )
// }