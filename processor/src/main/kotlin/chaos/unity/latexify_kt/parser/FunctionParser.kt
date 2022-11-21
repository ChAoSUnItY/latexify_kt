package chaos.unity.latexify_kt.parser

import chaos.unity.latexify_kt.`as`
import chaos.unity.latexify_kt.ppDataClass
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import kastree.ast.Node
import kastree.ast.psi.Parser
import java.io.File

class FunctionParser(private val logger: KSPLogger, functions: List<KSFunctionDeclaration>) {
    private val files: Map<KSFile?, List<KSFunctionDeclaration>> =
        functions.groupBy(KSFunctionDeclaration::containingFile)

    fun parse() {
        for ((file, functionDeclarations) in files) {
            if (file == null) continue

            val content = File(file.filePath).readText().replace("\r\n", "\n")
            val ast = Parser.parseFile(content)
            val filteredFunctions = ast.decls
                .filterIsInstance<Node.Decl.Func>()
                .filter { functionDeclarations.any { func -> func.simpleName.asString() == it.name } }
                .map { ppDataClass(it) to latexify(it) }

            logger.warn("Functions: $filteredFunctions", file)
        }
    }

    private fun latexify(func: Node.Decl.Func): String {
        val builder = StringBuilder("$")
        val result = parseFunctionDeclaration(builder, func)
        return if (result is Result.Success) {
            result.builder.append('$').toString()
        } else {
            ""
        }
    }

    private fun parseFunctionDeclaration(builder: StringBuilder, func: Node.Decl.Func): Result {
        val body = func.body

        if (func.name != null) {
            builder.append(func.name)
        } else {
            logger.error("Illegal equation form, missing function name from declaration")
            return Result.Failure
        }

        builder.append('(')
        func.params.joinToString(transform = Node.Decl.Func.Param::name)
            .let(builder::append)
        builder.append(')')

        return if (body != null) {
            builder.append('=')

            parseFunctionBody(builder, body)
        } else {
            Result.Success(builder)
        }
    }

    private fun parseFunctionBody(builder: StringBuilder, body: Node.Decl.Func.Body): Result {
        return when (body) {
            is Node.Decl.Func.Body.Block -> {
                val lastStatement = body.block.stmts.lastOrNull()

                if (lastStatement == null) {
                    logger.error("Illegal equation form, @Function requires at least 1 expression or statement")
                    return Result.Failure
                }

                TODO()
            }

            is Node.Decl.Func.Body.Expr -> parseExpression(builder, body.expr)
        }
    }

