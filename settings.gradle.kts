/*
 * Copyright (c) 2022. Bernard Bou <1313ou@gmail.com>
 */

pluginManagement {
    repositories {
        gradlePluginPortal()
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenLocal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenLocal()
        mavenCentral()
    }
}

// CORE

include(":treebolicGlue")
project(":treebolicGlue").projectDir = File("../TreebolicLib/treebolicGlue/")

include(":treebolicIface")
project(":treebolicIface").projectDir = File("../TreebolicLib/treebolicIface/")

// LIBS

include(":commonLib")
project(":commonLib").projectDir = File("../TreebolicSupportLibs/commonLib/")

include(":searchLib")
project(":searchLib").projectDir = File("../TreebolicSupportLibs/searchLib/")

include(":wheelLib")
project(":wheelLib").projectDir = File("../TreebolicSupportLibs/wheelLib/")

include(":colorLib")
project(":colorLib").projectDir = File("../TreebolicSupportLibs/colorLib/")

include(":guideLib")
project(":guideLib").projectDir = File("../TreebolicSupportLibs/guideLib/")

include(":downloadLib")
project(":downloadLib").projectDir = File("../TreebolicSupportLibs/downloadLib/")

include(":preferenceLib")
project(":preferenceLib").projectDir = File("../TreebolicSupportLibs/preferenceLib/")

include(":fileChooserLib")
project(":fileChooserLib").projectDir = File("../TreebolicSupportLibs/fileChooserLib/")

include(":storageLib")
project(":storageLib").projectDir = File("../TreebolicSupportLibs/storageLib/")

include(":rateLib")
project(":rateLib").projectDir = File("../TreebolicSponsorLibs/rateLib/")

include(":othersLib")
project(":othersLib").projectDir = File("../TreebolicSponsorLibs/othersLib/")

include(":donateLib")
project(":donateLib").projectDir = File("../TreebolicSponsorLibs/donateLib/")

// STUB

include(":treebolicOwlProvider")
include(":treebolicSqliteProvider")
include(":treebolicOneXml")
include(":treebolicOneSql")
include(":treebolicOneOwl")

// APP

include(":treebolicXmlTemplate")
include(":treebolicSqlTemplate")
include(":treebolicOwlTemplate")
