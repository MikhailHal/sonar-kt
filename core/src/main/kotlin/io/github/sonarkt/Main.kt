package io.github.sonarkt

import com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.analysis.api.standalone.buildStandaloneAnalysisAPISession
import org.jetbrains.kotlin.analysis.project.structure.builder.buildKtSourceModule
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms
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

    val ktFile = session.modulesWithFiles
        .flatMap { it.value }
        .filterIsInstance<org.jetbrains.kotlin.psi.KtFile>()
        .find { it.name == "PoC1.kt" }

    ktFile?.let {
        val ka = KotlinAnalysis()
        ka.perform(it)
    }

    Disposer.dispose(projectDisposable)
    exitProcess(0)
}
