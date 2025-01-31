plugins {
	`java-library`
	`maven-publish`
	signing
	id("com.github.ben-manes.versions") version "0.52.0"
 }

repositories {
	mavenLocal()
	mavenCentral()
	maven("https://oss.sonatype.org/content/repositories/snapshots")
}

group = "com.github.calimero"
version = "2.6-rc1"

tasks.compileJava {
	options.encoding = "UTF-8"
	options.compilerArgs = listOf(
		"-Xlint:all",
		"-Xlint:-options"
	)
}

tasks.compileTestJava {
	options.encoding = "UTF-8"
	options.compilerArgs = listOf(
		"-Xlint:all",
		"-Xlint:-try"
	)
}

tasks.named<JavaCompile>("compileJava") {
	options.javaModuleVersion = project.version.toString()
}

sourceSets {
	main {
		java.srcDir("src")
		resources.srcDir("resources")
	}
	test {
		java.srcDirs("test")
		resources.srcDir("build/classes/test")
		runtimeClasspath += files("build/classes/test")
	}
}

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(17))
	}
	withSourcesJar()
	withJavadocJar()
}

tasks.withType<Jar>().configureEach {
	from(projectDir) {
		include("LICENSE.txt")
		into("META-INF")
	}
	if (name == "sourcesJar") {
		from(projectDir) {
			include("README.md", "build.gradle", "settings.gradle", "gradle*/**")
		}
	}
}

dependencies {
	api("com.github.calimero:calimero-core:${project.version}")
	implementation("com.neuronrobotics:nrjavaserial:5.2.1")
}

publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			artifactId = rootProject.name
			from(components["java"])
			pom {
				name.set("Calimero RXTX Provider")
				description.set("Serial communication provider using RXTX")
				url.set("https://github.com/calimero-project/calimero-rxtx")
				licenses {
					license {
						name.set("GNU General Public License, version 2, with the Classpath Exception")
						url.set("LICENSE.txt")
					}
				}
				developers {
					developer {
						name.set("Boris Malinowsky")
						email.set("b.malinowsky@gmail.com")
					}
				}
				scm {
					connection.set("scm:git:git://github.com/calimero-project/calimero-rxtx.git")
					url.set("https://github.com/calimero-project/calimero-rxtx.git")
				}
			}
		}
	}
	repositories {
		maven {
			name = "maven"
			val releasesRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
			val snapshotsRepoUrl = uri("https://oss.sonatype.org/content/repositories/snapshots")
			url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
			credentials(PasswordCredentials::class)
		}
	}
}

signing {
	if (project.hasProperty("signing.keyId")) {
		sign(publishing.publications["mavenJava"])
	}
}
