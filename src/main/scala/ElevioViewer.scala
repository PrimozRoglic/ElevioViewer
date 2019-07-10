package elevio.viewer

import net.team2xh.onions.{Palettes, Themes}
import net.team2xh.onions.components.Frame
import net.team2xh.onions.components.widgets._
import net.team2xh.onions.utils.{Varying, Lorem, TextWrap}
import net.team2xh.scurses.{Colors, Scurses}
import net.team2xh.scurses.RichText._

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer

import scala.concurrent.Future
import scala.util.{ Failure, Success }

import scala.collection.immutable.Seq
import scala.io.Source

import spray.json._

import models._

case class Config (
  domain: String,
  authKey: String,
  jwt: String
)

object ElevioViewer extends App {

  // TODO: proper args validation
  val filePath = (args mkString)
  val bufferedSource = Source.fromFile(filePath)
  val config = bufferedSource.getLines.toList match { 
    case List(domain, authKey, jwt) => Config(domain, authKey, jwt) 
  }
  bufferedSource.close

  val client = Client.build(config.domain, config.authKey, config.jwt)

  val searchPageSize = 3;
  var currentSearchPage = 1;

  Scurses { implicit screen =>
    try {
      implicit val debug = true
      val frame = Frame(title = Some("Elevio Article Viewer"),
                        debug = true, theme = Themes.default)

      val colA = frame.panel
      val colB = colA.splitRight
      val colC1 = colB.splitRight
      val colC2 = colC1.splitDown

      colA.title = "Aritcle List"
      colB.title = "Selected Article"

      colC1.title = "Search"
      colC2.title = "Messages"

      val articleString: Varying[String] = ""
      val articleLabel = Label(colB, articleString)
      

      val updateArticle = (article: SingleArticle) => {
        articleString := article.toString()
        frame.redraw();
      }

      val updateArticles = (articles: Articles) => {
        articles.articles.foreach((article: Article) => {
          Label(colA, s"${article.id}: ${article.title}", action = () => client.article(article.id, updateArticle))
        })
        frame.redraw();
      }





      val input = Input(colC1, "Search")

      // this is a hack to remove Widgets from a FramePanel, since it is missing from library
      val resetSearch = () => {
        colC1.widgets.trimEnd(colC1.widgets.length-2)
        colC1.tabs(colC1.currentTab) = (colC1.widgets, 1, colC1.heights)
      }

      lazy val searchAction = () => {
        resetSearch()
        client.search(page=Some(currentSearchPage), rows=Some(searchPageSize),
          query=input.text.value, callback=showSearch)
      }


      Label(colC1, "GO", TextWrap.CENTER, action = () => {
        currentSearchPage = 1
        searchAction()
      })



      lazy val showSearch: (SearchResponse) => Unit = (searchResponse: SearchResponse) => {
        searchResponse.results.foreach((article: SearchArticle) => {
          Label(colC1, s"${article.id}: ${article.title}", action = () => client.article(article.id.toInt, updateArticle))
        })
        Label(colC1, "Next", TextWrap.CENTER, action = () => {
          currentSearchPage += 1
          searchAction()
        })
        Label(colC1, "Prev", TextWrap.CENTER, action = () => {
          currentSearchPage -= 1
          searchAction()
        })
        frame.redraw();
      }

      

      client.articles(callback = updateArticles)

      frame.show()
    } catch {
      case e => {
        screen.close()
        e.printStackTrace()
      }
    }
  }

  client.system.terminate();
}