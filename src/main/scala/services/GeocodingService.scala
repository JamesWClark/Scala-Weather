package services

import cats.effect.IO
import io.circe.parser._
import io.circe.{Json, HCursor}
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.client.dsl.io._
import org.http4s.Method._
import org.http4s.Uri
import org.http4s.circe._
import scala.concurrent.ExecutionContext.global

// example: https://api.opencagedata.com/geocode/v1/json?q=New%20York,NY&key=53771be1069a4f5c9d775211de433846

object GeocodingService {
  private val apiKey = "53771be1069a4f5c9d775211de433846"
  private val geocodingUrlTemplate = "https://api.opencagedata.com/geocode/v1/json?q=%s,%s&key=%s"

  def geocode(city: String, state: String): IO[(String, String)] = {
    BlazeClientBuilder[IO](global).resource.use { client =>
      val encodedCity = Uri.encode(city)
      val encodedState = Uri.encode(state)
      val uriString = geocodingUrlTemplate.format(encodedCity, encodedState, apiKey)
      val uri = Uri.unsafeFromString(uriString)
      client.expect[Json](GET(uri)).map { json =>
        val cursor: HCursor = json.hcursor
        val lat = cursor.downField("results").downArray.downField("geometry").get[Double]("lat").getOrElse(0.0).toString
        val lng = cursor.downField("results").downArray.downField("geometry").get[Double]("lng").getOrElse(0.0).toString
        (lat, lng)
      }
    }
  }
}