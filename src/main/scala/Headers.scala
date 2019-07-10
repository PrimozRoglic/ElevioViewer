package elevio.viewer

import akka.http.scaladsl.model.headers._
import scala.util.{Try}

final class ApiKeyHeader(token: String) extends ModeledCustomHeader[ApiKeyHeader] {
  override def renderInRequests = true
  override def renderInResponses = true
  override val companion = ApiKeyHeader
  override def value: String = token
}
object ApiKeyHeader extends ModeledCustomHeaderCompanion[ApiKeyHeader] {
  override val name = "x-api-key"
  override def parse(value: String) = Try(new ApiKeyHeader(value))
}

final class JwtHeader(token: String) extends ModeledCustomHeader[JwtHeader] {
  override def renderInRequests = true
  override def renderInResponses = true
  override val companion = JwtHeader
  override def value: String = token
}
object JwtHeader extends ModeledCustomHeaderCompanion[JwtHeader] {
  override val name = "Authorization"
  override def parse(value: String) = Try(new JwtHeader(value))
}