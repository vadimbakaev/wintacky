package services

import java.util.Base64

import cats.implicits._
import com.google.inject.{ImplementedBy, Singleton}
import javax.inject.Inject
import models.{LiveEventStub, LiveEventView}
import org.apache.http.message.BasicHeader
import org.apache.http.{HttpHeaders, HttpHost}
import org.elasticsearch.action.search.{SearchRequest, SearchResponse}
import org.elasticsearch.action.update.{UpdateRequest, UpdateResponse}
import org.elasticsearch.client.{RestClient, RestHighLevelClient}
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.index.query.QueryStringQueryBuilder
import org.elasticsearch.search.builder.SearchSourceBuilder
import play.api.Configuration
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

@ImplementedBy(classOf[SearchServiceImpl])
trait SearchService {
  @deprecated
  def searchStub(key: String): Future[Seq[LiveEventStub]]

  def search(key: String): Future[Seq[LiveEventView]]

  def upsert(event: LiveEventView): Future[String]

}

@Singleton
class SearchServiceImpl @Inject()(
    configuration: Configuration,
) extends SearchService {

  private[this] lazy val elasticHost: String   = configuration.get[String]("elastic.host")
  private[this] lazy val elasticPort: Int      = configuration.get[Int]("elastic.port")
  private[this] lazy val elasticScheme: String = configuration.get[String]("elastic.scheme")
  private[this] lazy val accessKey: String     = configuration.get[String]("elastic.access.key")
  private[this] lazy val accessSecret: String  = configuration.get[String]("elastic.access.secret")
  private[this] lazy val host: HttpHost        = new HttpHost(elasticHost, elasticPort, elasticScheme)
  private[this] lazy val loginPassword         = accessKey + ":" + accessSecret
  private[this] lazy val authorization: BasicHeader = new BasicHeader(
    HttpHeaders.AUTHORIZATION,
    s"Basic ${Base64.getEncoder.withoutPadding().encodeToString(loginPassword.getBytes)}"
  )

  override def searchStub(key: String): Future[Seq[LiveEventStub]] =
    Future {
      val client = new RestHighLevelClient(RestClient.builder(host).setDefaultHeaders(Array(authorization)))
      val searchRequest = new SearchRequest()
        .indices("wintacky")
        .source(new SearchSourceBuilder().query(new QueryStringQueryBuilder(key)))

      val response: SearchResponse = client.search(searchRequest)

      Try(client.close())

      response.getHits.getHits
        .filter(_.getType === "live-event")
        .flatMap(hint => Json.parse(hint.getSourceAsString).validate[LiveEventStub].asOpt)
        .toSeq
    }

  override def search(key: String): Future[Seq[LiveEventView]] =
    Future {
      val client = new RestHighLevelClient(RestClient.builder(host).setDefaultHeaders(Array(authorization)))
      val searchRequest = new SearchRequest()
        .indices("collection")
        .source(new SearchSourceBuilder().query(new QueryStringQueryBuilder(key)))

      val response: SearchResponse = client.search(searchRequest)

      Try(client.close())

      response.getHits.getHits
        .filter(_.getType === "event")
        .flatMap(hint => Json.parse(hint.getSourceAsString).validate[LiveEventView].asOpt)
        .toSeq
    }

  override def upsert(event: LiveEventView): Future[String] =
    Future {
      val client = new RestHighLevelClient(RestClient.builder(host).setDefaultHeaders(Array(authorization)))
      val updateRequest = new UpdateRequest("collection", "event", event.id)
        .doc(Json.toJson(event).toString(), XContentType.JSON)
        .docAsUpsert(true)

      val response: UpdateResponse = client.update(updateRequest)

      Try(client.close())

      response.getId
    }
}
