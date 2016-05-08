package omdb.client

import akka.actor.ActorSystem
import org.scalatest.BeforeAndAfterAll

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

  override def afterAll(): Unit = {
    system.terminate()
  }
}
