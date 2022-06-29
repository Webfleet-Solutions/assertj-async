plugins {
    jacoco
    `java-library`
    `maven-publish`
    `project-report`
}

java {
    targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    mavenCentral()
}

dependencies {
    // api
    api("org.assertj", "assertj-core", "[3.23.0,3.24.0[")
    api("org.opentest4j", "opentest4j", "[1.2.0,1.3.0[")

    // Lombok
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

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            pom {
                description.set("AssertJ extension for making asynchronous assertions")
                url.set("https://github.com/Webfleet-Solutions/assertj-async")
                licenses {
                    license {
                        name.set("The MIT License")
                        url.set("https://github.com/Webfleet-Solutions/assertj-async/blob/main/LICENSE")
                    }
                }
                developers {
                    developer {
                        id.set("jakubmalek")
                        name.set("Jakub Malek")
                        email.set("jakub.stan.malek@gmail.com")
                    }
                }
                scm {
                    url.set("https://github.com/Webfleet-Solutions/assertj-async")
                }
                issueManagement {
                    url.set("https://github.com/Webfleet-Solutions/assertj-async/issues")
                }
            }
        }
    }
}