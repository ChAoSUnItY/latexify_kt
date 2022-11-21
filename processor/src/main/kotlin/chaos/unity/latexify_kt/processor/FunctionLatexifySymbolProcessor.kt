package chaos.unity.latexify_kt.processor

import chaos.unity.latexify_kt.annotation.Function
import chaos.unity.latexify_kt.parser.FunctionParser
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid

class FunctionLatexifySymbolProcessor(private val logger: KSPLogger) : SymbolProcessor {
    val functions: MutableList<KSFunctionDeclaration> = mutableListOf()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val functions = resolver.getSymbolsWithAnnotation(Function::class.qualifiedName!!)
        functions.forEach { it.accept(FunctionLatexifyVisitor(), Unit) }
        FunctionParser(logger, this.functions).parse()

        return emptyList()
    }

    inner class FunctionLatexifyVisitor : KSVisitorVoid() {
        override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
            logger.warn("Retrieved function with @Function ${function.simpleName.asString()}", function)
            logger.warn("Containing file ${function.containingFile!!.filePath}", function.containingFile)

            functions += function
        }
    }
}