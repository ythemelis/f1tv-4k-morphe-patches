group = "app.morphe.patches.f1tv"

patches {
    about {
        name = "F1TV 4K Patches"
        description = "Morphe patches for F1 TV (Android TV) to unlock 4K UHD streaming on all devices"
        source = "https://github.com/Alexvbp/f1tv-4k-patch"
        author = "Alexvbp & Morphe Community"
        contact = "na"
        website = "https://github.com/Alexvbp/f1tv-4k-patch"
        license = "GPL-3.0"
    }
}

// Separate configuration so gson is available at runtime for the
// generatePatchesList task but never bundled into the APK.
val patchListGeneratorClasspath = configurations.create("patchListGeneratorClasspath")

dependencies {
    compileOnly(libs.gson)
    patchListGeneratorClasspath(libs.gson)
}

tasks {
    register<JavaExec>("generatePatchesList") {
        description = "Build patch with patch list"

        dependsOn(build)

        classpath = sourceSets["main"].runtimeClasspath + patchListGeneratorClasspath
        mainClass.set("util.PatchListGeneratorKt")
    }

    // Used by gradle-semantic-release-plugin.
    publish {
        dependsOn("generatePatchesList")
    }
}
