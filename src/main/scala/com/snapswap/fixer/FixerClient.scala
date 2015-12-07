package com.snapswap.fixer

import com.snapswap.fixer.model.FxData

import scala.concurrent.Future

/**
  * Created by greenhost on 07/12/15.
  */
trait FixerClient {
  def latestRates(base: String, counters: Set[String]): Future[FxData]
}
