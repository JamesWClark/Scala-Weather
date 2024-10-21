package views

import scalatags.Text.all._

object WeatherView {
  def render(weather: String): String = {
    val json = io.circe.parser.parse(weather).getOrElse(io.circe.Json.Null)
    val periods = json.hcursor.downField("properties").downField("periods").as[List[io.circe.Json]].getOrElse(List.empty)

    div(
      h2("Weather Information"),
      periods.map { period =>
        val cursor = period.hcursor
        div(cls := "weather-period")(
          h3(cursor.downField("name").as[String].getOrElse("")),
          p(b("Temperature: "), cursor.downField("temperature").as[Int].getOrElse(0).toString + " " + cursor.downField("temperatureUnit").as[String].getOrElse("")),
          p(b("Forecast: "), cursor.downField("detailedForecast").as[String].getOrElse("")),
          img(src := cursor.downField("icon").as[String].getOrElse(""))
        )
      }
    ).render
  }
}