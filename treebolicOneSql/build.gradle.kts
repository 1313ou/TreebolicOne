plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
}

private val vCompileSdk by lazy { rootProject.extra["compileSdk"] as Int }
private val vMinSdk by lazy { rootProject.extra["minSdk"] as Int }

android {

    namespace = "org.treebolic.one.sql"

    compileSdk = vCompileSdk

    defaultConfig {
        minSdk = vMinSdk
        multiDexEnabled = true
    }

    buildFeatures {
        buildConfig = true
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
    implementation(libs.treebolic.mutable)
    implementation(libs.treebolic.view)
    implementation(libs.treebolic.provider.sql.generic)

    implementation(project(":treebolicIface"))
    implementation(project(":treebolicGlue"))
    implementation(project(":treebolicSqliteProvider"))
    implementation(project(":commonLib"))
    implementation(project(":storageLib"))
    implementation(project(":searchLib"))
    implementation(project(":preferenceLib"))
    implementation(project(":downloadLib"))
    implementation(project(":fileChooserLib"))
    implementation(project(":guideLib"))
    implementation(project(":rateLib"))
    implementation(project(":othersLib"))
    implementation(project(":donateLib"))

    implementation(libs.appcompat)
    implementation(libs.preference.ktx)
    implementation(libs.material)

    implementation(libs.core.ktx)
    coreLibraryDesugaring(libs.desugar)
}
