import java.time.LocalDate
import java.time.format.DateTimeFormatter

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.2.20"
    id("org.jetbrains.intellij.platform") version "2.20.0"
}

group = "sj.zheng"
// 版本号自动取构建当天的日期（yyyy.MM.dd）
version = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("2024.2.4")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks {
    wrapper {
        gradleVersion = "9.8.2"
    }
    build {
        // Gradle 9 + IntelliJ Platform Plugin 2.x 下，build 不再自动产出可分发的插件 zip
        dependsOn(buildPlugin)
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            // 2024.2 是首个运行 JBR 21 的版本，Java 21 字节码要求 IDE 运行时为 JBR 21
            sinceBuild.set("242")
            // 不指定 untilBuild，保持向前兼容
            untilBuild = provider { null }
        }
    }
}
