package com.snapswap.fixer.error

import scala.util.control.NoStackTrace

case class FixerAPIError(message: String) extends NoStackTrace {
  override def getMessage: String =
    message
}