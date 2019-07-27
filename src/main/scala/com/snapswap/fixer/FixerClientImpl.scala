package com.snapswap.fixer

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding._
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.settings.ConnectionPoolSettings
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import com.snapswap.fixer.error.{RequestFailed, UnexpectedResponse}
import com.snapswap.fixer.model.FxData
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.{DateTime, DateTimeZone, LocalDate}
import spray.json._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class FixerClientImpl(accessKey: String,
                      apiHost: String = "data.fixer.io",
                      apiBase: String = "/api")
                     (implicit val system: ActorSystem,
                      val materializer: Materializer)
  extends FixerClient {

  import system.dispatcher

  private val log = Logging(system, this.getClass)
  private val `yyyy-MM-dd` = ISODateTimeFormat.yearMonthDay()

  private lazy val fixerConnectionFlow =
    Http()
      .cachedHostConnectionPool[Unit](apiHost, settings = ConnectionPoolSettings(system.settings.config))
      .log("fixer")

  override def latestRatesEUR(): Future[FxData] = {
    import com.snapswap.fixer.FixerUnmarshaller._

    get("latest", Map.empty) { json =>
      json.convertTo[FxData]
    }
  }

  override def latestRates(base: String, counters: Set[String]): Future[FxData] = {
    if (counters.isEmpty) {
      Future.successful(
        FxData(
          base = base,
          asOf = DateTime.now(DateTimeZone.UTC),
          rates = Map()
        ))
    } else {
      import com.snapswap.fixer.FixerUnmarshaller._

      get("latest", Map("base" -> base, "symbols" -> counters.mkString(","))) { json =>
        if (json.convertTo[FxData].currencies == counters + base) {
          json.convertTo[FxData]
        } else {
          throw UnexpectedResponse("Unexpected currencies in response")
        }
      }
    }
  }

  override def ratesAsOfEUR(date: LocalDate): Future[FxData] = {
    import com.snapswap.fixer.FixerUnmarshaller._

    get(`yyyy-MM-dd`.print(date), Map.empty) { json =>
      json.convertTo[FxData]
    }
  }

  override def ratesAsOf(date: LocalDate, base: String, counters: Set[String]): Future[FxData] = {
    if (counters.isEmpty) {
      Future.successful(
        FxData(
          base = base,
          asOf = date.toDateTimeAtStartOfDay(DateTimeZone.UTC),
          rates = Map()
        ))
    } else {
      import com.snapswap.fixer.FixerUnmarshaller._
      get(`yyyy-MM-dd`.print(date), Map("base" -> base, "symbols" -> counters.mkString(","))) { json =>
        if (json.convertTo[FxData].currencies == counters + base) {
          json.convertTo[FxData]
        } else {
          throw UnexpectedResponse("Unexpected currencies in response")
        }
      }
    }
  }

  private def makeRequest(request: HttpRequest): Future[HttpResponse] =
    Source
      .single(request -> (()))
      .via(fixerConnectionFlow).mapAsync(1) {
      case (Success(resp), _) => resp.toStrict(1.second)
      case (Failure(ex), _) => Future.failed[HttpResponse](ex)
    }.runWith(Sink.head)

  private def get[T](path: String, query: Map[String, String])(parser: JsValue => T): Future[T] = {
    val url = (if (apiBase.endsWith("/")) apiBase else apiBase + "/") + path + parameters(query)
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
    s"?access_key=$accessKey&" + query.map(tup => s"${tup._1}=${tup._2}").mkString("&")
  }
}
