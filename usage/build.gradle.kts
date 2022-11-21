plugins {
    kotlin("jvm") version "1.7.20"
    id("com.google.devtools.ksp")
}

repositories {
    mavenCentral()
}

kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
    sourceSets.test {
        kotlin.srcDir("build/generated/ksp/test/kotlin")
    }
}

dependencies {
    implementation(project(":annotation"))
    implementation(project(":processor"))
    ksp(project(":processor"))
}