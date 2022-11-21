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

            logger.warn("Functions: ${filteredFunctions.map(::ppDataClass)}", file)
        }
    }
}