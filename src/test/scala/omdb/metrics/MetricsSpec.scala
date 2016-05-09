package omdb.metrics

import omdb.client.MovieClientTesting
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{BeforeAndAfterAll, Matchers, PropSpec}

/**
  * Testing the metrics measuring functions
  */
class MetricsSpec extends PropSpec
  with TableDrivenPropertyChecks
  with Matchers
  with BeforeAndAfterAll
  with MovieClientTesting {

  val titles = Table(
    "title",
    "Ben-Hur",
    "Ace Ventura",
    "Back to the future",
    "Some like it hot",
    "King Kong",
    "Altrimenti ci arrabbiamo"
  )

  import scala.concurrent.ExecutionContext.Implicits.global

  private val timedClientCall = Metrics.measuringAsyncExecution(client.listMoviesWithTitleLike, "Movie Listing")

  property("measuring a client call should never change the outcome") {
    forAll(titles) { title =>
      eventually(timedClientCall(title)) should equal (eventually(client listMoviesWithTitleLike title))
    }
  }

}
