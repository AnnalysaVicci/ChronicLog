// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false

    // Add the dependency for the Google services Gradle plugin
    id("com.google.gms.google-services") version "4.4.4" apply false
    // safe args plugin
    id("androidx.navigation.safeargs.kotlin") version "2.9.7" apply false
    id("com.google.devtools.ksp") version "2.3.5" apply false
}