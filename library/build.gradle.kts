// the default version name, used when publishing to the local maven repository
val localVersionName = "local"

group = "com.github.thumbtack"
version = "0.0.3"

if (properties.containsKey("localVersion")) {
    version = "local"
}

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    id("maven-publish")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions {
                jvmTarget = JavaVersion.VERSION_17.toString()
            }
        }
        publishing {
            mavenPublication {
                pom {
                    name.set("Kotlin Testing Tools by Thumbtack")
                    description.set("Utility functions to assist with automated testing of Kotlin code.")
                    url.set("https://github.com/thumbtack/kotlin-testing-tools")

                    licenses {
                        license {
                            name.set("Apache License 2.0")
                            url.set("https://github.com/thumbtack/kotlin-testing-tools/blob/main/LICENSE")
                        }
                    }

                    scm {
                        connection.set("scm:git:git://github.com:thumbtack/kotlin-testing-tools.git")
                        developerConnection.set("scm:git:ssh://github.com:thumbtack/kotlin-testing-tools.git")
                        url.set("https://github.com/thumbtack/thumbprint-android")
                    }
                }
                groupId = project.group.toString()
                artifactId = "kotlin-testing-tools"
                version = project.version.toString()
            }
        }
    }
    // Keeping these in here so we can activate when we're ready to build other platforms
//    androidTarget {
//        publishLibraryVariants("release")
//        compilations.all {
//            kotlinOptions {
//                jvmTarget = "17.0"
//            }
//        }
//    }
//    iosX64()
//    iosArm64()
//    iosSimulatorArm64()
//    linuxX64()

    sourceSets {
//        val commonMain by getting {
//            dependencies {
//                //put your multiplatform dependencies here
//            }
//        }
//        val commonTest by getting {
//            dependencies {
//                implementation(libs.kotlin.test)
//            }
//        }
        val jvmMain by getting {
            dependencies {
                implementation(kotlin("reflect"))
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}
