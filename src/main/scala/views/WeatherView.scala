package views

import scalatags.Text.all._
import scalatags.Text.tags2.title

object WeatherView {
  def render(weather: String): String = {
    "<!DOCTYPE html>" +
    html(
      head(
        meta(charset := "UTF-8"),
        title("Weather Information")
      ),
      body(
        h1("Weather Information"),
        pre(weather),
        a(href := "/")("Back to Home")
      )
    ).render
  }
}