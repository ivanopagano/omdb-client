package omdb.client

import omdb.client.api.OpenMovie

case class OpenMovie(
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
) extends omdb.client.api.OpenMovie

class OpenMovieClient extends api.OpenMovieClient {
  override def listMoviesWithTitleLike(title: String): Array[OpenMovie] = ???
}

