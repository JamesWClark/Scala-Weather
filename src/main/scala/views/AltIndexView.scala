package views

import scalatags.Text.all._
import scalatags.Text.tags2.title
import io.circe.Json
import io.circe.parser._

object AltIndexView {
  def render(weatherJson: Option[Json] = None, location: Option[String] = None, latitude: Option[String] = None, longitude: Option[String] = None): String = {
    val weatherInfo = weatherJson.flatMap { json =>
      for {
        shortForecast <- json.hcursor.downField("shortForecast").as[String].toOption
        temperature <- json.hcursor.downField("temperature").as[Int].toOption
        dayTemperature <- json.hcursor.downField("dayTemperature").as[Int].toOption
        nightTemperature <- json.hcursor.downField("nightTemperature").as[Int].toOption
        currentTemperature <- json.hcursor.downField("currentTemperature").as[Int].toOption
        characterization <- json.hcursor.downField("characterization").as[String].toOption
        icon <- json.hcursor.downField("icon").as[String].toOption
      } yield {
        (shortForecast, temperature, dayTemperature, nightTemperature, currentTemperature, characterization, icon)
      }
    }

    val locationDisplay = location.getOrElse("")

    val activeTab = if (latitude.isDefined && longitude.isDefined) "tab2" else "tab1"

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
          h1("Weather Search"),
          div(
            input(`type` := "radio", id := "tab1", name := "tabs", cls := "tab", if (activeTab == "tab1") checked := "checked" else ()),
            label(`for` := "tab1", cls := "tab-label")("City"),
            input(`type` := "radio", id := "tab2", name := "tabs", cls := "tab", if (activeTab == "tab2") checked := "checked" else ()),
            label(`for` := "tab2", cls := "tab-label")("Lat/Long"),
            input(`type` := "radio", id := "tab3", name := "tabs", cls := "tab"),
            label(`for` := "tab3", cls := "tab-label")("Map"),
            div(id := "tab-content1", cls := "tab-content")(
              h2("City Search"),
              form(action := "/weather", method := "get", id := "weatherForm", autocomplete := "off")(
                div(cls := "mb-3")(
                  label(`for` := "city", cls := "form-label")("City:"),
                  input(`type` := "text", cls := "form-control", id := "city", name := "city", autocomplete := "off", value := location.getOrElse(""))
                ),
                div(cls := "mb-3")(
                  input(`type` := "submit", cls := "btn btn-primary", value := "Submit")
                )
              )
            ),
            div(id := "tab-content2", cls := "tab-content")(
              h2("Lat / Long Search"),
              form(action := "/weather", method := "get", id := "latLongForm", autocomplete := "off")(
                div(cls := "mb-3")(
                  label(`for` := "latitude", cls := "form-label")("Latitude:"),
                  input(`type` := "text", cls := "form-control", id := "latitude", name := "latitude", autocomplete := "off", value := latitude.getOrElse(""))
                ),
                div(cls := "mb-3")(
                  label(`for` := "longitude", cls := "form-label")("Longitude:"),
                  input(`type` := "text", cls := "form-control", id := "longitude", name := "longitude", autocomplete := "off", value := longitude.getOrElse(""))
                ),
                div(cls := "mb-3")(
                  input(`type` := "submit", cls := "btn btn-primary", value := "Submit")
                )
              )
            ),
            div(id := "tab-content3", cls := "tab-content")(
              div(cls := "row")(
                div(cls := "form-container col-md-4")(
                  h2("Map Search"),
                  form(id := "mapForm", autocomplete := "off")(
                    div(cls := "mb-3")(
                      label(`for` := "city", cls := "form-label")("City:"),
                      input(`type` := "text", cls := "form-control", id := "map-city", name := "city", autocomplete := "off", disabled := "disabled")
                    ),
                    div(cls := "mb-3")(
                      label(`for` := "state", cls := "form-label")("State:"),
                      input(`type` := "text", cls := "form-control", id := "map-state", name := "state", autocomplete := "off", disabled := "disabled")
                    ),
                    div(cls := "mb-3")(
                      label(`for` := "lat", cls := "form-label")("Latitude:"),
                      input(`type` := "text", cls := "form-control", id := "map-latitude", name := "latitude", autocomplete := "off", disabled := "disabled")
                    ),
                    div(cls := "mb-3")(
                      label(`for` := "long", cls := "form-label")("Longitude:"),
                      input(`type` := "text", cls := "form-control", id := "map-longitude", name := "longitude", autocomplete := "off", disabled := "disabled")
                    ),
                    div(cls := "mb-3")(
                      input(`type` := "submit", cls := "btn btn-primary", value := "Submit")
                    )
                  )
                ),
                div(cls := "map-container col-md-8")(
                  div(id := "map", style := "width: 100%; height: 400px;")
                )
              )
            )
          ),
          div(cls := "row mt-4")(
            div(cls := "col-md-12")(
              div(id := "weatherInfo")(
                if (location.isDefined || (latitude.isDefined && longitude.isDefined)) {
                  weatherInfo.map { case (shortForecast, temperature, dayTemperature, nightTemperature, currentTemperature, characterization, icon) =>
                    div(cls := "card")(
                      div(cls := "card-body")(
                        h5(cls := "card-title")(locationDisplay),
                        div(cls := "d-flex align-items-center")(
                          if (icon.nonEmpty) img(src := icon, cls := "weather-icon mr-3") else "",
                          div(cls := "flex-grow-1")(
                            p(cls := "display-4")(s"$currentTemperature°F (Current)"),
                            p(cls := "lead")(shortForecast),
                            p(cls := "text-muted")(s"Day: $dayTemperature°F, Night: $nightTemperature°F"),
                            p(cls := "text-muted")(s"The outside air feels $characterization right now")
                          )
                        )
                      )
                    )
                  }.getOrElse(
                    div(cls := "alert alert-danger")("Failed to fetch weather data.")
                  )
                } else {
                  div()
                }
              )
            )
          )
        ),
        script(src := "/static/js/bootstrap.min.js"),
        script(src := "/static/js/alt-autocomplete.js"),
        script(src := "/static/js/openlayers.min.js"),
        script(src := "/static/js/map.js")
      )
    ).render
  }

  private def formatCityState(cityState: String): String = {
    val parts = cityState.split(",").map(_.trim)
    if (parts.length == 2) {
      val city = parts(0).split(" ").map(_.capitalize).mkString(" ")
      val state = parts(1).toUpperCase
      s"$city, $state"
    } else {
      cityState
    }
  }
}