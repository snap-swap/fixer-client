package com.snapswap.fixer

import org.joda.time.DateTime
import org.scalatest.{Matchers, FlatSpec}
import spray.json._
import com.snapswap.fixer.error.FixerAPIError
import com.snapswap.fixer.model.FxData
import com.snapswap.fixer.FixerUnmarshaller._

class FixerUnmarshallerSpec extends FlatSpec with Matchers {

  "FixerUnmarshaller" should "parse a valid FxData" in {
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

  it should "parse error response" in {
    val str = """{"success":false,"error":{"code":201,"type":"invalid_base_currency"}}"""
    val json = str.parseJson

    intercept[FixerAPIError] {
      json.convertTo[FxData]
    }
  }
}
