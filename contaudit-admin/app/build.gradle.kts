/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java application project to get you started.
 * For more details take a look at the 'Building Java & JVM projects' chapter in the Gradle
 * User Manual available at https://docs.gradle.org/7.6/userguide/building_java_projects.html
 */
plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
    java
}

repositories {
    mavenLocal()
    // Use Maven Central for resolving dependencies.
    mavenCentral()
    maven(url = "https://hyperledger.jfrog.io/hyperledger/fabric-maven")
    maven(url = "https://jitpack.io")
}

dependencies {
    // Use JUnit test framework.
    testImplementation("junit:junit:4.13.2")

    // This dependency is used by the application.
    implementation("org.hyperledger.fabric:fabric-gateway-java:2.2.8")
    implementation("org.bouncycastle:bcprov-jdk18on:1.72")
    implementation("org.slf4j:slf4j-simple:2.0.6")
    implementation("org.apache.logging.log4j:log4j-api:2.20.0")
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.googlecode.java-diff-utils:diffutils:1.3.0")
}

application {
    // Define the main class for the application.
    mainClass.set("br.ufrgs.inf.ppgc.contaudit.admin.Startup")
}