package omdb.client

import omdb.client.api._

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

class OpenMovieClientDef extends OpenMovieClient {
  override def listMoviesWithTitleLike(title: String): Array[OpenMovie] = ???
}

