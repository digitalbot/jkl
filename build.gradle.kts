plugins {
    id("org.jetbrains.kotlin.jvm").version("1.3.20")
    id("com.github.johnrengelman.shadow").version("5.0.0")
    application
}

repositories {
    jcenter()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

application {
    mainClassName = "com.digitalbot.jkl.AppKt"
    version = "0.0.1-SNAPSHOT"
}
