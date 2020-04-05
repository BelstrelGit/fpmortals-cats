// Copyright: 2017 - 2018 Sam Halliday, 2020 Zara Turtle
// License: https://firstdonoharm.dev/version/2/1/license.html

package fommil
package http.encoding

import prelude._, Z._

import java.net.URI
import eu.timepit.refined.string.Url

/**
 * URL query key=value pairs, in unencoded form.
 */
@xderiving(UrlEncodedWriter)
final case class UrlQuery(params: IList[(String, String)]) extends AnyVal
object UrlQuery {
  object ops {
    implicit class UrlOps(private val encoded: String Refined Url) {
      def withQuery(query: UrlQuery): String Refined Url = {
        val uri = new URI(encoded.value)
        val update = new URI(
          uri.getScheme,
          uri.getUserInfo,
          uri.getHost,
          uri.getPort,
          uri.getPath,
          // not a mistake: URI takes the decoded versions
          query.params.map { case (k, v) => k + "=" + v }.intercalate("&"),
          uri.getFragment
        )
        Refined.unsafeApply(update.toASCIIString)
      }
    }
  }

}
