plugins {
    application
    java
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.6")
    implementation("ch.qos.logback:logback-core:1.5.0")
    implementation("ch.qos.logback:logback-classic:1.5.0")

    implementation("org.hyperledger.fabric:fabric-gateway-java:2.2.8")
    implementation("com.google.code.gson:gson:2.10.1")
}

application {
    mainClass.set("br.ufrgs.inf.ppgc.contaudit.admin.Startup")
}