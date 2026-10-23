package app.morphe.patches.f1tv

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.f1tv.Constants.COMPATIBILITY_F1TV

private const val DEVICE_SUPPORT_CLASS = "Lcom/avs/f1/ui/tiledmediaplayer/DeviceSupportImpl;"
private const val TRUE_TV_HELPER_CLASS = "Lcom/tiledmedia/clearvrview/TrueTVDisplaySizeHelper;"
private const val DECODER_CAPABILITY_CLASS = "Lcom/tiledmedia/clearvrdecoder/util/DecoderCapability;"

@Suppress("unused")
val unlock4kPlaybackPatch = bytecodePatch(
    name = "Unlock UHD / 4K Playback",
    description = "Enables 2160p (4K UHD) streaming by bypassing device whitelist checks, display resolution limits, and HDR capability gates.",
    default = true
) {
    compatibleWith(COMPATIBILITY_F1TV)

    execute {
        // 1. Bypass DeviceSupportImpl validators:
        // - validateTmSdkSupport (ClearVR secure HEVC 4K decoder check)
        // - validateLowRamDeviceSupport (ActivityManager.isLowRamDevice check)
        // - validateApiLevelSupport (Android API level check)
        // - validateIsUhdSupportedDevice (configPROD.json hardware whitelist)
        val deviceSupportDef = classDefByOrNull(DEVICE_SUPPORT_CLASS)
        deviceSupportDef?.let { classDef ->
            val mutableClass = mutableClassDefBy(classDef)
            val validators = setOf(
                "validateTmSdkSupport",
                "validateLowRamDeviceSupport",
                "validateApiLevelSupport",
                "validateIsUhdSupportedDevice"
            )
            mutableClass.methods
                .filter { it.name in validators }
                .forEach { method ->
                    method.addInstructions(
                        0,
                        """
                            new-instance v0, Lkotlin/Pair;
                            const/4 v1, 0x1
                            invoke-static {v1}, Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;
                            move-result-object v1
                            const/4 p1, 0x0
                            invoke-direct {v0, v1, p1}, Lkotlin/Pair;-><init>(Ljava/lang/Object;Ljava/lang/Object;)V
                            return-object v0
                        """
                    )
                }
        }

        // 2. Force 4K display size detection in TrueTVDisplaySizeHelper
        // ClearVR caps streaming resolution at ~1.5x the detected display size.
        // On Android TV (including NVIDIA Shield), the UI surface is 1080p, which
        // caps tiles at 2880x1620. Reporting a 3840x2160 panel lifts this cap to full 2160p.
        val displayHelperDef = classDefByOrNull(TRUE_TV_HELPER_CLASS)
        displayHelperDef?.let { classDef ->
            val mutableClass = mutableClassDefBy(classDef)
            mutableClass.methods
                .filter { it.name == "getDefaultDisplaySize" }
                .forEach { method ->
                    method.addInstructions(
                        0,
                        """
                            new-instance v0, Landroid/graphics/Point;
                            const/16 v1, 0xf00
                            const/16 v2, 0x870
                            invoke-direct {v0, v1, v2}, Landroid/graphics/Point;-><init>(II)V
                            sput-object v0, Lcom/tiledmedia/clearvrview/TrueTVDisplaySizeHelper;->trueDisplaySize:Landroid/graphics/Point;
                            return-object v0
                        """
                    )
                }
        }

        // 3. Spoof display HDR capability
        // F1's 2160p tier is HLG (HDR type 3). Devices/displays reporting only HDR10 (type 2)
        // or SDR make the core withhold 2160p upstream. Forcing doesDisplaySupport -> true
        // makes the core serve the 2160p tiles, which the system pipeline renders or downconverts.
        DisplayHdrSpoofFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """
        )

        // 4. Disable NVIDIA post-process workaround
        // On NVIDIA Shield devices, deviceNeedsNoPostProcessWorkaround sets "no-post-process"=1,
        // which may cause ClearVR to select lower quality tiles.
        QuirksWorkaroundFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """
        )

        // 5. Override DecoderCapability protobuf reporting
        // Devices without ClearVR quirk profiles report 0 tile slots/rows/cols,
        // downgrading resolution. Override to 16 slots, 5 rows, 5 cols.
        val decoderCapDef = classDefByOrNull(DECODER_CAPABILITY_CLASS)
        decoderCapDef?.let { classDef ->
            val mutableClass = mutableClassDefBy(classDef)
            mutableClass.methods
                .find { it.name == "getAsCoreProtobuf" }
                ?.addInstructions(
                    0,
                    """
                        const/16 v0, 0x10
                        iput v0, p0, Lcom/tiledmedia/clearvrdecoder/util/DecoderCapability;->maxNumberOfSecureHEVCSamples:I
                        const/4 v0, 0x5
                        iput v0, p0, Lcom/tiledmedia/clearvrdecoder/util/DecoderCapability;->maxTileRows:I
                        iput v0, p0, Lcom/tiledmedia/clearvrdecoder/util/DecoderCapability;->maxTileColumns:I
                    """
                )
        }
    }
}
