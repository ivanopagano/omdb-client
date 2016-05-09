package omdb.client

import java.time.LocalDateTime

import akka.actor.ActorSystem
import omdb.client.api._
import omdb.client.valdation.ParameterValidation
import spray.http.{HttpRequest, HttpResponse}
import spray.httpx.SprayJsonSupport

import scala.concurrent.{ExecutionException, Future}
import scala.language.postfixOps

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

class OpenMovieClientDef(
  remoteProtocol: String = "http",
  remoteHost: String,
  queryConfiguration: Map[String, String] = Map.empty,
  byTitleParam: String,
  byIdParam: String
)(implicit val system: ActorSystem) extends OpenMovieClient
  with SprayJsonSupport
  with ParameterValidation
  with PipelineCaching {
  //use spray modules
  import spray.client.pipelining._
  import spray.httpx.PipelineException

  private[client] var cache = Map.empty[HttpRequest, (HttpResponse, LocalDateTime)]

  private val titleEndPoint = validate(remoteProtocol, remoteHost, queryConfiguration, byTitleParam)
  private val movieEndPoint = validate(remoteProtocol, remoteHost, queryConfiguration, byIdParam)

  override def listMoviesWithTitleLike(title: String): Future[Array[OpenMovie]] = {
    import java.net.URLEncoder.encode

    import MovieProtocol._

    import scala.concurrent.Await
    import scala.concurrent.duration._

    //defines the concurrent execution context
    import system.dispatcher

    val searchPipeline = requestMemorySend ~> caching ~> unmarshal[MovieSearchList]
    val moviePipeline = requestMemorySend ~> caching ~> unmarshal[OpenMovieDef]

    val encodedTitle = encode(title, "UTF-8")

    //compose operations on futures
    val results: Future[Seq[OpenMovieDef]] = for {
      searchUrl <- Future fromTry titleEndPoint
      MovieSearchList(entries) <- searchPipeline(Get(searchUrl buildQueryUrl encodedTitle))
      detailUrl <- Future fromTry movieEndPoint
      entryToMovie = (entry: MovieSearchEntry) => moviePipeline(Get(detailUrl buildQueryUrl entry.imdbID))
      entriesToMovie = (entrySeq: Seq[MovieSearchEntry]) => Future.traverse(entrySeq)(entryToMovie)
      movies <- entriesToMovie(entries)
    } yield movies

    //handle failure
    val safeResults = results.recover {
      case _: PipelineException | _: ExecutionException => Seq.empty[OpenMovie]
    }

    //return the async result
    safeResults map (_.toArray)

  }
}

