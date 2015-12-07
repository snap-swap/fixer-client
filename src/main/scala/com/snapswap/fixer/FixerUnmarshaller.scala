package com.snapswap.fixer

import org.joda.time.format.DateTimeFormat
import spray.json.{DefaultJsonProtocol, JsObject, JsValue, JsonReader}
import com.snapswap.fixer.error.{FixerAPIError, UnexpectedResponse}
import com.snapswap.fixer.model.FxData

import scala.util.{Failure, Success, Try}

/**
  * Created by greenhost on 07/12/15.
  */
private[fixer] object FixerUnmarshaller extends DefaultJsonProtocol {

  case class FixerErrorRaw(error: String)

  case class FixerResponseRaw(base: String, date: String, rates: Map[String, BigDecimal])

  private val df = DateTimeFormat.forPattern("yyyy-MM-dd").withZoneUTC()

  implicit val errorRawFormat = jsonFormat1(FixerErrorRaw.apply)
  implicit val responseRawFormat = jsonFormat3(FixerResponseRaw.apply)

  implicit val responseFormat = new JsonReader[FxData] {
    override def read(json: JsValue): FxData = json match {
      case obj: JsObject if obj.fields.contains("error") =>
        Try(obj.convertTo[FixerErrorRaw]) match {
          case Failure(ex) =>
            throw UnexpectedResponse(s"Reason: '${ex.getMessage}'. JSON: ${json.compactPrint}")
          case Success(err) =>
            throw FixerAPIError(err.error)
        }
      case _ =>
        Try(json.convertTo[FixerResponseRaw]) match {
          case Failure(ex) =>
            throw UnexpectedResponse(s"Reason: '${ex.getMessage}'. JSON: ${json.compactPrint}")
          case Success(response) =>
            Try(df.parseDateTime(response.date)) match {
              case Failure(ex) =>
                throw UnexpectedResponse(s"Unexpected date format: '${ex.getMessage}'. JSON: ${json.compactPrint}")
              case Success(date) =>
                FxData(
                  asOf = date,
                  base = response.base,
                  rates = response.rates
                )
            }
        }
    }
  }
}
