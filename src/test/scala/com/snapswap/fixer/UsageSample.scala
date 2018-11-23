package com.snapswap.fixer

import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.util.Timeout
import org.scalatest.{Matchers, AsyncFlatSpec}
import org.joda.time.{LocalDate, DateTimeZone}
import com.snapswap.fixer.error.FixerAPIError
import com.snapswap.fixer.model.FxData

class UsageSample extends AsyncFlatSpec with Matchers {

  val fixerAccessKey = "REPLACE WITH YOUR KEY"

  implicit val timeout: Timeout = 1.minute

  "FixerClient" should "handle success response" in {
    import setup._
    fixerClient.latestRates("USD", Set("EUR", "GBP")) map { result =>
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
    val pastDate = LocalDate.now(DateTimeZone.UTC).minusWeeks(1)
    fixerClient.ratesAsOf(pastDate, "GBP", Set("EUR")) map { result =>
      result shouldBe a[FxData]
      print(result)
      result.asOf shouldBe pastDate.toDateTimeAtStartOfDay(DateTimeZone.UTC)
    }
  }

  object setup {
    private implicit val system = ActorSystem("UsageSample")
    private implicit val materializer = ActorMaterializer()
    val fixerClient = new FixerClientImpl(fixerAccessKey)
  }
}
