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
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"

    jacoco
    `java-library`
    `maven-publish`
    `project-report`
    signing
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    mavenCentral()
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            username.set(System.getenv("SONATYPE_USERNAME"))
            password.set(System.getenv("SONATYPE_PASSWORD"))
        }
    }
}

signing {
    useInMemoryPgpKeys(
        System.getenv("SIGNING_KEY_ID"),
        System.getenv("SIGNING_KEY"),
        System.getenv("SIGNING_KEY_PASSWORD"))
    sign(publishing.publications["nebula"])
}

contacts {
    addPerson("jakub.stan.malek@gmail.com", delegateClosureOf<Contact> {
        github = "jakubmalek"
        moniker = "Jakub Małek"
        role("owner")
    })
}

sourceSets {
    register("junit4Test") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

configurations {
    "junit4TestImplementation" {
        extendsFrom(implementation.get())
    }
    "junit4TestAnnotationProcessor"  {
        extendsFrom(annotationProcessor.get())
    }
    "junit4TestRuntimeOnly" {
        extendsFrom(runtimeOnly.get())
    }
}

group = "com.webfleet"

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

    // junit4 test used to test optional dependency to opentest4j
    "junit4TestImplementation"("junit", "junit", "4.13.2")
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
    // Test tasks
    val junit4Test = register<Test>("junit4Test") {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        description = "Test checking compatibility with JUnit4 API"
        systemProperty("file.encoding", "UTF-8")
        testLogging {
            events("passed", "skipped", "failed")
        }
        val sourceSet = sourceSets["junit4Test"]
        testClassesDirs = sourceSet.output.classesDirs
        classpath = configurations[sourceSet.runtimeClasspathConfigurationName] + sourceSet.output + sourceSets["main"].output
    }
    test {
        useJUnitPlatform()
        systemProperty("file.encoding", "UTF-8")
        finalizedBy(junit4Test)
        finalizedBy(jacocoTestReport)
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
    // Jacoco tasks
    jacocoTestReport {
        dependsOn(test)
        dependsOn(junit4Test)
        executionData(fileTree(project.buildDir).include("jacoco/*.exec"))
        finalizedBy(jacocoTestCoverageVerification)
        reports {
            xml.required.set(false)
            html.required.set(true)
            csv.required.set(true)
        }
    }
    jacocoTestCoverageVerification {
        executionData(fileTree(project.buildDir).include("jacoco/*.exec"))
        violationRules {
            rule {
                element = "PACKAGE"
                includes = listOf("com.webfleet.*")
                limit {
                    counter = "INSTRUCTION"
                    value = "COVEREDRATIO"
                    minimum = "0.8".toBigDecimal()
                }
            }
        }
    }
}