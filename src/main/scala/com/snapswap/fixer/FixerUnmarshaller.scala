package com.snapswap.fixer

import scala.util.{Failure, Success, Try}
import org.joda.time.format.DateTimeFormat
import spray.json.{DefaultJsonProtocol, JsObject, JsValue, JsonReader}
import com.snapswap.fixer.error.{FixerAPIError, UnexpectedResponse}
import com.snapswap.fixer.model.FxData

private[fixer] object FixerUnmarshaller extends DefaultJsonProtocol {

  case class FixerErrorRaw(code: Int, `type`: String)

  case class FixerResponseRaw(base: String, date: String, rates: Map[String, BigDecimal])

  private val df = DateTimeFormat.forPattern("yyyy-MM-dd").withZoneUTC()

  implicit val errorRawFormat = jsonFormat2(FixerErrorRaw.apply)
  implicit val responseRawFormat = jsonFormat3(FixerResponseRaw.apply)

  implicit val responseFormat = new JsonReader[FxData] {
    override def read(json: JsValue): FxData = json match {
      case obj: JsObject if obj.fields.contains("error") =>
        Try(obj.fields("error").convertTo[FixerErrorRaw]) match {
          case Failure(ex) =>
            throw UnexpectedResponse(s"Reason: '${ex.getMessage}'. JSON: ${json.compactPrint}")
          case Success(err) =>
            throw FixerAPIError(s"[${err.code}] ${err.`type`}")
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
