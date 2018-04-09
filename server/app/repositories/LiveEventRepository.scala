package repositories

import com.google.inject.ImplementedBy
import com.mongodb.ConnectionString
import javax.inject.{Inject, Singleton}
import models.LiveEvent
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.connection.ClusterSettings
import org.mongodb.scala.model.{IndexOptions, Indexes}
import org.mongodb.scala.{Completed, _}
import play.api.Configuration

import scala.concurrent.Future

@ImplementedBy(classOf[LiveEventRepositoryImpl])
trait LiveEventRepository {

  def save(event: LiveEvent): Future[Completed]

}

@Singleton
class LiveEventRepositoryImpl @Inject()(
    config: Configuration
) extends LiveEventRepository {

  private[this] lazy val mongodbURI     = config.get[String](LiveEventRepositoryImpl.Uri)
  private[this] lazy val databaseName   = config.get[String](LiveEventRepositoryImpl.Db)
  private[this] lazy val userName       = config.get[String](LiveEventRepositoryImpl.UserName)
  private[this] lazy val password       = config.get[String](LiveEventRepositoryImpl.Password)
  private[this] lazy val collectionName = config.get[String](LiveEventRepositoryImpl.CollectionName)

  private[this] lazy val clusterSettings = ClusterSettings
    .builder()
    .applyConnectionString(new ConnectionString(mongodbURI))
    .build()

  private[this] lazy val clientSettings: MongoClientSettings = MongoClientSettings
    .builder()
    .credential(MongoCredential.createCredential(userName, databaseName, password.toCharArray))
    .clusterSettings(clusterSettings)
    .build()

  private[this] lazy val database: MongoDatabase = MongoClient(clientSettings)
    .getDatabase(databaseName)
    .withCodecRegistry(fromRegistries(fromProviders(classOf[LiveEvent]), DEFAULT_CODEC_REGISTRY))

  private[this] lazy val collection: MongoCollection[LiveEvent] = initCollection(database)

  private[this] def initCollection(database: MongoDatabase): MongoCollection[LiveEvent] = {
    val collection = database.getCollection[LiveEvent](collectionName)
    collection.createIndex(Indexes.ascending(), IndexOptions().background(true).unique(true))
    collection
  }

  override def save(event: LiveEvent): Future[Completed] = collection.insertOne(event).toFuture

}

object LiveEventRepositoryImpl {
  val Uri            = "mongo.uri"
  val Db             = "mongo.database"
  val UserName       = "mongo.username"
  val Password       = "mongo.password"
  val CollectionName = "mongo.collection"
}
