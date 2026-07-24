val apiVersion = fetchProperty("version.api", "invalid")
val mavenUsername = fetchEnv("MAVEN_DEPLOY_USR", "maven.username.sirblobman", "")
val mavenPassword = fetchEnv("MAVEN_DEPLOY_PSW", "maven.password.sirblobman", "")

val baseVersion = fetchProperty("version.base", "invalid")
val betaString = fetchProperty("version.beta", "false")
val jenkinsBuildNumber = fetchEnv("BUILD_NUMBER", null, "Unofficial")

val betaBoolean = betaString.toBoolean()
val betaVersion = if (betaBoolean) "Beta-" else ""
version = "$baseVersion.$betaVersion$jenkinsBuildNumber"

fun fetchProperty(propertyName: String, defaultValue: String): String {
    val found = findProperty(propertyName)
    if (found != null) {
        return found.toString()
    }

    return defaultValue
}

fun fetchEnv(envName: String, propertyName: String?, defaultValue: String): String {
    val found = System.getenv(envName)
    if (found != null) {
        return found
    }

    if (propertyName != null) {
        return fetchProperty(propertyName, defaultValue)
    }

    return defaultValue
}

plugins {
    id("java")
    id("distribution")
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://nexus.sirblobman.xyz/public/")
}

dependencies {
    compileOnly("org.jetbrains:annotations:26.1.0") // JetBrains Annotations
    compileOnly("io.papermc.paper:paper-api:26.2.build.+") // PaperMC API
}

distributions {
    main {
        contents {
            into("/") {
                from("resourcepack")
            }
        }
    }
}

tasks {
    named<Jar>("jar") {
        archiveBaseName.set("SonicScrewdriver")
    }

    named("distTar") {
        enabled = false
    }

    named<Zip>("distZip") {
        isPreserveFileTimestamps = true
        archiveBaseName.set("resourcepack")
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-Xlint:deprecation")
        options.compilerArgs.add("-Xlint:unchecked")
    }

    withType<Javadoc> {
        options.encoding = "UTF-8"
        val standardOptions = options as StandardJavadocDocletOptions
        standardOptions.addStringOption("Xdoclint:none", "-quiet")
    }

    named<ProcessResources>("processResources") {
        val pluginVersion = providers.provider { project.version.toString() }
        inputs.property("version", pluginVersion)

        filesMatching("paper-plugin.yml") {
            expand(mapOf("version" to pluginVersion.get()))
        }
    }
}
