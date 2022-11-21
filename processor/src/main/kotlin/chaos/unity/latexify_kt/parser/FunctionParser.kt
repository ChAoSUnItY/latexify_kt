package chaos.unity.latexify_kt.parser

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
            is Node.Decl.Func.Body.Expr ->  parseExpression(builder, body.expr)
        }
    }

    private fun parseExpression(builder: StringBuilder, expr: Node.Expr): Result {
        return when (expr) {
            is Node.Expr.If -> TODO()
            is Node.Expr.Try -> TODO()
            is Node.Expr.For -> TODO()
            is Node.Expr.While -> TODO()
            is Node.Expr.BinaryOp -> TODO()
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
            is Node.Expr.Call -> {
                val name = expr.expr as Node.Expr.Name

                when (name.name) {
                    "sin", "cos", "tan", "cot", "sec", "csc" -> {
                        builder.append("\\${name.name}")

                        if (expr.args.size != 1) {
                            logger.error("Illegal equation form, wave function ${name.name} takes 1 argument")
                            return Result.Failure
                        }

                        builder.append("({")

                        val result = parseExpression(builder, expr.args.first().expr)
                        if (result is Result.Failure) return result

                        builder.append("})")
                    }
                }

                return Result.Success(builder)
            }
            is Node.Expr.ArrayAccess -> TODO()
            is Node.Expr.AnonFunc -> TODO()
            is Node.Expr.Property -> TODO()
        }
    }
}