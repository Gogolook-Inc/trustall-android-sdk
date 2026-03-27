package com.gogolook.trustall

import org.gradle.api.artifacts.VersionCatalog

val VersionCatalog.compileSdk
    get(): Int = findVersion("compileSdk").get().toString().toInt()

val VersionCatalog.minSdk
    get(): Int = findVersion("minSdk").get().toString().toInt()

val VersionCatalog.targetSdk
    get(): Int = findVersion("targetSdk").get().toString().toInt()