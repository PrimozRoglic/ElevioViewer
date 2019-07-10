package elevio.viewer

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import scala.collection.immutable.Seq

import models._

import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.RootJsonFormat
import spray.json.DefaultJsonProtocol._

import scala.util.{ Failure, Success }

class Client(val domain: String, val authKey: String, val jwt: String) {

  
  // this stuff should be given by caller? 
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  // Any should be replaced with Integer | String sum type
  type QueryParam = (String, Option[Any])

  def getHeaders() = Seq(ApiKeyHeader(authKey), JwtHeader(s"Bearer ${jwt}"))
  def getUri(path: String, queryString: Option[String] = None): Uri = {
    Uri.from(scheme = "https", host = domain, path = path, queryString=queryString)
  }
  def flattenQueryParams(params: Seq[QueryParam]): Option[String] = {
    params.foldLeft(None: Option[String])((acc, curr) => {
      curr._2 match {
        case None => acc
        case Some(p) => acc match {
          case None => Some(s"${curr._1}=$p")
          case Some(soFar) => Some(s"$soFar&${curr._1}=$p")
        }
      }
    })
  }
  def performRequest[M <: Model](uri: Uri, callback: M => Unit)(implicit um: Unmarshaller[HttpEntity, M]): Unit = {
    val request = HttpRequest(uri = uri).withHeaders(getHeaders())
    Http().singleRequest(request)
      .onComplete {
        case Success(res) => {
          Unmarshal(res.entity).to[M].onComplete {
            case Success(a) => {
              callback(a)
            }
            case Failure(s) => sys.error(s.getMessage())
          }
        }
        case Failure(s)   => sys.error(s.getMessage())
      }
  }

  def langCodeToString(lc: LangCode): String = {
    lc match {
      case En() => "en"
      case De() => "de"
    }
  }

  def articlesUri(queryParams: Seq[QueryParam]): Uri = {
    getUri("/v1/articles", flattenQueryParams(queryParams))
  }
  def articlesQueryParams(page: Option[Int], pageSize: Option[Int]): Seq[QueryParam] = {
    Seq(("page", page), ("page_size", pageSize))
  }
  def articles(page: Option[Int] = None, pageSize: Option[Int] = None, callback: Articles => Unit) = {
    implicit val aritclesFormat = ArticlesProtocol.aritclesFormat

    val uri = articlesUri(articlesQueryParams(page, pageSize))
    performRequest[Articles](uri, callback);
  }

  def articleUri(id: Int): Uri = {
    getUri(s"/v1/articles/$id")
  }
  def article(id: Int, callback: SingleArticle => Unit) = {
    implicit val singleArticleFormat = ArticlesProtocol.singleArticleFormat
    val uri = articleUri(id)
    performRequest[SingleArticle](uri, callback);
  }

  def searchUri(langCode: LangCode, queryParams: Seq[QueryParam]): Uri = {
    getUri(s"/v1/search/${langCodeToString(langCode)}", flattenQueryParams(queryParams))
  }
  def searchQueryParams(query: String, page: Option[Int], rows: Option[Int]): Seq[QueryParam] = {
    Seq(("page", page), ("rows", rows), ("query", Some(query)))
  }
  def search(langCode: LangCode = En(), 
              page: Option[Int] = None,
              rows: Option[Int] = None,
              query: String, 
              callback: SearchResponse => Unit) {
    implicit val searchResponse = ArticlesProtocol.searchResponse
    val uri = searchUri(langCode, searchQueryParams(query, page, rows))
    performRequest[SearchResponse](uri, callback);
  }
}

object Client {
  def build(domain: String, authKey: String, jwt: String): Client = new Client(domain, authKey, jwt)

  
}