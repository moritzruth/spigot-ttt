import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    kotlin("jvm") version "1.3.72"
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
    implementation("commons-codec:commons-codec:1.14")
    compileOnly(files("./libs/ArmorEquipEvent-1.7.2.jar"))
    compileOnly("com.comphenix.protocol", "ProtocolLib", "4.5.0")
    compileOnly("org.spigotmc", "spigot-api", "1.15.2-R0.1-SNAPSHOT")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<ShadowJar> {
    val mcServerPath = System.getenv("MC_SERVER_DIR")
    if (mcServerPath != null) {
        destinationDirectory.set(file("$mcServerPath/plugins"))
    }

    archiveFileName.set("TTT.jar")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}