    private fun parseExpression(builder: StringBuilder, expr: Node.Expr): Result {
        return when (expr) {
            is Node.Expr.If -> TODO()
            is Node.Expr.Try -> TODO()
            is Node.Expr.For -> TODO()
            is Node.Expr.While -> TODO()
            is Node.Expr.BinaryOp -> {
                val (lhs, op, rhs) = expr

                when (op) {
                    is Node.Expr.BinaryOp.Oper.Infix -> {
                        logger.error("Illegal equation form, binary expression must explicitly have operators")
                        Result.Failure
                    }

                    is Node.Expr.BinaryOp.Oper.Token -> {
                        val token = op.token

                        when (token) {
                            Node.Expr.BinaryOp.Token.MUL -> {
                                var result = parseExpression(builder, expr.lhs)
                                if (result is Result.Failure) return result

                                builder.append("\\cdot")

                                result = parseExpression(builder, expr.rhs)
                                if (result is Result.Failure) return result

                                result
                            }
                            Node.Expr.BinaryOp.Token.DIV -> TODO()
                            Node.Expr.BinaryOp.Token.MOD -> TODO()
                            Node.Expr.BinaryOp.Token.ADD -> TODO()
                            Node.Expr.BinaryOp.Token.SUB -> TODO()
                            Node.Expr.BinaryOp.Token.IN -> TODO()
                            Node.Expr.BinaryOp.Token.NOT_IN -> TODO()
                            Node.Expr.BinaryOp.Token.GT -> TODO()
                            Node.Expr.BinaryOp.Token.GTE -> TODO()
                            Node.Expr.BinaryOp.Token.LT -> TODO()
                            Node.Expr.BinaryOp.Token.LTE -> TODO()
                            Node.Expr.BinaryOp.Token.EQ -> TODO()
                            Node.Expr.BinaryOp.Token.NEQ -> TODO()
                            Node.Expr.BinaryOp.Token.ASSN -> TODO()
                            Node.Expr.BinaryOp.Token.MUL_ASSN -> TODO()
                            Node.Expr.BinaryOp.Token.DIV_ASSN -> TODO()
                            Node.Expr.BinaryOp.Token.MOD_ASSN -> TODO()
                            Node.Expr.BinaryOp.Token.ADD_ASSN -> TODO()
                            Node.Expr.BinaryOp.Token.SUB_ASSN -> TODO()
                            Node.Expr.BinaryOp.Token.OR -> TODO()
                            Node.Expr.BinaryOp.Token.AND -> TODO()
                            Node.Expr.BinaryOp.Token.ELVIS -> TODO()
                            Node.Expr.BinaryOp.Token.RANGE -> TODO()
                            Node.Expr.BinaryOp.Token.DOT -> {
                                // Dot have different meanings on certain context
                                // For example: sin(1.0).pow(2)
                                var result: Result

                                // Special case:
                                // If lhs is call, and rhs is call `pow`,
                                // we put pow in the middle of lhs call's name and args
                                if (lhs is Node.Expr.Call && rhs is Node.Expr.Call && rhs.expr.`as`<Node.Expr.Name>().name == "pow") {
                                    result = parseCall(builder, lhs, rhs)
                                    if (result is Result.Failure) return result
                                } else {
                                    result = parseExpression(builder, expr.lhs)
                                    if (result is Result.Failure) return result
                                    result = parseExpression(builder, expr.rhs)
                                    if (result is Result.Failure) return result
                                }

                                Result.Success(builder)
                            }

                            Node.Expr.BinaryOp.Token.DOT_SAFE -> TODO()
                            Node.Expr.BinaryOp.Token.SAFE -> TODO()
                        }
                    }
                }
            }

            is Node.Expr.UnaryOp -> TODO()
            is Node.Expr.TypeOp -> TODO()
            is Node.Expr.DoubleColonRef.Callable -> TODO()
            is Node.Expr.DoubleColonRef.Class -> TODO()
            is Node.Expr.Paren -> TODO()
            is Node.Expr.StringTmpl -> TODO()
            is Node.Expr.Const -> {
                builder.append(expr.value)
                return Result.Success(builder)
            }

            is Node.Expr.Brace -> TODO()
            is Node.Expr.Brace.Param -> TODO()
            is Node.Expr.This -> TODO()
            is Node.Expr.Super -> TODO()
            is Node.Expr.When -> TODO()
            is Node.Expr.Object -> TODO()
            is Node.Expr.Throw -> TODO()
            is Node.Expr.Return -> TODO()
            is Node.Expr.Continue -> TODO()
            is Node.Expr.Break -> TODO()
            is Node.Expr.CollLit -> TODO()
            is Node.Expr.Name -> TODO()
            is Node.Expr.Labeled -> TODO()
            is Node.Expr.Annotated -> TODO()
            is Node.Expr.Call -> parseCall(builder, expr)
            is Node.Expr.ArrayAccess -> TODO()
            is Node.Expr.AnonFunc -> TODO()
            is Node.Expr.Property -> TODO()
        }
    }

    private fun parseCall(builder: StringBuilder, call: Node.Expr.Call, midExpr: Node.Expr? = null): Result {
        val name = call.expr as Node.Expr.Name
        var result: Result

        when (name.name) {
            "sin", "cos", "tan", "cot", "sec", "csc" -> {
                builder.append("\\${name.name}")

                if (call.args.size != 1) {
                    logger.error("Illegal equation form, wave function ${name.name} takes 1 argument")
                    return Result.Failure
                }

                if (midExpr != null) {
                    result = parseExpression(builder, midExpr)
                    if (result is Result.Failure) return result
                }

                builder.append("({")

                result = parseExpression(builder, call.args.first().expr)
                if (result is Result.Failure) return result

                builder.append("})")
            }

            "pow" -> {
                // pow in kotlin is an extension function
                if (call.args.size != 1) {
                    logger.error("Illegal equation form, power function ${name.name} takes 1 argument")
                    return Result.Failure
                }

                builder.append('^')
                result = parseExpression(builder, call.args.first().expr)
                if (result is Result.Failure) return result
            }
        }

        return Result.Success(builder)
    }
}