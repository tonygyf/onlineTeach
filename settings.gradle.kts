pluginManagement {
    repositories {
        // 添加阿里云镜像，加快插件解析
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }

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
        // 阿里云的几个仓库镜像（顺序：google -> jcenter -> central）
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/central") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }

        // 原始官方源作为兜底
        google()
        mavenCentral()
    }
}


rootProject.name = "onlineTeach"
include(":app")
 