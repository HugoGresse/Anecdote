import java.util.Base64
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "io.gresse.hugo.anecdote"
    compileSdk = 37

    defaultConfig {
        applicationId = "io.gresse.hugo.anecdote"
        minSdk = 24
        targetSdk = 36
        versionCode = 28
        versionName = "2.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions.annotationProcessorOptions.arguments["room.schemaLocation"] =
            "$projectDir/schemas"
    }

    flavorDimensions += "store"
    productFlavors {
        create("fdroid") {
            dimension = "store"
        }
        create("play") {
            dimension = "store"
            // ObjectBox is non-free: it is only kept here to import favorites saved by versions up to 1.3.0.
            javaCompileOptions.annotationProcessorOptions.arguments["objectbox.modelPath"] =
                "$projectDir/objectbox-models/default.json"
        }
    }

    dependenciesInfo {
        // Encrypted dependency metadata is unreadable for F-Droid and reproducible builds.
        includeInApk = false
        includeInBundle = false
    }

    base.archivesName = "Anecdote-${defaultConfig.versionName}"

    signingConfigs {
        create("release") {
            val signingPropFile = rootProject.file("signing.properties")
            // GitHub Actions passes an unset secret as an empty string, not as a missing variable.
            val keystoreBase64 = System.getenv("RELEASE_KEYSTORE_BASE64")?.takeIf { it.isNotBlank() }
            if (signingPropFile.exists()) {
                val signingProp = Properties().apply { signingPropFile.inputStream().use(::load) }
                storeFile = file(signingProp.getProperty("release.storeFile"))
                storePassword = signingProp.getProperty("release.storePassword")
                keyAlias = signingProp.getProperty("release.keyAlias")
                keyPassword = signingProp.getProperty("release.keyPassword")
            } else if (keystoreBase64 != null) {
                storeFile = decodeKeyStore(keystoreBase64)
                storePassword = System.getenv("RELEASE_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("RELEASE_KEYSTORE_KEY_ALIAS")
                keyPassword = System.getenv("RELEASE_KEYSTORE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

            val releaseSigning = signingConfigs.getByName("release")
            if (releaseSigning.storeFile != null) {
                signingConfig = releaseSigning
            }
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    lint {
        abortOnError = false
    }
}

dependencies {
    implementation(libs.androidx.activity)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.core)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.material)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.ui)
    implementation(libs.eventbus)
    implementation(libs.glide)
    implementation(libs.glide.okhttp3)
    annotationProcessor(libs.glide.compiler)
    implementation(libs.gson)
    implementation(libs.jsoup)
    implementation(libs.okhttp)
    implementation(libs.photoview)
    implementation(libs.androidx.room.runtime)
    annotationProcessor(libs.androidx.room.compiler)

    "playImplementation"(libs.objectbox.android)
    "playAnnotationProcessor"(libs.objectbox.processor)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.espresso.contrib)
    androidTestImplementation(libs.screengrab)
}

fun decodeKeyStore(base64: String): File {
    val keyStoreFile = File.createTempFile("tmp_ks_", ".jks")
    keyStoreFile.deleteOnExit()
    keyStoreFile.writeBytes(Base64.getDecoder().decode(base64))
    return keyStoreFile
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(listOf("-Xlint:deprecation", "-Xlint:unchecked"))
}
