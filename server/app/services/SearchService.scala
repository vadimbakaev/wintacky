package services

import java.util.Base64

import com.google.inject.{ImplementedBy, Singleton}
import javax.inject.Inject
import models.LiveEvent
import org.apache.http.HttpHost
import org.apache.http.message.BasicHeader
import org.elasticsearch.action.search.{SearchRequest, SearchResponse}
import org.elasticsearch.client.{RestClient, RestHighLevelClient}
import org.elasticsearch.index.query.QueryStringQueryBuilder
import org.elasticsearch.search.builder.SearchSourceBuilder
import play.api.Configuration
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

@ImplementedBy(classOf[SearchServiceImpl])
trait SearchService {
  def search(key: String): Future[Seq[LiveEvent]]
}

@Singleton
class SearchServiceImpl @Inject()(
    configuration: Configuration
) extends SearchService {

  private lazy val elasticHost: String   = configuration.get[String]("elastic.host")
  private lazy val elasticPort: Int      = configuration.get[Int]("elastic.port")
  private lazy val elasticScheme: String = configuration.get[String]("elastic.scheme")
  private lazy val accessKey: String     = configuration.get[String]("elastic.access.key")
  private lazy val accessSecret: String  = configuration.get[String]("elastic.access.secret")
  private lazy val host: HttpHost        = new HttpHost(elasticHost, elasticPort, elasticScheme)
  private lazy val authorization: BasicHeader = new BasicHeader(
    "Authorization",
    s"Basic ${Base64.getEncoder.withoutPadding().encodeToString(s"$accessKey:$accessSecret".getBytes)}"
  )

  override def search(key: String): Future[Seq[LiveEvent]] =
    Future {
      val client = new RestHighLevelClient(RestClient.builder(host).setDefaultHeaders(Array(authorization)))
      val searchRequest = new SearchRequest()
        .indices("wintacky")
        .source(new SearchSourceBuilder().query(new QueryStringQueryBuilder(key)))

      val response: SearchResponse = client.search(searchRequest)

      Try(client.close())

      response.getHits.getHits
        .filter(_.getType == "live-event")
        .flatMap(hint => Json.parse(hint.getSourceAsString).validate[LiveEvent].asOpt)
        .toSeq
    }

}
