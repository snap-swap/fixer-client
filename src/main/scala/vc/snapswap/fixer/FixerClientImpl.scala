package vc.snapswap.fixer

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding._
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source, Flow}
import org.joda.time.{DateTimeZone, DateTime}
import spray.json.JsValue
import vc.snapswap.fixer.error.{UnexpectedResponse, RequestFailed}
import spray.json._
import vc.snapswap.fixer.FixerUnmarshaller._
import vc.snapswap.fixer.model.FxData
import scala.concurrent.Future

class FixerClientImpl()(implicit val system: ActorSystem, val materializer: Materializer) extends FixerClient {

  import system.dispatcher

  private val log = Logging(system, this.getClass)

  protected val baseURL = "/"

  private lazy val fixerConnectionFlow: Flow[HttpRequest, HttpResponse, Any] =
    Http().outgoingConnection("api.fixer.io", 80).log("fixer")

  override def latestRates(base: String, counters: Set[String]): Future[FxData] = {
    if (counters.isEmpty) {
      Future.successful(
        FxData(
          base = base,
          asOf = DateTime.now(DateTimeZone.UTC),
          rates = Map()
        ))
    } else {
      import vc.snapswap.fixer.FixerUnmarshaller._

      get("latest", Map("base" -> base, "symbols" -> counters.mkString(","))) { json =>
        if (json.convertTo[FxData].currencies == counters + base) {
          json.convertTo[FxData]
        } else {
          throw UnexpectedResponse("Unexpected currencies in response")
        }
      }
    }
  }

  private def makeRequest(request: HttpRequest): Future[HttpResponse] =
    Source.single(request).via(fixerConnectionFlow).runWith(Sink.head)

  private def get[T](path: String, query: Map[String, String])(parser: JsValue => T): Future[T] = {
    val url = baseURL + path + parameters(query)
    val request = Get(url)

    makeRequest(
      request
    ).flatMap { response =>
      Unmarshal(response.entity)
        .to[String]
        .map { asString =>
          log.debug(s"GET [$url] -> [${response.status}] [$asString]")
          asString.parseJson
        }
    }.map {
      parser
    }.recover {
      case e: Exception =>
        log.error(e, s"GET [$url] failed with [${e.getMessage}]")
        throw RequestFailed(e.getMessage)
    }
  }

  private def parameters[T](query: Map[String, String]): String = {
    if (query.isEmpty) {
      ""
    } else {
      "?" + query.map(tup => s"${tup._1}=${tup._2}").mkString("&")
    }
  }
}