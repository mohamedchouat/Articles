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
        maven("https://jitpack.io") // rest-api-debugger, consumed as a published artifact — see app/build.gradle.kts
    }
}

rootProject.name = "Articles"
include(":app")
include(":rest-api-debugger")
