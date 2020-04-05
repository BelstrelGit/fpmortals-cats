// Copyright: 2017 - 2018 Sam Halliday, 2020 Zara Turtle
// License: https://firstdonoharm.dev/version/2/1/license.html

package fommil
package http
package oauth2

import prelude._, Z._

import pureconfig.orphans._
import eu.timepit.refined.string.Url

import api._
import encoding.UrlQuery.ops._
import encoding.UrlQueryWriter.ops._

/** Defines fixed information about a server's OAuth 2.0 service. */
@deriving(ConfigReader)
final case class ServerConfig(
  auth: String Refined Url,
  access: String Refined Url,
  refresh: String Refined Url,
  scope: String,
  clientId: String,
  clientSecret: String
)

/** Code tokens are one-shot and expire on use. */
@deriving(Equal, Show)
final case class CodeToken(
  token: String,
  // for some stupid reason, the protocol needs the exact same
  // redirect_uri in subsequent calls
  redirect_uri: String Refined Url
)

/** The beginning of an OAuth2 setup */
trait Auth[F[_]] {
  def authenticate: F[CodeToken]
}

final class AuthModule[F[_]: Monad](
  config: ServerConfig
)(
  I: UserInteraction[F]
) extends Auth[F] {
  def authenticate: F[CodeToken] =
    for {
      callback <- I.start
      params   = AuthRequest(callback, config.scope, config.clientId)
      _        <- I.open(config.auth.withQuery(params.toUrlQuery))
      code     <- I.stop
    } yield code
}

/**
 * Algebra for the part of OAuth2 to obtain a `CodeToken` through user
 * interaction.
 */
trait UserInteraction[F[_]] {

  /** returns the URL of the local server */
  def start: F[String Refined Url]

  /** prompts the user to open this URL */
  def open(uri: String Refined Url): F[Unit]

  /** recover the code from the callback */
  def stop: F[CodeToken]
}
