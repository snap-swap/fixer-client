package vc.snapswap.fixer.error

import scala.util.control.NoStackTrace

case class RequestFailed(message: String) extends NoStackTrace {
  override def getMessage: String =
    message
}