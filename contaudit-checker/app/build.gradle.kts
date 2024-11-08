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

    implementation("org.hyperledger.fabric:fabric-gateway-java:2.2.0")
    implementation("org.bouncycastle:bcprov-jdk18on:1.72")
    implementation("com.google.code.gson:gson:2.10.1")

    testImplementation("org.mockito:mockito-core:4.0.0")
    testImplementation("org.mockito:mockito-junit-jupiter:4.0.0")
    testImplementation("org.mockito:mockito-inline:4.0.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

application {
    mainClass.set("br.ufrgs.inf.ppgc.contaudit.Startup")
}
