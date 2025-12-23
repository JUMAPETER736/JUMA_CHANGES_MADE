pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        jcenter()
        maven(uri("https://jitpack.io"))
        maven(uri("https://oss.sonatype.org/content/repositories/snapshots"))
        maven(uri("https://jcenter.bintray.com"))
    }
}

rootProject.name = "Circuit"
include(":app")
include(":network")
include(":notifications")
include(":notifications")
include(":medialoader")
include(":core")
include(":compressor")
include(":chatsuit")
include(":call")
include(":business")
