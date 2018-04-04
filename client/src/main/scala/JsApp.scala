import org.scalajs.dom._
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.html.{Button, Div, Input}
import org.scalajs.dom.raw.KeyboardEvent

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js.Dynamic.literal
import scala.scalajs.js.annotation.JSExport
import cats.implicits._


object JsApp {

  @JSExport
  def main(args: Array[String]): Unit = {
    println("JsApp Started")

    val searchInput: Input   = document.getElementById("search-input").asInstanceOf[Input]
    val searchButton: Button = document.getElementById("search-btn").asInstanceOf[Button]

    def getSearchKey = searchInput.value

    def updateElementById(id: String, response: XMLHttpRequest): Unit = {
      val oldElement = document.getElementById(id)
      val newElement = response.responseXML.getElementById(id)
      oldElement.innerHTML = newElement.innerHTML
    }

    def reload(key: String = getSearchKey, pushState: Boolean = true): Unit = {
      val searchPath = s"/search?key=$key"

      Ajax
        .get(searchPath, responseType = "document")
        .foreach { response: XMLHttpRequest =>
          val starter = document.getElementById("starter-block").asInstanceOf[Div]
          starter.style.display = "none"

          updateElementById("search-result", response)
        }

      if (pushState) {
        window.history.pushState(literal(key = key), "", searchPath)
      }
    }

    searchInput.onkeypress = (e: KeyboardEvent) => if ("Enter" === e.key) reload()
    searchButton.onclick = (e: Event) => reload()

  }

}
