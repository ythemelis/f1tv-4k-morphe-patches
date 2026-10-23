package app.morphe.patches.f1tv

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.FormFactor

object Constants {
    const val PACKAGE_NAME = "com.formulaone.production"

    val COMPATIBILITY_F1TV = Compatibility(
        name = "F1 TV (Android TV)",
        packageName = PACKAGE_NAME,
        apkFileType = ApkFileType.APKM,
        // Formula 1 Racing Red
        appIconColor = 0xE10600,
        formFactor = FormFactor.TV,
        targets = listOf(
            // Target any recent or future version
            AppTarget(
                version = null,
                isExperimental = true
            ),
            // Confirmed working versions
            AppTarget(
                version = "3.0.49.5"
            ),
            AppTarget(
                version = "3.0.49.4"
            )
        )
    )
}
