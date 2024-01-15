import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.ksp)
}

kotlin {
    jvmToolchain(11)

    macosX64()
    macosArm64()
    iosArm64()
    iosX64()
    iosSimulatorArm64()

    watchosArm32()
    watchosArm64()
    watchosX64()
    watchosSimulatorArm64()
    // kotest support for Tier 3 KN targets is missing, expected in 5.9 release
    // watchosDeviceArm64()
    tvosArm64()
    tvosX64()
    tvosSimulatorArm64()
    jvm()
    js {
        browser()
        nodejs()
    }

    linuxArm64()
    linuxX64()
    mingwX64()


    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlin.coroutines.core)

            implementation(libs.turbine)
            implementation(libs.kotestMatchers)
        }
        commonTest.dependencies {
            implementation(project(":komposable-architecture-test"))
            implementation(libs.bundles.multiplatform.common.test)
        }
    }
}

dependencies {
    configurations
        .filter { it.name.startsWith("ksp") && it.name.contains("Test") }
        .forEach {
            add(it.name, "io.mockative:mockative-processor:2.0.1")
        }
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs += listOf(
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
        )
    }
}