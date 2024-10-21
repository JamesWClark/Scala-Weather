package views

import scalatags.Text.all._
import scalatags.Text.tags2.title

object IndexView {
  def render(weather: Option[String] = None): String = {
    "<!DOCTYPE html>" +
    html(
      head(
        meta(charset := "UTF-8"),
        title("Weather"),
        link(rel := "stylesheet", href := "/static/css/bootstrap.min.css"),
        link(rel := "stylesheet", href := "/static/css/openlayers.css"),
        link(rel := "stylesheet", href := "/static/css/main.css")
      ),
      body(
        div(cls := "container")(
          div(cls := "row")(
            div(cls := "col-md-6")(
              h1("Enter Latitude and Longitude"),
              form(action := "/weather", method := "get", id := "weatherForm")(
                div(cls := "mb-3")(
                  label(`for` := "city", cls := "form-label")("City:"),
                  input(`type` := "text", cls := "form-control", id := "city", name := "city")
                ),
                div(cls := "mb-3")(
                  label(`for` := "state", cls := "form-label")("State:"),
                  input(`type` := "text", cls := "form-control", id := "state", name := "state")
                ),
                div(cls := "mb-3")(
                  label(`for` := "lat", cls := "form-label")("Latitude:"),
                  input(`type` := "text", cls := "form-control", id := "lat", name := "lat")
                ),
                div(cls := "mb-3")(
                  label(`for` := "long", cls := "form-label")("Longitude:"),
                  input(`type` := "text", cls := "form-control", id := "long", name := "long")
                ),
                div(cls := "mb-3")(
                  input(`type` := "submit", cls := "btn btn-primary", value := "Submit")
                )
              )
            ),
            div(cls := "col-md-6")(
              div(id := "map", style := "width: 100%; height: 400px;")
            )
          ),
          div(cls := "row mt-4")(
            div(cls := "col-md-12")(
              h2("Weather Information"),
              div(id := "weatherInfo")(
                weather.map { w =>
                  raw(WeatherView.render(w))
                }.getOrElse("")
              )
            )
          )
        ),
        script(src := "/static/js/bootstrap.min.js"),
        script(src := "/static/js/openlayers.min.js"),
        script(src := "/static/js/map.js"),
        script(src := "/static/js/opencage-autocomplete.js"),
        script(src := "/static/js/weather.js")
      )
    ).render
  }
}