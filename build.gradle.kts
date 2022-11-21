plugins {
    kotlin("jvm") version "1.7.20" apply false
    id("com.google.devtools.ksp") version "1.7.20-1.0.8" apply false
}

buildscript {
    dependencies {
        classpath(kotlin("gradle-plugin", version = "1.7.20"))
    }
}
