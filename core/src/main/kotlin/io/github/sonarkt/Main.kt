package io.github.sonarkt

import com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.cli.common.config.addKotlinSourceRoot
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import java.io.File

fun main() {
    val env = createKotlinCoreEnvironment()
    val sourceFiles = env.getSourceFiles()

    println("Found ${sourceFiles.size} source files:")
    sourceFiles.forEach { ktFile ->
        println("  - ${ktFile.name}")
    }
}

private fun createKotlinCoreEnvironment(): KotlinCoreEnvironment {
    val configuration = CompilerConfiguration().apply {
        // MessageCollector
        put(CommonConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)

        // モジュール名を設定
        put(CommonConfigurationKeys.MODULE_NAME, "core")

        // ソースルートを追加
        addKotlinSourceRoot("/Users/haruto/Desktop/dev/oss/sonar-kt/core/src/main/kotlin")

        // JDKクラスパス
        val jdkHome = File(System.getProperty("java.home"))
        put(JVMConfigurationKeys.JDK_HOME, jdkHome)
    }

    // IntelliJのファイルシステムフォールバック
    System.setProperty("idea.io.use.fallback", "true")

    return KotlinCoreEnvironment.createForProduction(
        Disposer.newDisposable(),
        configuration,
        EnvironmentConfigFiles.JVM_CONFIG_FILES
    )
}