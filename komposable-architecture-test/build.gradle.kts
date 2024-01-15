import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

//extra.apply {
//    set("PUBLISH_GROUP_ID", "com.toggl")
//    set("PUBLISH_VERSION", "1.0.0-preview01")
//    set("PUBLISH_ARTIFACT_ID", "komposable-architecture-test")
//}
//
//apply(from = "${rootProject.projectDir}/scripts/publish-module.gradle")
//
//java {
//    withJavadocJar()
//    withSourcesJar()
//}

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
    watchosDeviceArm64()

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
            implementation(project(":komposable-architecture"))
            implementation(libs.bundles.multiplatform.common.test)
        }
        commonTest.dependencies {
            implementation(libs.bundles.multiplatform.common.test)
        }
    }
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs += listOf(
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
        )
    }
}