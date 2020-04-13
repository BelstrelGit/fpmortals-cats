// Copyright: 2017 - 2018 Sam Halliday, 2020 Zara Turtle
// License: https://firstdonoharm.dev/version/2/1/license.html

package fpmortals
package http

import prelude._, Z._

import jsonformat.JsDecoder
import eu.timepit.refined.string.Url

import http.encoding._

private[http] abstract class JsonClientBoilerplate {
  this: JsonClient.type =>

  def liftM[F[_]: Monad, G[_[_], _]: MonadTrans](
    f: JsonClient[F]
  ): JsonClient[G[F, ?]] =
    new JsonClient[G[F, ?]] {
      def get[A: JsDecoder](
        uri: String Refined Url,
        headers: IList[(String, String)]
      ): G[F, A] = f.get(uri, headers).liftM[G]

      def post[P: UrlEncodedWriter, A: JsDecoder](
        uri: String Refined Url,
        payload: P,
        headers: IList[(String, String)]
      ): G[F, A] = f.post(uri, payload, headers).liftM[G]
    }

}
