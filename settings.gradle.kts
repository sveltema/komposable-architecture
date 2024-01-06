enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
//    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "TKA"

include(
    ":komposable-architecture",
    ":komposable-architecture-annotations",
    ":komposable-architecture-test",
    ":komposable-architecture-compiler"
)

//":komposable-architecture-compiler",
// "samples:todos"
