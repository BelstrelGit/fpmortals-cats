// Copyright: 2017 - 2018 Sam Halliday, 2020 Zara Turtle
// License: https://firstdonoharm.dev/version/2/1/license.html

package fpmortals
package http.encoding

import prelude._, Z._

import eu.timepit.refined.auto._
import eu.timepit.refined.string.Url

class UrlQuerySpec extends Test {
  import UrlQuery.ops._

  "UrlQuery".should("allow changing the query").in {
    val url: String Refined Url = "http://fommil.com?wibble=wobble"

    url
      .withQuery(
        UrlQuery(
          ("blah"    -> "bloo") ::
            (" meh " -> "#") ::
            IList.empty
        )
      )
      .value
      .shouldBe("http://fommil.com?blah=bloo&%20meh%20=%23")
  }

}
