import nebula.plugin.contacts.Contact

plugins {
    id("org.owasp.dependencycheck") version "7.1.1"
    id("nebula.release") version "16.0.0"
    id("nebula.maven-nebula-publish") version "18.4.0"
    id("nebula.maven-developer") version "18.4.0"
    id("nebula.maven-scm") version "18.4.0"
    id("nebula.contacts") version "6.0.0"
    id("nebula.info-scm") version "11.3.3"
    id("tylerthrailkill.nebula-mit-license") version "0.0.3"

    jacoco
    `java-library`
    `maven-publish`
    `project-report`
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    mavenCentral()
}

dependencies {
    // api
    api("org.assertj", "assertj-core", "[3.23.0,3.24.0[")
    compileOnly("org.opentest4j", "opentest4j", "[1.2.0,1.3.0[")

    // lombok
    compileOnly(annotationProcessor("org.projectlombok", "lombok", "[1.18.0,2.0.0["))
    testCompileOnly(testAnnotationProcessor("org.projectlombok", "lombok", "[1.18.0,2.0.0["))

    // test
    testImplementation("org.junit.jupiter", "junit-jupiter", "[5.8.0,6.0.0[")
    testImplementation("org.mockito", "mockito-junit-jupiter", "[4.6.0,5.0.0[")
}

tasks {
    test {
        useJUnitPlatform()
    }
}

contacts {
    addPerson("jakub.stan.malek@gmail.com", delegateClosureOf<Contact> {
        github = "jakubmalek"
        moniker = "Jakub Małek"
        role("owner")
    })
}