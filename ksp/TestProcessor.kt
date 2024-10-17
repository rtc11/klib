package ksp

import com.google.devtools.ksp.*
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import kotlin.io.println

class TestProcessor(codeGenerator: CodeGenerator) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver.getSymbolsWithAnnotation("ksp.YourAnnotation")
                .filterIsInstance<KSClassDeclaration>()
                .forEach { println("test ${it.simpleName}") }

        return emptyList()
    }
}


class TestProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return TestProcessor(environment.codeGenerator)
    }
}
