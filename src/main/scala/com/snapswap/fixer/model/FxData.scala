package com.snapswap.fixer.model

import org.joda.time.DateTime

case class FxData(asOf: DateTime, base: String, rates: Map[String, BigDecimal]) {
  override def toString: String = {
    if (rates.isEmpty) {
      s"empty FX rates as of [$asOf]"
    } else {
      s"FX rates as of [$asOf]: " + rates.map { case (counter, value) => s"$base$counter = $value" }.mkString(", ")
    }
  }

  def rate(base: String, counter: String): Option[BigDecimal] = {
    if (base == counter) {
      Some(1)
    } else if (rates.isEmpty) {
      None
    } else if (base == this.base) {
      rates.get(counter)
    } else if (counter == this.base) {
      rates.get(base).map(1.0 / _)
    } else {
      (rates.get(base), rates.get(counter)) match {
        case (Some(b), Some(c)) => Some(c / b)
        case _ => None
      }
    }
  }

  def currencies: Set[String] = {
    if (rates.isEmpty) {
      Set.empty[String]
    } else {
      rates.keySet + base
    }
  }
}
