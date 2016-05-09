package omdb.client

import akka.actor.ActorSystem
import org.scalatest._

/** Defines a contract for the omdb client
  * Test common cases
  */
class ClientSpec extends AsyncFlatSpec
  with Matchers
  with BeforeAndAfterAll
  with MovieClientTesting {

  "A OpenMovieClient" should "eventually fetch a list of movie entries when called with a valid title" in {
    val movies = client.listMoviesWithTitleLike("zoolander")

    movies map (_ should not be empty)
  }

  it should "eventually return an empty list if the search title match no movie" in {
    val movies = client.listMoviesWithTitleLike("zoorlander")

    movies map (_ shouldBe empty)
  }

  it should "eventually convert the json results to a full OpenMovie object with the correct values" in {
    val movie = client
      .listMoviesWithTitleLike("zoolander")
      .map(_.find(_.title == "Zoolander"))

    import org.scalatest.OptionValues._

    movie map (_.value should have(
      'title ("Zoolander"),
      'year ("2001"),
      'genre ("Comedy"),
      'director ("Ben Stiller"),
      'actors ("Ben Stiller, Owen Wilson, Christine Taylor, Will Ferrell")
    ))
  }

}
