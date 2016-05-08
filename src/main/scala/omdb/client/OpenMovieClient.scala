package omdb.client

import akka.actor.ActorSystem
import omdb.client.api._
import omdb.client.valdation.{ParameterValidation, ValidEndpoint}
import spray.httpx.SprayJsonSupport

import scala.concurrent.Future
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
)(implicit system: ActorSystem) extends OpenMovieClient with SprayJsonSupport with ParameterValidation {
  //use spray modules
  import spray.client.pipelining._
  import spray.httpx.PipelineException

  private val titleEndPoint = validate(remoteProtocol, remoteHost, queryConfiguration, byTitleParam)
  private val movieEndPoint = validate(remoteProtocol, remoteHost, queryConfiguration, byIdParam)

  private def asyncEndPoint(validEndpoint: Option[ValidEndpoint]): Future[ValidEndpoint] = validEndpoint match {
    case None => Future.failed(new IllegalArgumentException("Configuration parameters for the Movie Client are not valid"))
    case Some(endpoint) => Future.successful(endpoint)
  }

  override def listMoviesWithTitleLike(title: String): Array[OpenMovie] = {
    import MovieProtocol._
    import scala.concurrent.Await
    import scala.concurrent.duration._
    import java.net.URLEncoder.encode

    //defines the concurrent execution context
    import system.dispatcher

    val searchPipeline = sendReceive ~> unmarshal[MovieSearchList]
    val moviePipeline = sendReceive ~> unmarshal[OpenMovieDef]

    val encodedTitle = encode(title, "UTF-8")

    //compose operations on futures
    val results: Future[Seq[OpenMovieDef]] = for {
      searchUrl <- asyncEndPoint(titleEndPoint)
      MovieSearchList(entries) <- searchPipeline(Get(searchUrl buildQueryUrl encodedTitle))
      detailUrl <- asyncEndPoint(movieEndPoint)
      entryToMovie = (entry: MovieSearchEntry) => moviePipeline(Get(detailUrl buildQueryUrl entry.imdbID))
      entriesToMovie = (entrySeq: Seq[MovieSearchEntry]) => Future.traverse(entrySeq)(entryToMovie)
      movies <- entriesToMovie(entries)
    } yield movies

    //handle failure
    val safeResults = results.recover { case _: PipelineException => Seq.empty[OpenMovie] }

    //wait for the result
    Await.result(safeResults, 5 seconds).toArray

  }
}

