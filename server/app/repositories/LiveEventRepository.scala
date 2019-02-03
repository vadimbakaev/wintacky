package repositories

import com.google.inject.ImplementedBy
import com.mongodb.ConnectionString
import javax.inject.{Inject, Singleton}
import models.{Address, LiveEvent, Price}
import org.bson.codecs.configuration.CodecRegistries._
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.model.{Filters, IndexOptions, Indexes}
import org.mongodb.scala.{Completed, _}
import play.api.{Configuration, Logging}
import repositories.codec.LocalDateCodec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@ImplementedBy(classOf[LiveEventRepositoryImpl])
trait LiveEventRepository {

  def save(event: LiveEvent): Future[Completed]

  def get(_id: ObjectId): Future[Option[LiveEvent]]

}

@Singleton
class LiveEventRepositoryImpl @Inject()(
    config: Configuration
) extends LiveEventRepository
    with Logging {

  private[this] lazy val mongodbURI     = config.get[String](LiveEventRepositoryImpl.Uri)
  private[this] lazy val databaseName   = config.get[String](LiveEventRepositoryImpl.Db)
  private[this] lazy val userName       = config.get[String](LiveEventRepositoryImpl.UserName)
  private[this] lazy val password       = config.get[String](LiveEventRepositoryImpl.Password)
  private[this] lazy val collectionName = config.get[String](LiveEventRepositoryImpl.CollectionName)

  private[this] lazy val clientSettings: MongoClientSettings = MongoClientSettings
    .builder()
    .applyConnectionString(new ConnectionString(mongodbURI))
    .credential(MongoCredential.createCredential(userName, databaseName, password.toCharArray))
    .build()

  private[this] lazy val database: MongoDatabase = MongoClient(clientSettings)
    .getDatabase(databaseName)
    .withCodecRegistry(
      fromRegistries(
        fromCodecs(new LocalDateCodec),
        fromProviders(classOf[LiveEvent], classOf[Address], classOf[Price]),
        DEFAULT_CODEC_REGISTRY,
      )
    )

  private[this] lazy val collection: MongoCollection[LiveEvent] = initCollection(database)

  private[this] def initCollection(database: MongoDatabase): MongoCollection[LiveEvent] = {
    val collection = database.getCollection[LiveEvent](collectionName)
    collection.createIndex(Indexes.ascending(), IndexOptions().background(true).unique(true))
    collection
  }

  override def save(event: LiveEvent): Future[Completed] = collection.insertOne(event).toFuture.recover {
    case t @ _ => logger.error("Fail to save event", t); throw t
  }

  override def get(_id: ObjectId): Future[Option[LiveEvent]] =
    collection.find[LiveEvent](Filters.eq("_id", _id)).toFuture().map(_.headOption).recover {
      case t @ _ => logger.error("Fail to get event", t); throw t
    }
}

object LiveEventRepositoryImpl {
  val Uri            = "mongo.uri"
  val Db             = "mongo.database"
  val UserName       = "mongo.username"
  val Password       = "mongo.password"
  val CollectionName = "mongo.collection"
}
