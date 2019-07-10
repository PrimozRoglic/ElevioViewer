package elevio.viewer.models

import spray.json._
import DefaultJsonProtocol._

// sum type
sealed trait Model

case class SingleArticle(
  article: FullArticle
) extends Model

case class FullArticle(
  id: Int,
  title: String,
  author: Author,
  translations: Seq[Translation]
) extends Model

case class Author(
  id: Int,
  name: String
)

case class Translation(
  id: Int,
  title: String,
  body: String
)

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
  implicit val authorFormat = jsonFormat2(Author)
  implicit val translationFormat = jsonFormat3(Translation)
  implicit val fullArticleFormat = jsonFormat4(FullArticle)
  implicit val singleArticleFormat = jsonFormat1(SingleArticle)
  implicit val searchArticle = jsonFormat2(SearchArticle)
  implicit val searchResponse = jsonFormat6(SearchResponse)
}

// sum type
sealed trait LangCode

case class En() extends LangCode
case class De() extends LangCode


