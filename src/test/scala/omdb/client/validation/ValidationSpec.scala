package omdb.client.validation

import omdb.client.valdation.{ParameterValidation, ValidEndpoint}
import org.scalatest._

import scala.util.Success

/**
  * Created by Ivano Pagano on 08/05/16.
  */
class ValidationSpec extends FlatSpec with Matchers {

  val validator = new ParameterValidation {}

  "The parameter validator " should "fail when one of the mandatory params is empty" in {
    validator.validate(protocol = "", host = "www.host.it", queryParam = "q") shouldBe 'failure
    validator.validate(protocol = "http", host = "", queryParam = "q") shouldBe  'failure
    validator.validate(protocol = "http", host = "www.host.it", queryParam = "") shouldBe  'failure
  }
  it should "reject an unknown protocol " in {
    validator.validate(protocol = "file", host = "www.host.it", queryParam = "q") shouldBe  'failure
  }
  it should "reject a mistyped protocol " in {
    validator.validate(protocol = "httc", host = "www.host.it", queryParam = "q") shouldBe  'failure
  }
  it should "reject a short hostname " in {
    validator.validate(protocol = "http", host = "www.it", queryParam = "q") shouldBe  'failure
  }
  it should "reject a hostname with missing text between levels" in {
    validator.validate(protocol = "http", host = "www..it", queryParam = "q") shouldBe  'failure
  }
  it should "reject a hostname with unusual characters" in {
    validator.validate(protocol = "http", host = "www.$dd.i1t", queryParam = "q") shouldBe  'failure
  }
  it should "reject a hostname with invalid port" in {
    validator.validate(protocol = "http", host = "www.host.it:7wd", queryParam = "q") shouldBe  'failure
  }
  it should "reject a query parameter with unusual characters" in {
    validator.validate(protocol = "http", host = "www.host.it", queryParam = "q_3") shouldBe  'failure
  }
  it should "reject parameters with unusual characters" in {
    validator.validate(
      protocol = "http",
      host = "www.host.it",
      queryParam = "q",
      params = Map("%1" -> "ok", "r" -> "json")
    ) shouldBe  'failure
  }
  it should "accept both HTTP/S protocols with simple host and alphanumeric parameters" in {
    validator.validate(
      protocol = "http",
      host = "www.host.it",
      queryParam = "q",
      params = Map("a" -> "1", "b" -> "2")) shouldBe Success(ValidEndpoint("http://www.host.it", "a=1&b=2", "q"))
    validator.validate(
      protocol = "https",
      host = "www.host.it",
      queryParam = "q2",
      params = Map("a" -> "1", "b" -> "2")) shouldBe Success(ValidEndpoint("https://www.host.it", "a=1&b=2", "q2"))
  }
  it should "accept a port number and a trailing slash in the host" in {
    validator.validate(
      protocol = "http",
      host = "www.host.it:1234",
      queryParam = "q",
      params = Map("a" -> "1", "b" -> "2")) shouldBe Success(ValidEndpoint("http://www.host.it:1234", "a=1&b=2", "q"))
    validator.validate(
      protocol = "https",
      host = "www.host.it:1234/",
      queryParam = "q2",
      params = Map("a" -> "1", "b" -> "2")) shouldBe Success(ValidEndpoint("https://www.host.it:1234/", "a=1&b=2", "q2"))
  }

}
