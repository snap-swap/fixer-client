package com.snapswap.fixer

import scala.concurrent.Future
import org.joda.time.LocalDate
import com.snapswap.fixer.model.FxData

trait FixerClient {
  def latestRates(base: String, counters: Set[String]): Future[FxData]
  def ratesAsOf(date: LocalDate, base: String, counters: Set[String]): Future[FxData]
}
