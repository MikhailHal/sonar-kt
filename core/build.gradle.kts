val analysisApiKotlinVersion = "2.0.20-dev-3728"
val intellijVersion = "213.7172.25"

repositories {
    maven("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies")
    maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-ide-plugin-dependencies")
    maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/bootstrap")
    maven("https://www.jetbrains.com/intellij-repository/releases")
}

dependencies {
    /**
     * Kotlin Analysis APIはBOMが整備されておらず、直接的な依存関係を手動で設定する必要がある。
     * "https://github.com/fwcd/kotlin-analysis-server/blob/main/build.gradle.kts"を参照して依存関係を調整。
     */
    // IntelliJ Platform APIs
    implementation("com.jetbrains.intellij.platform:core:$intellijVersion")
    implementation("com.jetbrains.intellij.platform:core-impl:$intellijVersion")
    implementation("com.jetbrains.intellij.platform:util:$intellijVersion")

    // Kotlin Compiler
    implementation("org.jetbrains.kotlin:kotlin-compiler:$analysisApiKotlinVersion")

    // Kotlin Analysis API
    implementation("org.jetbrains.kotlin:high-level-api-fir-for-ide:$analysisApiKotlinVersion") { isTransitive = false }
    implementation("org.jetbrains.kotlin:high-level-api-for-ide:$analysisApiKotlinVersion") { isTransitive = false }
    implementation("org.jetbrains.kotlin:low-level-api-fir-for-ide:$analysisApiKotlinVersion") { isTransitive = false }
    implementation("org.jetbrains.kotlin:analysis-api-providers-for-ide:$analysisApiKotlinVersion") { isTransitive = false }
    implementation("org.jetbrains.kotlin:analysis-project-structure-for-ide:$analysisApiKotlinVersion") { isTransitive = false }
    implementation("org.jetbrains.kotlin:symbol-light-classes-for-ide:$analysisApiKotlinVersion") { isTransitive = false }
    implementation("org.jetbrains.kotlin:analysis-api-standalone-for-ide:$analysisApiKotlinVersion") { isTransitive = false }
    implementation("org.jetbrains.kotlin:high-level-api-impl-base-for-ide:$analysisApiKotlinVersion") { isTransitive = false }

    testImplementation(kotlin("test"))
}
