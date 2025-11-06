@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "EntainNextRaces"

include(
    ":app",
    ":core:common",
    ":core:data",
    ":core:model",
    ":core:network",
    ":core:designsystem",
    ":core:testing",
    ":feature:races"
)
