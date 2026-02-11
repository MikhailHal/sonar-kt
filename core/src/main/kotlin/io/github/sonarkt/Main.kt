package io.github.sonarkt

import com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.analysis.api.standalone.buildStandaloneAnalysisAPISession
import org.jetbrains.kotlin.analysis.project.structure.builder.buildKtSourceModule
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms
import org.jetbrains.kotlin.psi.KtFile
import java.nio.file.Paths
import kotlin.system.exitProcess

fun main() {
    val projectDisposable = Disposer.newDisposable("PoC")

    val session = buildStandaloneAnalysisAPISession(projectDisposable) {
        buildKtModuleProvider {
            platform = JvmPlatforms.defaultJvmPlatform

            addModule(buildKtSourceModule {
                moduleName = "core"
                platform = JvmPlatforms.defaultJvmPlatform
                addSourceRoot(Paths.get("/Users/haruto/Desktop/dev/oss/sonar-kt/core/src/main/kotlin"))
            })
        }
    }

    // 全KtFileを取得
    val ktFiles = session.modulesWithFiles
        .flatMap { it.value }
        .filterIsInstance<KtFile>()

    println("=== Found ${ktFiles.size} Kotlin files ===")
    ktFiles.forEach { println("  - ${it.name}") }
    println()

    // 依存グラフを構築
    println("=== Building dependency graph ===")
    val graphBuilder = GraphBuilder()
    val graph = graphBuilder.build(ktFiles)
    println()

    // グラフの内容を表示
    println("=== Reverse Dependency Graph ===")
    println(graph.stats())
    println()
    for ((callee, callers) in graph.getAllEdges()) {
        println("$callee")
        callers.forEach { caller -> println("  <- $caller") }
    }
    println()

    // 影響テスト特定のテスト
    println("=== Affected Test Resolution ===")
    val resolver = AffectedTestResolver(graph)

    // シナリオ1: Calculator.add が変更された場合
    // 期待: testAdd (直接呼んでる), testHelper (helperB経由で間接的に影響)
    val changedFunctions1 = setOf("io.github.sonarkt.Calculator.add")
    val affected1 = resolver.findAffectedTests(changedFunctions1)
    println("\nChanged: $changedFunctions1")
    println("Affected tests:")
    affected1.forEach { println("  - $it") }

    // シナリオ2: helperB が変更された場合
    // 期待: testHelper のみ
    val changedFunctions2 = setOf("io.github.sonarkt.helperB")
    val affected2 = resolver.findAffectedTests(changedFunctions2)
    println("\nChanged: $changedFunctions2")
    println("Affected tests:")
    affected2.forEach { println("  - $it") }

    Disposer.dispose(projectDisposable)
    exitProcess(0)
}

fun printCallers(graph: ReverseDependencyGraph, callee: String) {
    val callers = graph.getCallers(callee)
    println("\nWho calls '$callee'?")
    if (callers.isEmpty()) {
        println("  (no callers found)")
    } else {
        callers.forEach { println("  - $it") }
    }
}
