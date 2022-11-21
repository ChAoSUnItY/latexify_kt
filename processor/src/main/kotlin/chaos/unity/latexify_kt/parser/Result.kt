package chaos.unity.latexify_kt.parser

import java.lang.StringBuilder

sealed class Result private constructor(open val builder: StringBuilder?) {
    data class Success(override val builder: StringBuilder) : Result(builder)
    object Failure : Result(null)
}