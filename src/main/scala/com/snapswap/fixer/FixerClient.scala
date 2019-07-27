package com.snapswap.fixer

import com.snapswap.fixer.model.FxData
import org.joda.time.LocalDate

import scala.concurrent.Future

trait FixerClient {
  def latestRates(base: String, counters: Set[String]): Future[FxData]

  def latestRatesEUR(): Future[FxData]

  def ratesAsOf(date: LocalDate, base: String, counters: Set[String]): Future[FxData]

  def ratesAsOfEUR(date: LocalDate): Future[FxData]
}
