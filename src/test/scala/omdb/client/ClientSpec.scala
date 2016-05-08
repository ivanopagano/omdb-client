package omdb.client

import akka.actor.ActorSystem
import org.scalatest._

/** Defines a contract for the omdb client
  * Test common cases
  */
class ClientSpec extends FlatSpec
  with Matchers
  with BeforeAndAfterAll
  with MovieClientTesting {

  "A OpenMovieClient" should "fetch a list of movie entries when called with a valid title" in {
    val movies = client.listMoviesWithTitleLike("zoolander")

    movies should not be empty
  }

  it should "return an empty list if the search title match no movie" in {
    val movies = client.listMoviesWithTitleLike("zoorlander")

    movies shouldBe empty
  }

  it should "convert the json results to a full OpenMovie object with the correct values" in {
    val movie = client
      .listMoviesWithTitleLike("zoolander")
      .find(_.title == "Zoolander")

    movie shouldBe defined

    import  org.scalatest.OptionValues._

    movie.value should have (
      'title ("Zoolander"),
      'year ("2001"),
      'genre ("Comedy"),
      'director ("Ben Stiller"),
      'actors ("Ben Stiller, Owen Wilson, Christine Taylor, Will Ferrell")
    )
  }

}
