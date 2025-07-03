plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
}

private val vCompileSdk by lazy { rootProject.extra["compileSdk"] as Int }
private val vMinSdk by lazy { rootProject.extra["minSdk"] as Int }

android {

    namespace "treebolic.provider.owl"

    compileSdk = vCompileSdk

    defaultConfig {
        minSdkVersion rootProject.ext.minSdkVersion
        targetSdkVersion rootProject.ext.targetSdkVersion
        multiDexEnabled true
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation "io.github.treebolic:treebolic-model:${treebolicVersion}"
    implementation "io.github.treebolic:treebolic-mutable:${treebolicVersion}"
    implementation "io.github.treebolic:provider-owl-owlapi:${treebolicVersion}"

    implementation project(":treebolicGlue")

    implementation "androidx.annotation:annotation:1.9.1"

    coreLibraryDesugaring(libs.desugar)
}
