package omdb.client

import java.time.LocalDateTime
import java.time.LocalDateTime._

import akka.actor.ActorSystem
import spray.client.pipelining._
import spray.http.CacheDirectives.{`max-age`, public}
import spray.http.HttpHeaders.`Cache-Control`
import spray.http.{CacheDirective, HttpRequest, HttpResponse}

import scala.concurrent.Future

/**
  * Created by Ivano Pagano on 09/05/16.
  */
trait PipelineCaching {

  private[client] def cache: Map[HttpRequest, (HttpResponse, LocalDateTime)]

  private[client] def cache_=(newCache: Map[HttpRequest, (HttpResponse, LocalDateTime)])

  private[client] def system: ActorSystem

  val caching = (rr: (HttpRequest, HttpResponse)) => {
    import scala.concurrent.ExecutionContext.Implicits.global

    val (req, res) = rr
    Future {
      val ageToExpiration: PartialFunction[CacheDirective, LocalDateTime] = {case `max-age`(deltaSeconds) => now plusSeconds deltaSeconds}
      for {
        `Cache-Control`(directives) <- res.header[`Cache-Control`]
        _ <- directives.find(_ == public)
        expire <- directives collectFirst ageToExpiration
      } {
        cache += (req ->(res, expire))
      }
    }
    res
  }

  val requestMemorySend: HttpRequest => Future[(HttpRequest, HttpResponse)] = req => {
    implicit val actorSystem = system
    import actorSystem.dispatcher
    val packWithRequest = (pipeSend: HttpRequest => Future[HttpResponse]) => (req: HttpRequest) =>
      pipeSend(req) map ((req, _))
    cache get req match {
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
  }

}
