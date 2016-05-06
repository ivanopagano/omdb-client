package omdb.client

import omdb.client.api._
import spray.httpx.SprayJsonSupport

case class OpenMovieDef(
  title: String,
  year: String,
  rated: String,
  released: String,
  runtime: String,
  genre: String,
  director: String,
  writer: String,
  actors: String,
  plot: String,
  language: String,
  country: String,
  awards: String,
  poster: String,
  `type`: String
) extends OpenMovie

class OpenMovieClientDef extends OpenMovieClient with SprayJsonSupport {
  //use spray modules
  import spray.client.pipelining._
  import spray.httpx.PipelineException

  //needed to use spray client (based on akka)
  import akka.actor.ActorSystem
  implicit val system = ActorSystem()

  override def listMoviesWithTitleLike(title: String): Array[OpenMovie] = {
    import MovieProtocol._
    import scala.concurrent.{Await, Future}
    import scala.concurrent.duration._
    //defines the concurrent execution context
    import system.dispatcher

    val searchPipeline = sendReceive ~> unmarshal[MovieSearchList]
    val moviePipeline = sendReceive ~> unmarshal[OpenMovieDef]

    //search results in the future
    val outcome: Future[MovieSearchList] = searchPipeline(Get(s"http://www.omdbapi.com/?s=$title&r=json"))

    val results: Future[Seq[OpenMovieDef]] = outcome flatMap { searchList =>
      Future.sequence(searchList.Search map {
        entry => moviePipeline(Get(s"http://www.omdbapi.com/?i=${entry.imdbID}&plot=full&r=json"))
      })
    }

    //handle failure
    val safeResults = results.recover {case _:PipelineException => Seq.empty[OpenMovie]}

    Await.result(safeResults, 5 seconds).toArray

  }
}

