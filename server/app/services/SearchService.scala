package services

import com.google.inject.{ImplementedBy, Singleton}
import models.LiveEvent

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random

@ImplementedBy(classOf[SearchServiceImpl])
trait SearchService {
  def search(key: Option[String]): Future[Seq[LiveEvent]]
}

@Singleton
class SearchServiceImpl extends SearchService {
  override def search(key: Option[String]): Future[Seq[LiveEvent]] =
    Future.successful(key.map(k => SearchServiceImpl.EventsStub(k)).getOrElse(Nil))
}

object SearchServiceImpl {
  def EventsStub(key: String): Seq[LiveEvent] = (1 to Random.nextInt(23)).map(
    index =>
      LiveEvent(
        s"$key $index",
        "Warsaw, Poland",
        "6 April 2018 - 7 April 2018",
        "This is a wider card with supporting text below as a natural lead-in to additional content. This content is a little bit longer."
    )
  )
}
