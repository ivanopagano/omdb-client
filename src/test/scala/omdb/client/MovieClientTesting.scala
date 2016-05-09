package omdb.client

import scala.language.postfixOps
import akka.actor.ActorSystem
import org.scalatest.BeforeAndAfterAll

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

/**
  * Created by Ivano Pagano on 09/05/16.
  */
trait MovieClientTesting { self: BeforeAndAfterAll =>

  implicit val system = ActorSystem()

  val client = new OpenMovieClientDef(
    remoteHost = "www.omdbapi.com",
    queryConfiguration = Map("plot" -> "full", "r" -> "json"),
    byTitleParam = "s",
    byIdParam = "i"
  )

  protected def eventually[T] (fut: Future[T]): T = Await.result(fut, 10 seconds)

  override def afterAll(): Unit = {
    system.terminate()
  }
}
