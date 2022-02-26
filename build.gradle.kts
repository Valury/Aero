plugins {
    id("maven-publish")
    id("com.github.johnrengelman.shadow") version "7.1.2"
    kotlin("jvm") version "1.6.10"
    kotlin("kapt") version "1.6.10"
}

repositories {
    maven {
        url = uri("https://repo.jaren.wtf/repository/valury/")
        credentials {
            username = property("valuryUsername") as String
            password = property("valuryPassword") as String
        }
    }
    mavenCentral()
    maven("https://nexus.velocitypowered.com/repository/maven-public/")
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://jitpack.io/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://m2.dv8tion.net/releases")
    maven("https://repo.viaversion.com/")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    compileOnly("com.velocitypowered:velocity-api:3.1.1")
    kapt("com.velocitypowered:velocity-api:3.1.1")

    compileOnly("wtf.jaren.valury:valury-api:1.17.1-R0.1-SNAPSHOT")

    compileOnly("net.luckperms:api:5.3")

    implementation("org.mongodb:mongodb-driver-sync:4.5.0")

    implementation("org.ocpsoft.prettytime:prettytime:5.0.2.Final")

    compileOnly("com.github.plan-player-analytics:Plan:5.4.1366")

    compileOnly("net.luckperms:api:5.3")

    compileOnly("me.clip:placeholderapi:2.10.10")

    implementation("net.dv8tion:JDA:5.0.0-alpha.5") {
        exclude(module = "opus-java")
    }

    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")

    compileOnly("com.viaversion:viaversion-api:4.1.1")

    implementation("net.kyori:adventure-platform-bukkit:4.0.1")
}

publishing {
    repositories {
        maven {
            name = "valury"
            url = uri("https://repo.jaren.wtf/repository/valury/")
            credentials(PasswordCredentials::class)
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = "wtf.jaren.aero"
            artifactId = "aero"
            version = "1.0-SNAPSHOT"

            from(components["kotlin"])
        }
    }
}


tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    archiveFileName.set("Aero.jar")
}
