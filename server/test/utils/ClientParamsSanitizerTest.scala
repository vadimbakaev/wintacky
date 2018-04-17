package utils

import org.specs2._
import org.specs2.matcher.MatchResult
import org.specs2.specification.core.SpecStructure

class ClientParamsSanitizerTest extends Specification {
  def is: SpecStructure = s2"""

  This is a specification for the ClientParamsSanitizer

  The apply should
    remove { character                              $e1
    remove } character                              $e2
    remove ' character                              $e3
    remove \" character                             $e4
    """

  def e1: MatchResult[String] = ClientParamsSanitizer("{Hell{o world") must_=== "Hello world"
  def e2: MatchResult[String] = ClientParamsSanitizer("Hell}}o world") must_=== "Hello world"
  def e3: MatchResult[String] = ClientParamsSanitizer("'Hello' world") must_=== "Hello world"
  def e4: MatchResult[String] = ClientParamsSanitizer("Hell\"o world") must_=== "Hello world"

}
