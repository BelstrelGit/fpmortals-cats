// Copyright: 2017 - 2018 Sam Halliday, 2020 Zara Turtle
// License: https://firstdonoharm.dev/version/2/1/license.html

package pureconfig

import java.lang.{ IllegalArgumentException, String }
import scala.{ AnyVal, Either, Left, None, Right }

import scalaz.ioeffect.Task

import eu.timepit.refined.refineV
import eu.timepit.refined.api._
import org.http4s.client.blaze.BlazeClientConfig

import pureconfig.error._

object orphans {
  // superior alternative to loadConfig, deferring I/O, and avoiding the
  // Derivation indirection.
  def readConfig[A: ConfigReader]: Task[A] = readConfig("")
  def readConfig[A: ConfigReader](path: String): Task[A] =
    Task(loadConfig(path)(Derivation.Successful(ConfigReader[A]))).flatMap {
      case Left(e) =>
        Task.fail(new IllegalArgumentException(e.toString)) // scalafix:ok
      case Right(a) => Task.now(a)
    }

  // I am unconvinced about the utility of pureconfig's error "ADT": it isn't
  // sealed and it is not easy to create entries.
  def configReaderFailures[A](
    msg: String
  ): Either[ConfigReaderFailures, A] =
    Left(
      ConfigReaderFailures(
        new ConfigReaderFailure {
          def description: String = msg
          def location: None.type = None
        }
      )
    )

  def failureReason[A](msg: String): Either[FailureReason, A] =
    Left(
      new FailureReason {
        def description: String = msg
      }
    )

  implicit val configReaderBlazeClientConfig: ConfigReader[BlazeClientConfig] =
    _ => configReaderFailures("BlazeClientConfig")

  implicit def configReaderRefined[A: ConfigReader, P](
    implicit V: Validate[A, P]
  ): ConfigReader[A Refined P] =
    ConfigReader[A].emap(
      refineV[P](_) match {
        case Right(success) => Right(success)
        case Left(err)      => failureReason(err)
      }
    )

  final implicit class ConfigReaderXMap[A](private val self: ConfigReader[A])
      extends AnyVal {
    final def xmap[B](f: A => B, g: B => A): ConfigReader[B] = {
      val _ = g
      self.map(f)
    }
  }
}
