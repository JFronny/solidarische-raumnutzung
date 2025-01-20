import java.util.*
import kotlin.experimental.xor

plugins {
    java
    id("org.springframework.boot") version "3.4.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.flywaydb.flyway") version "11.1.1"
    id("gg.jte.gradle") version "3.1.16"
    jacoco
}

group = "edu.kit.hci"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

val doctex by configurations.creating

// Used to un-salt the credentials for the GitHub package repository
// For some reason, GitHub does not allow anonymous access to public packages,
// so we have to embed the credentials.
// Salting them makes it harder to find the credentials and should prevent automatic discovery, but does not make them secure.
// As such, a restricted token of a service account is used.
private fun unsalt(data: String, salt: LongArray) = salt.map { Random(it shl 3 or 12) }.run { Base64.getDecoder().decode(data).mapIndexed { l, r -> ByteArray(5).let { get((l + 3 shr 5).mod(size - (l % 2))).nextBytes(it); it[l % 4] } xor r } }.toByteArray().let { dm -> dm.decodeToString(dm[11].toUByte().toInt() % 65, dm.size - dm[12].toUByte().toInt() % dm[11].toUByte().toInt(), false) }.replace('\uFFFD', '1')

repositories {
    mavenCentral()
    maven("https://maven.pkg.github.com/Solidarische-Raumnutzung/DocTeX") {
        credentials {
            username = ""
            password = unsalt("MsUHMHvj/UIdFMSOMTkYborAekxPK52/EB4bp2Kxmd+cShtWiO6xXeMJp1NNIvBhAcNvH35gG2/uh9DRCs8+mf3YzyIIarY+1hTSOW6BAi3tkqOxDmNfU4bbKKj/M/JpmSKnafLJYcdMH1ASRvEWuCA=", longArrayOf(-1344705729828745711, 5230045263062089707, 3545512903530151910, 7538809493429025070, -2925924544857994240, -8595937774785329809, -4208388178093363894, 6992710797411217798, -6389370378960172826, 3591822878033896109, 8889865935792943001, -4262397429266853753, 2298705730591068518, 4714639302703995747, -7464986330344552584, -7518779346602212671, -7579240134292203452, 2373566333070360596, 6000643398013606382, 1307504402323163593, 3915882559778035436, -8081209879159995769, -9207588343828866839, -3787429060347275080, 6273897675385322987, 288272166847093320, 6835607591775321015, -6203230303138578259, -4541277508978494093, 2065286167320721702, -2261015450204600435, -9132305004876616378, -7041677951324361252, 5891454128313438131, 3367594326194710532, 7273550992031740203, 980246378010968093, -145285380090142264, 5638755824395442070, -4662549621614845308, -8319182660990689705, 4593349031178611454, 9188854271361800229, 3389677104980428669, -1826074559192361909, -991538057649777867, 6767921920810646389, 425275679354278601, -1129287175256942626, 3246752065518662417, -3173336438845015123, 324568664530661404))
        }
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-core:11.1.1") // TODO: remove explicit version when the Spring Boot version is updated
    implementation("org.flywaydb:flyway-database-postgresql:11.1.1")

    implementation("org.springframework.boot:spring-boot-starter-mail")

    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("gg.jte:jte:3.1.15")
    implementation("org.springframework.boot:spring-boot-starter-security")

    annotationProcessor("com.google.auto.service:auto-service:1.1.1")
    testAnnotationProcessor(annotationProcessor("org.projectlombok:lombok")!!)
    testCompileOnly(compileOnly("org.projectlombok:lombok")!!)

    compileOnly("org.jetbrains:annotations:26.0.1")

    doctex("edu.kit.hci.soli:doctex:1.0.1-SNAPSHOT")

    // https://docs.spring.io/spring-boot/reference/features/dev-services.html#features.dev-services.docker-compose
    testAndDevelopmentOnly("org.springframework.boot:spring-boot-docker-compose")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

jte {
    generate()
    binaryStaticContent = true
    packageName = "edu.kit.hci.soli.view.jte"
}

tasks {
    withType<Test> {
        useJUnitPlatform()
    }

    val copyArtifact by creating(Copy::class) {
        dependsOn(bootJar)
        from(bootJar)
        into(layout.buildDirectory.dir("libs"))
        rename(".*\\.jar", "app.jar")
    }
    assemble {
        dependsOn(copyArtifact)
    }

    val patchJteSources by creating {
        // By default, JTE does not mark its generated classes as such, which is a problem for our coverage
        fun String.markGenerated() = replace("""
                        @SuppressWarnings("unchecked")
                        public final class Jte
                    """.trimIndent().trim(), """
                        @SuppressWarnings("unchecked")
                        @edu.kit.hci.soli.config.Generated
                        public final class Jte
                    """.trimIndent().trim())

        // This is necessary because JTE does not use lambdas here and inner classes cause performance issues and mess with our coverage
        fun String.lambdafy(indent: String = "\t\t\t", jteOutput: String = "jteOutput"): String {
            val head = "new gg.jte.html.HtmlContent() {\n${indent}public void writeTo(gg.jte.html.HtmlTemplateOutput $jteOutput) {"
            val tail = "\n${indent}}"
            val headIndex = indexOf(head)
            if (headIndex == -1) return this
            val tailIndex = indexOf(tail, headIndex)
            if (tailIndex == -1) throw Exception("Could not find tail of lambda in JTE source")
            val lowerJteOutput = "${jteOutput}_"
            val loweredSection = (1..10).map { "\t".repeat(it) }.map { "$indent$it" } // Conditions may cause this to vary
                .fold(substring(headIndex + head.length, tailIndex).replace(jteOutput, lowerJteOutput)) { l, i -> l.lambdafy(i, lowerJteOutput) }
            val result = substring(0, headIndex) + "(gg.jte.html.HtmlContent) ${jteOutput}_ -> {" +
                    loweredSection +
                    substring(tailIndex + tail.length)
            return result.lambdafy(indent, jteOutput) // Recurse to handle multiple lambdas
        }

        // Patch the generated JTE sources
        doLast {
            layout.buildDirectory.dir("generated-sources/jte").get()
                .asFileTree
                .filter { it.isFile && it.extension == "java" }
                .forEach {
                    it.writeText(it.readText()
                        .markGenerated()
                        .lambdafy()
                    )
                }
        }
    }

    generateJte {
        finalizedBy(patchJteSources)
    }
    compileJava {
        dependsOn(patchJteSources)
    }

    jacocoTestReport {
        dependsOn(test)
        reports {
            xml.required = true
        }
    }

    val doctex by creating(JavaExec::class) {
        group = "documentation"
        description = "Generate documentation from LaTeX sources"
        mainClass = "de.mr_pine.doctex.CliKt"
        classpath = doctex
        args("--output=./entwurfsheft/javadoc", "src/main/java", "edu.kit.hci.soli")
    }
}
