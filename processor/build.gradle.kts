plugins {
    kotlin("jvm") version "1.7.20"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":annotation"))
    implementation(kotlin("reflect"))
    implementation("com.google.devtools.ksp:symbol-processing-api:1.7.20-1.0.8")
    implementation("com.github.cretz.kastree:kastree-ast-jvm:0.4.0")
    implementation("com.github.cretz.kastree:kastree-ast-psi:0.4.0")
    implementation("com.github.cretz.kastree:kastree-ast-common:0.4.0")
}

sourceSets.main {
    java.srcDirs("src/main/kotlin")
}
