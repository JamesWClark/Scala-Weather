package views

import scalatags.Text.all._
import scalatags.Text.tags2.title

object AltIndexView {
  def render(weather: Option[String] = None): String = {
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
                  input(`type` := "text", cls := "form-control", id := "city", name := "city", autocomplete := "off")
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
                weather.map { w =>
                  raw(WeatherView.render(w))
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