
plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    kotlin("jvm") version "1.4.21"

    // Apply the application plugin to add support for building a CLI application in Java.
    application
}

repositories {
    // Use JCenter for resolving dependencies.
    jcenter()
}

dependencies {
    // OpenGL library
	val jogampVersion = "2.3.1"
	implementation("org.jogamp.gluegen:gluegen-rt:$jogampVersion")
	implementation("org.jogamp.jogl:jogl-all:$jogampVersion")

	runtimeOnly("org.jogamp.gluegen:gluegen-rt:$jogampVersion:natives-android-aarch64")
	runtimeOnly("org.jogamp.gluegen:gluegen-rt:$jogampVersion:natives-android-armv6")
	runtimeOnly("org.jogamp.gluegen:gluegen-rt:$jogampVersion:natives-linux-amd64")
	runtimeOnly("org.jogamp.gluegen:gluegen-rt:$jogampVersion:natives-linux-armv6")
	runtimeOnly("org.jogamp.gluegen:gluegen-rt:$jogampVersion:natives-linux-armv6hf")
	runtimeOnly("org.jogamp.gluegen:gluegen-rt:$jogampVersion:natives-linux-i586")
	runtimeOnly("org.jogamp.gluegen:gluegen-rt:$jogampVersion:natives-macosx-universal")
	runtimeOnly("org.jogamp.gluegen:gluegen-rt:$jogampVersion:natives-solaris-amd64")
	runtimeOnly("org.jogamp.gluegen:gluegen-rt:$jogampVersion:natives-solaris-i586")
	runtimeOnly("org.jogamp.gluegen:gluegen-rt:$jogampVersion:natives-windows-amd64")
	runtimeOnly("org.jogamp.gluegen:gluegen-rt:$jogampVersion:natives-windows-i586")

	runtimeOnly("org.jogamp.jogl:jogl-all:$jogampVersion:natives-android-aarch64")
	runtimeOnly("org.jogamp.jogl:jogl-all:$jogampVersion:natives-android-armv6")
	runtimeOnly("org.jogamp.jogl:jogl-all:$jogampVersion:natives-linux-amd64")
	runtimeOnly("org.jogamp.jogl:jogl-all:$jogampVersion:natives-linux-armv6")
	runtimeOnly("org.jogamp.jogl:jogl-all:$jogampVersion:natives-linux-armv6hf")
	runtimeOnly("org.jogamp.jogl:jogl-all:$jogampVersion:natives-linux-i586")
	runtimeOnly("org.jogamp.jogl:jogl-all:$jogampVersion:natives-macosx-universal")
	runtimeOnly("org.jogamp.jogl:jogl-all:$jogampVersion:natives-solaris-amd64")
	runtimeOnly("org.jogamp.jogl:jogl-all:$jogampVersion:natives-solaris-i586")
	runtimeOnly("org.jogamp.jogl:jogl-all:$jogampVersion:natives-windows-amd64")
	runtimeOnly("org.jogamp.jogl:jogl-all:$jogampVersion:natives-windows-i586")

	// Use the Kotlin JDK 8 standard library.
	implementation(kotlin("stdlib-jdk8"))

	// This dependency is used by the application.
	implementation("com.google.guava:guava:29.0-jre")
}

tasks {
	compileKotlin {
        kotlinOptions.jvmTarget = "11"
    }
    run.configure {
        jvmArgs("--illegal-access=deny")
        workingDir = project.rootDir
    }
}
java {
	sourceCompatibility = JavaVersion.VERSION_11
	targetCompatibility = JavaVersion.VERSION_11
}

application {
    // Define the main class for the application.
    mainClass.set("shaderView.MainKt")
}
