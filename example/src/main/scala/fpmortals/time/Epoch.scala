// Copyright: 2017 - 2018 Sam Halliday, 2020 Zara Turtle
// License: https://firstdonoharm.dev/version/2/1/license.html

package fpmortals

import cats._, implicits._
import cats.effect.IO

import java.time.Instant
import java.lang.System

import scala.{ Either, Left, Right, StringContext }
import scala.concurrent.duration._
import scala.util.control.NonFatal

import contextual._

package object time {
  implicit class EpochMillisStringContext(sc: StringContext) {
    val epoch: Prefix[Epoch, Context, EpochInterpolator.type] =
      Prefix(EpochInterpolator, sc)
  }
}

package time {
  final case class Epoch(millis: Long) extends AnyVal {
    def +(d: FiniteDuration): Epoch = Epoch(millis + d.toMillis)
    def -(e: FiniteDuration): Epoch = Epoch(millis - e.toMillis)
    def -(e: Epoch): FiniteDuration = (millis - e.millis).millis
  }
  object Epoch {
    def now: IO[Epoch] =
      IO(Epoch(System.currentTimeMillis))

    implicit val show: Show[Epoch] = e => Instant.ofEpochMilli(e.millis).toString
  }

  object EpochInterpolator extends Verifier[Epoch] {
    def check(s: String): Either[(Int, String), Epoch] =
      try Right(Epoch(Instant.parse(s).toEpochMilli))
      catch { case NonFatal(_) => Left((0, "not in ISO-8601 format")) }
  }
}
