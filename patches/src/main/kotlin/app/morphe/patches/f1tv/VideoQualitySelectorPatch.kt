package app.morphe.patches.f1tv

import app.morphe.patcher.extensions.InstructionExtensions.returnEarly
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.f1tv.Constants.COMPATIBILITY_F1TV

@Suppress("unused")
val videoQualitySelectorPatch = bytecodePatch(
    name = "Enable Video Quality Selector",
    description = "Enables the in-app manual video quality (resolution / bitrate) selector in the playback options menu.",
    default = true
) {
    compatibleWith(COMPATIBILITY_F1TV)

    execute {
        // Force isVideoQualityEnabled() -> true in DiagnosticsPreferenceManagerImpl
        QualitySelectorFingerprint.methodOrNull?.returnEarly(true)
    }
}
