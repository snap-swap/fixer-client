package vc.snapswap.fixer

import vc.snapswap.fixer.model.FxData

import scala.concurrent.Future

trait FixerClient {
  def latestRates(base: String, counters: Set[String]): Future[FxData]
}