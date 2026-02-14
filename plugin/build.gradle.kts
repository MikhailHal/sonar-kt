plugins {
    `java-gradle-plugin`
}

group = "io.github.sonarkt"
version = "0.1.0-SNAPSHOT"

repositories {
    maven("https://redirector.kotlinlang.org/maven/intellij-dependencies")
}

dependencies {
    compileOnly(gradleApi())
    implementation(project(":core"))
    testImplementation(kotlin("test"))
}

gradlePlugin {
    plugins {
        create("sonarKt") {
            id = "io.github.sonarkt"
            implementationClass = "io.github.sonarkt.gradle.SonarKtPlugin"
        }
    }
}
