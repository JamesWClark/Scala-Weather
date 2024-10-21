package views

import scalatags.Text.all._
import scalatags.Text.tags2.title
import io.circe.Json
import io.circe.parser._

object AltIndexView {
  def render(weatherJson: Option[Json] = None, city: Option[String] = None): String = {
    val weatherInfo = weatherJson.flatMap { json =>
      for {
        shortForecast <- json.hcursor.downField("shortForecast").as[String].toOption
        temperature <- json.hcursor.downField("temperature").as[Int].toOption
        characterization <- json.hcursor.downField("characterization").as[String].toOption
        icon <- json.hcursor.downField("icon").as[String].toOption
      } yield (shortForecast, temperature, characterization, icon)
    }

    "<!DOCTYPE html>" +
    html(
      head(
        meta(charset := "UTF-8"),
        title("Weather"),
        link(rel := "stylesheet", href := "/static/css/bootstrap.min.css"),
        link(rel := "stylesheet", href := "/static/css/main.css")
      ),
      body(
        div(cls := "container")(
          div(cls := "row")(
            div(cls := "col-md-6")(
              h1("Enter City"),
              form(action := "/weather", method := "get", id := "weatherForm", autocomplete := "off")(
                div(cls := "mb-3")(
                  label(`for` := "city", cls := "form-label")("City:"),
                  input(`type` := "text", cls := "form-control", id := "city", name := "city", autocomplete := "off", value := city.getOrElse(""))
                ),
                div(cls := "mb-3")(
                  input(`type` := "submit", cls := "btn btn-primary", value := "Submit")
                )
              )
            )
          ),
          div(cls := "row mt-4")(
            div(cls := "col-md-12")(
              div(id := "weatherInfo")(
                weatherInfo.map { case (shortForecast, temperature, characterization, icon) =>
                  div(cls := "card")(
                    div(cls := "card-body")(
                      h5(cls := "card-title")("Weather Information"),
                      p(cls := "card-text")(s"Short Forecast: $shortForecast"),
                      p(cls := "card-text")(s"Temperature: $temperature°F"),
                      p(cls := "card-text")(s"Characterization: $characterization"),
                      if (icon.nonEmpty) img(src := icon, cls := "weather-icon") else ""
                    )
                  )
                }.getOrElse("")
              )
            )
          )
        ),
        script(src := "/static/js/bootstrap.min.js"),
        script(src := "/static/js/alt-autocomplete.js")
      )
    ).render
  }
}