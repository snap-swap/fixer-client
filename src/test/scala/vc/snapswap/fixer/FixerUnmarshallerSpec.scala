package vc.snapswap.fixer

import org.joda.time.DateTime
import vc.snapswap.fixer.error.FixerAPIError
import vc.snapswap.fixer.model.FxData
import spray.json._
import vc.snapswap.fixer.FixerUnmarshaller._
import org.scalatest.{Matchers, WordSpecLike}
import org.scalatest.concurrent.ScalaFutures

class FixerUnmarshallerSpec
  extends WordSpecLike
    with ScalaFutures
    with Matchers {

  "FixerUnmarshaller" should {
    "parse a valid FxData" in {
      val str = """{"base":"USD","date":"2015-07-20","rates":{"GBP":0.5,"EUR":0.8}}"""
      val result = str.parseJson.convertTo[FxData]

      result shouldBe FxData(
        asOf = DateTime.parse("2015-07-20T00:00:00.000Z"),
        base = "USD",
        Map(
          "GBP" -> BigDecimal("0.5"),
          "EUR" -> BigDecimal("0.8")
        )
      )
      result.currencies shouldBe Set("USD", "GBP", "EUR")
      result.all shouldBe Map(
        ("USD", "EUR") -> 0.8,
        ("USD", "GBP") -> 0.5,
        ("EUR", "USD") -> 1.25,
        ("GBP", "USD") -> 2,
        ("GBP", "EUR") -> 1.6,
        ("EUR", "GBP") -> 0.625
      )
    }
    "parse error response" in {
      val str = """{"error":"Invalid base"}"""
      val json = str.parseJson

      intercept[FixerAPIError] {
        json.convertTo[FxData]
      }
    }
  }
}