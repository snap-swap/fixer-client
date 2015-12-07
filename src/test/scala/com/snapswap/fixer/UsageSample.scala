package com.snapswap.fixer

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{Matchers, WordSpecLike}
import com.snapswap.fixer.error.FixerAPIError
import com.snapswap.fixer.model.FxData

class UsageSample
  extends WordSpecLike
    with ScalaFutures
    with Matchers {

  implicit override val patienceConfig = PatienceConfig(timeout = Span(5, Seconds), interval = Span(50, Millis))

  "FixerClient" should {
    "handle success response" in new setup {
      whenReady(fixerClient.latestRates("USD", Set("EUR", "GBP"))) { result =>
        result shouldBe a[FxData]
      }
    }
    "handle failure response" in new setup {
      whenReady(fixerClient.latestRates("UUUSSSDDD", Set("EUR", "GBP")).failed) { ex =>
        ex shouldBe a[FixerAPIError]
      }
    }
  }

  trait setup {
    private implicit val system = ActorSystem("UsageSample")
    private implicit val materializer = ActorMaterializer()
    val fixerClient = new FixerClientImpl()
  }

}