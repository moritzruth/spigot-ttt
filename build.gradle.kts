import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    kotlin("jvm") version "1.3.71"
    id("com.github.johnrengelman.shadow") version("5.2.0")
}

group = "de.moritz-ruth.spigot-ttt"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://oss.sonatype.org/content/repositories/central")
    maven("https://repo.dmulloy2.net/nexus/repository/public/")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("net.dv8tion:JDA:4.1.1_155")
    implementation("com.google.guava:guava:29.0-jre")
    compileOnly(files("./libs/CorpseReborn.jar"))
    compileOnly(files("./libs/ActionBarAPI.jar"))
    compileOnly(files("./libs/ProtocolLib.jar"))
    compileOnly("org.spigotmc", "spigot-api", "1.14.4-R0.1-SNAPSHOT")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
}

tasks.withType<ShadowJar> {
    val mcServerPath = System.getenv("MC_SERVER_DIR")
    if (mcServerPath != null) {
        destinationDirectory.set(file("$mcServerPath/plugins"))
    }

    archiveFileName.set("TTT.jar")
    dependencies {
        exclude(dependency("org.spigotmc:spigot-api"))
    }
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "11"
    }
}
