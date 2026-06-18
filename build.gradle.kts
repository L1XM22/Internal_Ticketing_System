plugins {
    kotlin("jvm") version "1.9.22"
    id("org.jetbrains.compose") version "1.6.0"
}

group = "com.intersupport"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

sourceSets {
    main {
        kotlin.srcDirs("src")
    }
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // SQL Server JDBC Driver
    implementation("com.microsoft.sqlserver:mssql-jdbc:12.4.2.jre11")
    
    // JavaMail API for SMTP
    implementation("com.sun.mail:javax.mail:1.6.2")
}

compose.desktop {
    application {
        mainClass = "com.intersupport.ticketingsystem.MainKt"
        
        nativeDistributions {
            targetFormats(org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi)
            packageName = "InterSupport"
            packageVersion = "1.0.0"
            description = "IT Ticketing System"
            vendor = "Intercable"
            
            // This explicitly includes necessary modules that the packager might miss.
            modules("java.sql")
            modules("java.naming")
            modules("java.desktop")
            modules("jdk.crypto.ec")

            windows {
                menuGroup = "InterSupport"
                // The path now points to the project root, not the temporary build folder.
                iconFile.set(project.file("icon.ico"))
            }
        }
        
        buildTypes.release.proguard {
            isEnabled.set(false)
        }
    }
}