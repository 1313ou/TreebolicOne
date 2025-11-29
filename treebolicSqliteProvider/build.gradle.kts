plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
}

private val vCompileSdk by lazy { rootProject.extra["compileSdk"] as Int }
private val vMinSdk by lazy { rootProject.extra["minSdk"] as Int }

android {

    namespace = "treebolic.provider.sqlite"

    compileSdk = vCompileSdk

    defaultConfig {
        minSdk = vMinSdk
        multiDexEnabled = true
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.treebolic.model)
    implementation(libs.treebolic.graph)
    implementation(libs.treebolic.loadbalancer)
    implementation(libs.treebolic.provider.sql.generic)

    implementation(project(":treebolicIface"))

    implementation(libs.annotation)

    implementation(libs.core.ktx)
    coreLibraryDesugaring(libs.desugar)
}
