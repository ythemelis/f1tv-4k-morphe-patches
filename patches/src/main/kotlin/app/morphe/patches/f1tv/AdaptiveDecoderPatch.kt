package app.morphe.patches.f1tv

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.f1tv.Constants.COMPATIBILITY_F1TV

@Suppress("unused")
val adaptiveDecoderPatch = bytecodePatch(
    name = "Adaptive Playback Decoder Hints",
    description = "Sets max-width and max-height hints on the secure HEVC decoder to prevent TM4014 allocation crashes on MediaTek / Amlogic chipsets during resolution ramp-up.",
    default = true
) {
    compatibleWith(COMPATIBILITY_F1TV)

    execute {
        // Find the MediaFormat.setInteger("height", ...) call in VideoDecoderBridgeShared.getMediaFormat
        val lastMatch = DecoderMediaFormatFingerprint.instructionMatches.lastOrNull()
        val insertIndex = if (lastMatch != null) lastMatch.index + 1 else 0

        DecoderMediaFormatFingerprint.method.addInstructions(
            insertIndex,
            """
                iget-object v1, p0, Lcom/tiledmedia/clearvrdecoder/video/VideoDecoderBridgeShared${'$'}Configuration;->decoderCapabilities:Lcom/tiledmedia/clearvrdecoder/util/DecoderCapability;
                if-eqz v1, :uhd_maxres_skip
                invoke-virtual {v1}, Lcom/tiledmedia/clearvrdecoder/util/DecoderCapability;->getMaxSupportedAndValidResolution()Lcom/tiledmedia/clearvrdecoder/util/Resolution;
                move-result-object v1
                if-eqz v1, :uhd_maxres_skip
                invoke-virtual {v1}, Lcom/tiledmedia/clearvrdecoder/util/Resolution;->getWidth()I
                move-result v2
                if-lez v2, :uhd_maxres_skip
                const-string p1, "max-width"
                invoke-virtual {v0, p1, v2}, Landroid/media/MediaFormat;->setInteger(Ljava/lang/String;I)V
                invoke-virtual {v1}, Lcom/tiledmedia/clearvrdecoder/util/Resolution;->getHeight()I
                move-result v2
                const-string p1, "max-height"
                invoke-virtual {v0, p1, v2}, Landroid/media/MediaFormat;->setInteger(Ljava/lang/String;I)V
                :uhd_maxres_skip
            """
        )
    }
}
