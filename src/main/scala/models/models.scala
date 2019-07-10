package elevio.viewer.models

import spray.json._
import DefaultJsonProtocol._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport

// sum type
sealed trait Model

case class SingleArticle(
  article: Article
) extends Model

case class Article(
  id: Int,
  title: String
) extends Model 

case class SearchArticle(
  id: String,
  title: String
) extends Model 

case class Articles (
  articles: Seq[Article],
  page_number: Int,
  page_size: Int,
  total_pages: Int,
  total_entries: Int
) extends Model

case class SearchResponse(
  queryTerm: String,
  totalResults: Int,
  totalPages: Int,
  currentPage: Int,
  count: Int,
  results: Seq[SearchArticle]
) extends Model

object ArticlesProtocol extends DefaultJsonProtocol {
  implicit val aritcleFormat = jsonFormat2(Article)
  implicit val aritclesFormat = jsonFormat5(Articles)
  implicit val singleArticleFormat = jsonFormat1(SingleArticle)
  implicit val searchArticle = jsonFormat2(SearchArticle)
  implicit val searchResponse = jsonFormat6(SearchResponse)
}

import scala.reflect.ClassTag

sealed trait LangCode

case class En() extends LangCode
case class De() extends LangCode


