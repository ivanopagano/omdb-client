package omdb.metrics

import scala.compat.Platform.currentTime
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

/**
  * Created by Ivano Pagano on 09/05/16.
  * == Obiettivo 5. ==
  * Tracciare delle metriche di base: tempi di risposta
  */
object Metrics {

  /**
    * measure execution time of a given function
    */
  def measuringExecution[A, B](op: A => B, opName: String = "Operation"): A => B =
    a => {
      val start = currentTime
      val b = op(a)
      val lapse = currentTime - start
      logTime(opName, lapse.milliseconds)
      b
    }

  /**
    * measure execution time of a given asynchronous function
    */
  def measuringAsyncExecution[A, B, C](op: A => C, opName: String = "Operation")
    (implicit evidence: C <:< Future[B], ec: ExecutionContext): A => Future[B] =
    a => {
      val start = currentTime
      val c = op(a)
      c onComplete { _ =>
        val lapse = currentTime - start
        logTime(opName, lapse.milliseconds)
      }
      c
    }

  def logTime(operation: String, lapse: FiniteDuration): Unit =
    println(s"Recorded time for call to $operation is ${lapse.toMillis} milliseconds")
}
