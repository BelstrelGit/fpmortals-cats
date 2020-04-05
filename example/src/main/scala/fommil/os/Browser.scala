// Copyright: 2017 - 2018 Sam Halliday, 2020 Zara Turtle
// License: https://firstdonoharm.dev/version/2/1/license.html

package fommil
package os

import prelude._

import java.awt.Desktop
import scala.sys.process._

import eu.timepit.refined.string.Url

/**
 * Does horrible things to try and open a URL in a browser.
 *
 * Caveat emptor. Dragons. Cats and dogs, living together. MASS HYSTERIA!
 */
object Browser {

  def open(url: String Refined Url): Task[Unit] =
    Task(Desktop.getDesktop().browse(new java.net.URI(url.value))).orElse(
      Task {
        // we could use BrowserLauncher2 for that true retro feel...
        if (str"xdg-open ${url.value}".! != 0)
          throw new java.lang.IllegalStateException("non-compliant browser") // scalafix:ok
      }
    )

}
