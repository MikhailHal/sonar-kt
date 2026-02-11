package io.github.sonarkt

import com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.analysis.api.standalone.buildStandaloneAnalysisAPISession
import org.jetbrains.kotlin.analysis.project.structure.builder.buildKtSourceModule
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms
import org.jetbrains.kotlin.psi.KtFile
import java.nio.file.Paths
import kotlin.system.exitProcess

fun main() {
    // === Collector動作確認 ===
    testGitDiffParser()
    println()

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

    // === ChangedFunctionCollector動作確認 ===
    testChangedFunctionCollector(ktFiles)
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

    // === Emitter動作確認 ===
    println("\n=== Emitter Test ===")
    println("Output format (plain text):")
    println(AffectedTestEmitter.emit(affected1))

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

/**
 * GitDiffParserの動作確認
 */
fun testGitDiffParser() {
    println("=== GitDiffParser Test ===")

    // サンプルのgit diff出力（Calculator.ktのaddメソッドを変更した場合を想定）
    val sampleDiff = """
        diff --git a/core/src/main/kotlin/io/github/sonarkt/Calculator.kt b/core/src/main/kotlin/io/github/sonarkt/Calculator.kt
        --- a/core/src/main/kotlin/io/github/sonarkt/Calculator.kt
        +++ b/core/src/main/kotlin/io/github/sonarkt/Calculator.kt
        @@ -4 +4 @@ class Calculator {
        -    fun add(a: Int, b: Int): Int = a + b
        +    fun add(a: Int, b: Int): Int = a + b + 0  // 変更
        diff --git a/core/src/main/kotlin/io/github/sonarkt/Helper.kt b/core/src/main/kotlin/io/github/sonarkt/Helper.kt
        --- a/core/src/main/kotlin/io/github/sonarkt/Helper.kt
        +++ b/core/src/main/kotlin/io/github/sonarkt/Helper.kt
        @@ -7,3 +7,4 @@ fun helperB() {
        +    // コメント追加
    """.trimIndent()

    val result = GitDiffParser.parseKotlinFiles(sampleDiff)

    println("Parsed ${result.size} Kotlin files:")
    for ((path, fileDiff) in result) {
        println("  File: $path")
        for (range in fileDiff.changedLineRanges) {
            println("    Changed lines: $range")
        }
    }
}

/**
 * ChangedFunctionCollectorの動作確認
 */
fun testChangedFunctionCollector(ktFiles: List<KtFile>) {
    println("=== ChangedFunctionCollector Test ===")

    // サンプルのgit diff出力
    // Calculator.ktの4行目(addメソッド)とHelper.ktの7-10行目(helperB関数内)を変更
    val sampleDiff = """
        diff --git a/src/main/kotlin/io/github/sonarkt/Calculator.kt b/src/main/kotlin/io/github/sonarkt/Calculator.kt
        --- a/src/main/kotlin/io/github/sonarkt/Calculator.kt
        +++ b/src/main/kotlin/io/github/sonarkt/Calculator.kt
        @@ -4 +4 @@ class Calculator {
        -    fun add(a: Int, b: Int): Int = a + b
        +    fun add(a: Int, b: Int): Int = a + b + 0
        diff --git a/src/main/kotlin/io/github/sonarkt/Helper.kt b/src/main/kotlin/io/github/sonarkt/Helper.kt
        --- a/src/main/kotlin/io/github/sonarkt/Helper.kt
        +++ b/src/main/kotlin/io/github/sonarkt/Helper.kt
        @@ -7,3 +7,4 @@ fun helperB() {
        +    // comment
    """.trimIndent()

    val collector = ChangedFunctionCollector()
    val changedFunctions = collector.collect(sampleDiff, ktFiles)

    println("Changed functions:")
    if (changedFunctions.isEmpty()) {
        println("  (none found)")
    } else {
        changedFunctions.forEach { println("  - $it") }
    }
}
