plugins {
    alias(libs.plugins.kotlin.multiplatform)
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
}
