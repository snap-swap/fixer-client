package com.snapswap.fixer

import org.joda.time.DateTime
import com.snapswap.fixer.error.FixerAPIError
import com.snapswap.fixer.model.FxData
import spray.json._
import com.snapswap.fixer.FixerUnmarshaller._
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