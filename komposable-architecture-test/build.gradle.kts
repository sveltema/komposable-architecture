plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
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
    androidTarget()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "komposable-architecture-test"
            isStatic = true
        }
    }

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

android {
    namespace = "com.toggl.komposable_architecture_test"
    compileSdk = 34
    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
