package views

import scalatags.Text.all._
import scalatags.Text.tags2.title

object IndexView {
  def render(): String = {
    "<!DOCTYPE html>" +
    html(
      head(
        meta(charset := "UTF-8"),
        title("Enter Latitude and Longitude"),
        link(rel := "stylesheet", href := "/static/css/bootstrap.min.css"),
        link(rel := "stylesheet", href := "/static/css/openlayers.css"),
        link(rel := "stylesheet", href := "/static/css/main.css"),
      ),
      body(
        div(cls := "container")(
          div(cls := "row")(
            div(cls := "col-md-6")(
              h1("Enter Latitude and Longitude"),
              form(action := "/weather", method := "get")(
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
          )
        ),
        script(src := "/static/js/bootstrap.min.js"),
        script(src := "/static/js/openlayers.min.js"),
        script(src := "/static/js/map.js")
      )
    ).render
  }
}