package io.github.sonarkt

import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.singleFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid

class KotlinAnalysis {
    fun perform(element: KtElement) {
        element.accept(object : KtTreeVisitorVoid() {
            override fun visitCallExpression(expression: KtCallExpression) {
                println("Found call: ${expression.text}")

                analyze(expression) {
                    val call = expression.resolveToCall()
                    val functionSymbol = call?.singleFunctionCallOrNull()?.symbol

                    functionSymbol?.let { symbol ->
                        val fqName = symbol.callableId?.asSingleFqName()
                        println("  Resolved to: $fqName")
                    }
                }

                super.visitCallExpression(expression)
            }
        })
    }
}