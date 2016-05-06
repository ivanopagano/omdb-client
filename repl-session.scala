import spray.http._
import spray.client.pipelining._

import akka.actor.ActorSystem
implicit val system = ActorSystem()
import system.dispatcher
val searchPipeline = sendReceive
val searchFor = "the+matrix"
val search = searchPipeline(Get(s"http://www.omdbapi.com/?s=$searchFor&plot=full&r=json"))

import scala.concurrent.Await
import scala.concurrent.duration._

val result = Await.result(search, 1 second))
val movies = result.Search


//protocol
import spray.httpx.SprayJsonSupport._
import spray.json.DefaultJsonProtocol
// case class Movie(Title: String, Year: String, Rated: String, Released: String, Runtime: String, Genre: String, Director: String, Writer: String, Actors: String, Plot: String, Language: String, Country: String, Awards: String, Poster: String, Type: String)
case class MovieSearchEntry(Title: String, Year: String, imdbID: String, Type: String, Poster: String)
case class MovieSearchList(Search: Array[MovieSearchEntry])

object MovieProtocol extends DefaultJsonProtocol {
  // implicit val movie = jsonFormat15(Movie)
  implicit val entryFormat = jsonFormat5(MovieSearchEntry)
  implicit val searchFormat = jsonFormat1(MovieSearchList)
}
import MovieProtocol._
val searchPipeline = sendReceive ~> unmarshal[MovieSearchList]
// val moviePipeline = sendReceive ~> unmarshal[Movie]
val searchFor = "the+matrix"
val search = searchPipeline(Get(s"http://www.omdbapi.com/?s=$searchFor&plot=full&r=json"))
val result = Await.result(search, 1 second))
val movies = result.Search

movies.find(_.Title contains "Revolutions").map(_.Poster)
