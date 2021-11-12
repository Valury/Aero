plugins {
    id("maven-publish")
    id("com.github.johnrengelman.shadow") version "7.1.0"
    kotlin("jvm") version "1.5.31"
    kotlin("kapt") version "1.5.21"
}

repositories {
    mavenCentral()
    maven("https://nexus.velocitypowered.com/repository/maven-public/")
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://jitpack.io/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://m2.dv8tion.net/releases")
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.0.0")
    kapt("com.velocitypowered:velocity-api:3.0.0")

    compileOnly("io.papermc.paper:paper-api:1.17-R0.1-SNAPSHOT")

    compileOnly("net.luckperms:api:5.3")

    implementation("org.mongodb:mongodb-driver-sync:4.3.2")

    implementation("org.ocpsoft.prettytime:prettytime:5.0.2.Final")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    compileOnly("com.github.plan-player-analytics:Plan:5.4.1366")

    compileOnly("net.luckperms:api:5.3")

    compileOnly("me.clip:placeholderapi:2.10.10")

    implementation("net.dv8tion:JDA:4.3.0_277")

    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
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
            groupId = "wtf.jaren.aurora"
            artifactId = "aurora"
            version = "1.0"

            from(components["kotlin"])
        }
    }
}


tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "16"
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    archiveFileName.set("Aurora.jar")
}
