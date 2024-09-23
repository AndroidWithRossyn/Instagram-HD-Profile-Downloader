plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.googleDevtoolsKsp)
}

android {
    namespace = "com.instagram.hdprofile.downloader"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.instagram.hdprofile.downloader"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"  //(Major.Minor.Patch)
//      versionName = "0.dev.testing"
        setProperty("archivesBaseName", "hdprofile-$versionName")
        vectorDrawables.useSupportLibrary = true
        renderscriptTargetApi = 24
        renderscriptSupportModeEnabled = true
        multiDexEnabled = true
        resourceConfigurations += setOf()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    packaging {
        jniLibs.useLegacyPackaging = false
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }

    allprojects {
        gradle.projectsEvaluated {
            tasks.withType<JavaCompile> {
                options.compilerArgs.add("-Xlint:unchecked")
            }
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.androidx.fragment.ktx)

    implementation(libs.browser)

    implementation(libs.sdp.android)
    implementation(libs.ssp.android)

    implementation(libs.androidx.security.crypto)
    implementation(libs.androidx.multidex)

    implementation(libs.toolargetool)
    implementation(libs.androidx.swiperefreshlayout)
    // gson
    implementation(libs.gson)
    // retrofit
    implementation(libs.adapter.rxjava3)
    implementation(libs.converter.gson)
    implementation(libs.retrofit)
    //http clint
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    //    rx java
    implementation(libs.rxandroid)
    implementation(libs.rxjava)

    //image with glide
    implementation(libs.glide)
//    ksp(libs.glide)
    annotationProcessor(libs.compiler)
}