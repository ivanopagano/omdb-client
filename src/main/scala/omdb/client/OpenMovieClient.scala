package omdb.client

import java.time.LocalDateTime

import akka.actor.ActorSystem
import omdb.client.api._
import omdb.client.valdation.{ParameterValidation, ValidEndpoint}
import spray.http.{CacheDirective, HttpRequest, HttpResponse}
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
)(implicit system: ActorSystem) extends OpenMovieClient with SprayJsonSupport with ParameterValidation {
  //use spray modules
  import spray.client.pipelining._
  import spray.httpx.PipelineException

  import spray.http.CacheDirectives._
  import spray.http.HttpHeaders.`Cache-Control`

  private[this] var clientCache = Map.empty[HttpRequest, (HttpResponse, LocalDateTime)]

  private val titleEndPoint = validate(remoteProtocol, remoteHost, queryConfiguration, byTitleParam)
  private val movieEndPoint = validate(remoteProtocol, remoteHost, queryConfiguration, byIdParam)

  override def listMoviesWithTitleLike(title: String): Array[OpenMovie] = {
    import MovieProtocol._
    import scala.concurrent.Await
    import scala.concurrent.duration._
    import java.net.URLEncoder.encode

    //defines the concurrent execution context
    import system.dispatcher

    import LocalDateTime._

    val caching = (rr: (HttpRequest, HttpResponse)) => {
      val (req, res) = rr
      Future {
        val ageToExpiration: PartialFunction[CacheDirective, LocalDateTime] = {case `max-age`(deltaSeconds) => now plusSeconds deltaSeconds}
        for {
          `Cache-Control`(directives) <- res.header[`Cache-Control`]
          _ <- directives.find(_ == public)
          expire <- directives collectFirst ageToExpiration
        } {
          clientCache += (req -> (res, expire))
        }
      }
      res
    }

    val packWithRequest = (pipeSend: HttpRequest => Future[HttpResponse]) => (req: HttpRequest) => pipeSend(req) map ((req, _))
    val requestMemorySend: HttpRequest => Future[(HttpRequest, HttpResponse)] = req =>
      clientCache.get(req) match {
        case Some((response, expiring)) if expiring isAfter now => {
          println(s"cache hit for ${req.uri}, valid until $expiring")
          Future.successful((req, response))
        }
        case Some((response, expiring)) => {
          println(s"cache stale for ${req.uri}, expired $expiring")
          packWithRequest(sendReceive)(req)
        }
        case None => {
          println(s"cache miss for ${req.uri}")
          packWithRequest(sendReceive)(req)
        }
      }

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

    //wait for the result
    Await.result(safeResults, 5 seconds).toArray

  }
}

