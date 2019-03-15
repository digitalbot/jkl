import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm").version("1.3.20")
    id("com.github.johnrengelman.shadow").version("5.0.0")
    id("org.jmailen.kotlinter").version("1.21.0")
    id("org.jetbrains.dokka").version("0.9.17")
    application
}

repositories {
    jcenter()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    
    implementation("org.slf4j:slf4j-api:1.7.26")
    runtime("ch.qos.logback:logback-classic:1.2.3")

    implementation("com.github.ajalt:clikt:1.6.0")

    // for test
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

application {
    mainClassName = "com.digitalbot.jkl.AppKt"
    version = "1.0.0"
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
