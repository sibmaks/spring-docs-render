plugins {
    java
    id("org.springframework.boot") version "2.7.8"
    id("io.spring.dependency-management") version "1.1.7"
    id("io.gatling.gradle") version "3.13.5.4"
}

group = "com.github.sibmaks"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencyManagement {
    // for Gatling
    imports {
        mavenBom("com.fasterxml.jackson:jackson-bom:2.18.3")
        mavenBom("io.netty:netty-bom:4.1.119.Final")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation("net.sf.jasperreports:jasperreports:6.21.3")
    implementation("net.sf.barcode4j:barcode4j:2.1")
    implementation("com.google.zxing:core:3.5.3")
    implementation("org.apache.xmlgraphics:batik-bridge:1.18")

    compileOnly("org.projectlombok:lombok")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    runtimeOnly("com.h2database:h2")
  
    runtimeOnly("org.postgresql:postgresql")

    annotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

gatling {
}
