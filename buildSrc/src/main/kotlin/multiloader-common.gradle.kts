plugins {
    id("java")
    id("idea")
    id("java-library")
}

version = "${commonMod.version}+${commonMod.mc}"

base {
    archivesName.set(commonMod.id)
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(commonProject.prop("java_version")!!)
    // withSourcesJar()
    // withJavadocJar()
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://maven.parchmentmc.org") {
        name = "ParchmentMC"
    }
    exclusiveContent {
        forRepository {
            maven("https://repo.spongepowered.org/repository/maven-public") { name = "Sponge" }
        }
        filter { includeGroupAndSubgroups("org.spongepowered") }
    }
    exclusiveContent {
        forRepositories(
            mavenLocal(),
            maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" },
            maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
        )
        filter { includeGroupAndSubgroups("dev.kikugie") }
    }
    exclusiveContent {
        forRepositories(
            maven {
                name = "Local Maven"
                url = rootProject.uri("local-maven")
            },
            maven("https://maven.terraformersmc.com/releases/") { name = "TerraformersMC" }
        )
        filter { includeGroupAndSubgroups("com.terraformersmc") }
    }
    exclusiveContent {
        forRepositories(
            maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" },
            maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
        )
        filter { includeGroupAndSubgroups("dev.kikugie") }
    }
    exclusiveContent {
        forRepositories(
            mavenCentral(), //1.21.1+ is on Maven Central
            maven("https://maven.isxander.dev/releases") { name = "Xander Maven" }
        )
        filter { includeGroupAndSubgroups("dev.isxander") }
    }
}

// Workaround for unstable KikuGie Maven downloads.
// The Fletching Table plugin adds dev.kikugie:fletching-table to compile resolution.
// On some networks Gradle cannot resolve it reliably, so we remove the module
// dependency and provide the downloaded API jar directly.
val localFletchingTableJar = rootProject.file("libs/kikugie/fletching-table-0.1.0-alpha.22-api.jar")

configurations.configureEach {
    exclude(group = "dev.kikugie", module = "fletching-table")
}

dependencies {
    compileOnly(files(localFletchingTableJar))
}


tasks {
    processResources {

        // Gradle 9 validates resource input directories during task configuration.
        // KSP may not generate anything for some versions, so the directory can be absent.
        layout.buildDirectory.dir("generated/ksp").get().asFile.mkdirs()

        val expandProps = mapOf(
            "version" to version as String,
            "java_version" to commonMod.propOrNull("java_version"),
            "forgelike_loader_version_range" to commonMod.propOrNull("forgelike_loader_version_range"),
            "forgelike_minecraft_version_range" to commonMod.propOrNull("forgelike_minecraft_version_range"),
            "fabric_minecraft_version_range" to commonMod.propOrNull("fabric_minecraft_version_range"),
            "loader" to loader
        ).filterValues { it?.isNotEmpty() == true }.mapValues { (_, v) -> v!! }

        val jsonExpandProps = expandProps.mapValues { (_, v) -> v.replace("\n", "\\\\n") }

        filesMatching(listOf("META-INF/mods.toml", "META-INF/neoforge.mods.toml")) {
            expand(expandProps)
        }

        filesMatching(listOf("pack.mcmeta", "fabric.mod.json", "*.mixins.json")) {
            expand(jsonExpandProps)
        }

        //HACK: Exclude post pass files on 1.21.5 and above, to resolve issue with Fletching Table
        //  failing to process files that result in being empty.
        if (stonecutterBuild.eval(stonecutterBuild.current.version, ">=1.21.5")) {
            exclude("assets/picturemode/shaders/post/*.json5")
        }

        inputs.properties(expandProps)
    }


}