package omdb.client

import spray.json.DefaultJsonProtocol

//define object representation for json intermediate results
case class MovieSearchEntry(Title: String, Year: String, imdbID: String, Type: String, Poster: String)

case class MovieSearchList(Search: Seq[MovieSearchEntry])

/**
  * Created by Ivano Pagano on 06/05/16.
  */
object MovieProtocol extends DefaultJsonProtocol {
  implicit val movieFormat = jsonFormat(
    OpenMovieDef,
    "Title",
    "Year",
    "Rated",
    "Released",
    "Runtime",
    "Genre",
    "Director",
    "Writer",
    "Actors",
    "Plot",
    "Language",
    "Country",
    "Awards",
    "Poster",
    "Type")
  implicit val entryFormat = jsonFormat5(MovieSearchEntry)
  implicit val searchFormat = jsonFormat1(MovieSearchList)
}
