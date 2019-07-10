package elevio.viewer

import net.team2xh.onions.{Themes}
import net.team2xh.onions.components.Frame
import net.team2xh.onions.components.widgets._
import net.team2xh.onions.utils.{Varying, TextWrap}
import net.team2xh.scurses.{Scurses}

import scala.io.Source

import models._

case class Config (
  domain: String,
  authKey: String,
  jwt: String
)
// this was an attempt to reduce repetition
trait Pages {
  var currentPage: Int
  var totalPages: Int
}
object UIstate {
  object ListState extends Pages {
    var currentPage = 1
    var totalPages = 100000
  }
  object SearchState extends Pages {
    var currentPage = 1
    var totalPages = 100000
  }
}

object ElevioViewer extends App {

  // TODO: proper args validation
  val filePath = (args mkString)
  val bufferedSource = Source.fromFile(filePath)
  val config = bufferedSource.getLines.toList match { 
    case List(domain, authKey, jwt) => Config(domain, authKey, jwt) 
  }
  bufferedSource.close

  val client = Client.build(config.domain, config.authKey, config.jwt)

  // this could be configurable
  val searchPageSize = 5;

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

      // ------------ Messages ------------

      val messageString: Varying[String] = ""

      val messageLabel = Label(colC2, messageString)

      val defaultErrorCallback = (t: Throwable) => {
        messageString := s"An error occured: ${t.getMessage()}"
        frame.redraw();
      }

      // ------------ End of Messages ------------

      // ------------ Article ------------

      val translationToString = (translation: Translation) => {
        translation match {
          case Translation(id, title, body) => s"{title: $title | body: $body}" 
        }
      }

      val authorToString = (author: Author) => {
        author match {
          case Author(id, name) => s"{id: $id | name: $name}"
        }
      }

      val fullArticleToString = (fullArticle: FullArticle) => {
        fullArticle match {
          case FullArticle(id, _, author, translations) => {
            s"{id: $id | author: ${authorToString(author)} | translation: ${translationToString(translations(0))}}"
          }
        }
      }

      val articleString: Varying[String] = ""

      val articleLabel = Label(colB, articleString)

      val updateArticle = (article: SingleArticle) => {
        articleString := fullArticleToString(article.article)
        frame.redraw();
      }

      // ------------ End of Article ------------

      // ------------ Article List ------------

      // this is a hack to remove Widgets from a FramePanel, since it is missing from library
      val resetArticles = () => {
        colA.widgets.trimEnd(colA.widgets.length)
        colA.tabs(colA.currentTab) = (colA.widgets, 0, colA.heights)
      }

      lazy val listAction = () => {
        resetArticles()
        client.articles(page=Some(UIstate.ListState.currentPage), pageSize=Some(searchPageSize),
          callback = showArticles, errorCallback = defaultErrorCallback)
      }

      lazy val showArticles: (Articles) => Unit = (articles: Articles) => {
        UIstate.ListState.totalPages = articles.total_pages
        articles.articles.foreach((article: Article) => {
          Label(colA, s"${article.id}: ${article.title}", action = () => client.article(article.id, updateArticle, errorCallback = defaultErrorCallback))
        })
        if (UIstate.ListState.currentPage < UIstate.ListState.totalPages) {
          Label(colA, "Next", TextWrap.CENTER, action = () => {
            UIstate.ListState.currentPage += 1
            listAction()
          })
        }
        if (UIstate.ListState.currentPage > 1) {
          Label(colA, "Prev", TextWrap.CENTER, action = () => {
            UIstate.ListState.currentPage -= 1
            listAction()
          })
        }
        Label(colA, s"Page ${UIstate.ListState.currentPage}/${UIstate.ListState.totalPages}", TextWrap.CENTER)
        frame.redraw();
      }

      val updateArticles = (articles: Articles) => {
        UIstate.SearchState.currentPage = 1
        articles.articles.foreach((article: Article) => {
          Label(colA, s"${article.id}: ${article.title}", action = () => client.article(article.id, updateArticle, errorCallback = defaultErrorCallback))
        })
        frame.redraw();
      }

      // ------------ End of Article List ------------

      // ------------ Search ------------

      val input = Input(colC1, "Search")

      // this is a hack to remove Widgets from a FramePanel, since it is missing from library
      val resetSearch = () => {
        colC1.widgets.trimEnd(colC1.widgets.length-2)
        colC1.tabs(colC1.currentTab) = (colC1.widgets, 1, colC1.heights)
      }

      lazy val searchAction = () => {
        resetSearch()
        client.search(page=Some(UIstate.SearchState.currentPage), rows=Some(searchPageSize),
          query=input.text.value, callback=showSearch, errorCallback = defaultErrorCallback)
      }

      Label(colC1, "GO", TextWrap.CENTER, action = () => {
        UIstate.SearchState.currentPage = 1
        searchAction()
      })

      lazy val showSearch: (SearchResponse) => Unit = (searchResponse: SearchResponse) => {
        UIstate.SearchState.totalPages = searchResponse.totalPages
        searchResponse.results.foreach((article: SearchArticle) => {
          Label(colC1, s"${article.id}: ${article.title}", action = () => client.article(article.id.toInt, updateArticle, errorCallback = defaultErrorCallback))
        })
        if (UIstate.SearchState.currentPage < UIstate.SearchState.totalPages) {
          Label(colC1, "Next", TextWrap.CENTER, action = () => {
            UIstate.SearchState.currentPage += 1
            searchAction()
          })
        }
        if (UIstate.SearchState.currentPage > 1) {
          Label(colC1, "Prev", TextWrap.CENTER, action = () => {
            UIstate.SearchState.currentPage -= 1
            searchAction()
          })
        }
        Label(colC1, s"Page ${UIstate.SearchState.currentPage}/${UIstate.SearchState.totalPages}", TextWrap.CENTER)
        frame.redraw();
      }

      // ------------ End of Search ------------

      // init 
      listAction()
      frame.show()

    } catch {
      case e: Throwable => {
        screen.close()
        client.close()
        e.printStackTrace()
      }
    }
  }

  client.close();
}