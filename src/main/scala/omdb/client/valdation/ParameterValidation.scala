package omdb.client.valdation

import scala.util.Try

/**
  * Creare un sistema di validazione dei parametri di configurazione del servizio
  * - protocollo
  * - host
  * - coppie di parametri e valori fissi
  * - parametri variabili (i.e. il termine di ricerca)
  *
  * Il client quindi ottiene questi parametri nel costruttore e andranno controllati secondo
  * questi semplici criteri
  * protocollo: http o https
  * host: formato separato da punti contenente lettere/numeri
  * parametri: alfanumerici
  */
trait ParameterValidation {

  def validate(
    protocol: String,
    host: String,
    params: Map[String, String] = Map.empty,
    queryParam: String): Try[ValidEndpoint] = {

    val validProtocol = (protocol: String) => ("^http(s?)$".r findFirstIn protocol).isDefined
    val validHost = (host: String) => ("""(\w+\.){2,}\w+(:\d+)?/?$""".r findFirstIn host).isDefined
    val validParam = (param: String) => param.nonEmpty && param.forall(_.isLetterOrDigit)
    val validParamMap = (paramMap: Map[String, String]) => paramMap forall {
      case (key, value) => validParam(key) && validParam(value)
    }

    def concatParams(pm: Map[String, String]): String =
      pm.map {
        case (key, value) => key + "=" + value
      }.mkString("&")

    Try {
      assume(validProtocol(protocol), s"$protocol is not a valid protocol")
      assume(validHost(host), s"$host is not a valid host")
      assume(validParamMap(params), s"$params contains one or more invalid tokens")
      assume(validParam(queryParam), s"$queryParam is not a valid query parameter")
      ValidEndpoint(protocol + "://" + host, concatParams(params), queryParam)
    }

  }

}

case class ValidEndpoint(baseUrl: String, fixedParams: String, queryParam: String) {
  def buildQueryUrl(queryArg: String) = s"$baseUrl?$fixedParams&$queryParam=$queryArg"
}