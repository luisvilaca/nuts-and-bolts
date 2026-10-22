import com.google.firebase.appdistribution.gradle.firebaseAppDistribution
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.appDistribution)
}

// Sync (Firebase) only when the project's google-services.json is here (see SYNC.md). Without it the app builds and
// runs as before, with sync off.
if (file("google-services.json").exists()) apply(plugin = libs.plugins.googleServices.get().pluginId)

// Kept out of git, in local.properties: who gets each build through App Distribution (appDistributionTesters=a@b.c,...).
val local = Properties().apply { rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) } }

// The build's hour as yyMMddHH (26102115 = 2026-10-21, 15h), so every build installs over the one before. A
// ValueSource, so the configuration cache reads it again on every build instead of keeping the first one.
abstract class BuildHour : ValueSource<Int, ValueSourceParameters.None> {
    override fun obtain(): Int = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHH")).toInt()
}
val buildHour: Int = providers.of(BuildHour::class.java) {}.get()

android {
    namespace = "com.luisvilaca.nutsandbolts"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.luisvilaca.nutsandbolts"
        minSdk = 26
        targetSdk = 37
        versionCode = buildHour
        versionName = "1.1.$buildHour"
    }
    buildFeatures {
        compose = true
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // no store key yet: signed with this PC's debug key, so the release build installs over Android Studio's
            // and App Distribution's alike; every update must come from this same key (see SYNC.md)
            signingConfig = signingConfigs.getByName("debug")
            // gradlew appDistributionUploadRelease sends this build to the testers' tablet and phone
            firebaseAppDistribution {
                artifactType = "APK"
                testers = local.getProperty("appDistributionTesters", "")
            }
        }
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    implementation(libs.activity.compose)
    debugImplementation(libs.compose.ui.tooling)
    // sync: Google sign-in and the cloud copy of the state and the diary pages
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.credentials)
    implementation(libs.credentials.play)
    implementation(libs.googleid)
    // the home-screen widget: today's habits with a Done each
    implementation(libs.glance)
    testImplementation(libs.junit)
    // screen captures on the JVM (ScreensTest), so the layouts can be checked without a device
    testImplementation(libs.robolectric)
    testImplementation(platform(libs.compose.bom))
    testImplementation(libs.compose.ui.test)
    debugImplementation(libs.compose.ui.test.manifest)
}
