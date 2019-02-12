package utils

object ClientParamsSanitizer {
  def apply(param: String): String =
    param.replaceAll("[\'\"{}\\\\]", "")
}
