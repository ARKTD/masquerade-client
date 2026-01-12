plugins {
    id("java")
}

group = "org.masquerade"
version = "0.0.1-alpha"

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.java.dev.jna:jna:5.17.0")
    implementation("net.java.dev.jna:jna-platform:5.17.0")
    implementation("org.fusesource.jansi:jansi:2.4.2")
}


tasks.withType<JavaExec> {
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}

tasks.withType<Test> {
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "org.masquerade.Main",
            "Add-Opens" to "java.base/java.lang=ALL-UNNAMED",
            "Enable-Native-Access" to "ALL-UNNAMED"
        )
    }

    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}