package omdb.client.valdation

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
    queryParam: String): Option[ValidEndpoint] = {

    def asOption[A](predicate: A => Boolean): A => Option[A] = a =>
      if (predicate(a)) Some(a) else None

    val validProtocol = (protocol: String) => "^http(s?)$".r findFirstIn protocol
    val validHost = (host: String) => """(\w+\.){2,}\w+(:\d+)?/?$""".r findFirstIn host
    val paramPredicate = (param: String) => param.nonEmpty && param.forall(_.isLetterOrDigit)
    val paramMapPredicate = (paramMap: Map[String, String]) => paramMap forall {
      case (key, value) => paramPredicate(key) & paramPredicate(value)
    }
    val validParam = asOption(paramPredicate)
    val validParamMap = asOption(paramMapPredicate)

    def concatParams(pm: Map[String, String]): String =
      pm.map {
        case (key, value) => key + "=" + value
      }.mkString("&")

    for {
      pr <- validProtocol(protocol)
      hs <- validHost(host)
      ps <- validParamMap(params)
      qp <- validParam(queryParam)
    } yield ValidEndpoint(pr + "://" + hs, concatParams(params), qp)

  }

}

case class ValidEndpoint(baseUrl: String, fixedParams: String, queryParam: String) {
  def buildQueryUrl(queryArg: String) = s"$baseUrl?$fixedParams&$queryParam=$queryArg"
}