package views

import scalatags.Text.all._
import scalatags.Text.tags2.title

object IndexView {
  def render(): String = {
    "<!DOCTYPE html>" + 
    html(
      head(
        title("Enter Latitude and Longitude"),
        link(rel := "stylesheet", href := "https://cdn.jsdelivr.net/npm/ol@latest/dist/ol.css"),
        script(src := "https://cdn.jsdelivr.net/npm/ol@latest/dist/ol.js"),
        script(src := "/static/map.js")
      ),
      body(
        h1("Enter Latitude and Longitude"),
        form(action := "/weather", method := "get")(
          label(`for` := "lat")("Latitude:"),
          input(`type` := "text", id := "lat", name := "lat"),
          br(), br(),
          label(`for` := "long")("Longitude:"),
          input(`type` := "text", id := "long", name := "long"),
          br(), br(),
          input(`type` := "submit", value := "Submit")
        ),
        div(id := "map", width := "100%", height := "400px")
      )
    ).render
  }
}