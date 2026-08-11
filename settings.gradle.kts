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
        maven("https://jitpack.io") // github.com/mohamedchouat/rest-api-debugger — see app/build.gradle.kts
    }
}

rootProject.name = "Articles"
include(":app")
