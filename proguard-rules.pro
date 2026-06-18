# Ignore warnings about missing classes from optional dependencies of the SQL Server JDBC driver
-dontwarn com.azure.**
-dontwarn com.microsoft.aad.msal4j.**
-dontwarn org.antlr.v4.runtime.**
-dontwarn org.osgi.**
-dontwarn kotlinx.serialization.**
-dontwarn com.google.gson.**
-dontwarn reactor.core.publisher.**
-dontwarn org.bouncycastle.**
-dontwarn sun.font.**

# Keep data classes from being stripped
-keep class com.intersupport.ticketingsystem.data.** { *; }
-keepclassmembers class com.intersupport.ticketingsystem.data.** { *; }

# Keep JDBC driver classes
-keep class com.microsoft.sqlserver.jdbc.** { *; }
-keep class java.sql.** { *; }
