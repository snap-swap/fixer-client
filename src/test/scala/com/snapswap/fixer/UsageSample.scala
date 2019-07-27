package com.snapswap.fixer

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.snapswap.fixer.error.FixerAPIError
import com.snapswap.fixer.model.FxData
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.scalatest.{AsyncFlatSpec, Matchers}

import scala.concurrent.duration._

class UsageSample extends AsyncFlatSpec with Matchers {

  val fixerAccessKey = "fd76c5aed04f05f3de8efa0060351316"

  implicit val timeout: Timeout = 1.minute

  "FixerClient" should "handle success response" in {
    import setup._
    //    fixerClient.latestRates("USD", Set("EUR", "GBP"))
    fixerClient.latestRatesEUR()
      .map { result =>
        println("=" * 100)
        println(result)
        println("=" * 100)
        result shouldBe a[FxData]
      }
  }
  it should "handle failure response" in {
    import setup._
    recoverToSucceededIf[FixerAPIError] {
      fixerClient.latestRates("UUUSSSDDD", Set("EUR", "GBP"))
    }
  }
  it should "get rates as of EOD of a specific date" in {
    import setup._
    val dtf = DateTimeFormat.forPattern("yyyy-MM-dd")
    val pastDate = dtf.parseLocalDate("2018-01-31") //LocalDate.now(DateTimeZone.UTC).minusWeeks(1)
    //    fixerClient.ratesAsOf(pastDate, "GBP", Set("EUR"))
    fixerClient.ratesAsOfEUR(pastDate)
      .map { result =>
        result shouldBe a[FxData]
        println("=" * 100)
        println(result)
        println("=" * 100)
        result.asOf shouldBe pastDate.toDateTimeAtStartOfDay(DateTimeZone.UTC)
      }
  }

  object setup {
    private implicit val system = ActorSystem("UsageSample")
    private implicit val materializer = ActorMaterializer()
    val fixerClient = new FixerClientImpl(fixerAccessKey)
  }

}
