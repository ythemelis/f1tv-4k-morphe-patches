package app.morphe.patches.f1tv

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.f1tv.Constants.COMPATIBILITY_F1TV

@Suppress("unused")
val dynamicColorspacePatch = bytecodePatch(
    name = "Dynamic HDR / Colorspace Routing",
    description = "Routes HLG HDR streams per device at runtime: native HLG for supported TVs, PQ reroute for NVIDIA Shield, and direct rendering for MediaTek/Amlogic TVs.",
    default = true
) {
    compatibleWith(COMPATIBILITY_F1TV)

    // Merge helper extension into the target APK
    extendWith("extensions/f1tv-extension.mpe")

    execute {
        // 1. Force EGLRenderTarget.getIsBt2020HlgExtensionSupported -> true
        // ClearVR's DeviceParameters queries this to report HDR types to F1TV backend.
        // If false, backend withholds every 2160p tier and caps SDR at 1620p.
        EglHdrExtensionFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """
        )

        // 2. Patch RenderTargetConfig.requireHLG() to query real hardware HLG support
        RenderTargetConfigHlgFingerprint.method.addInstructions(
            0,
            """
                invoke-static {p0}, Lapp/morphe/extension/f1tv/F1TvColorspaceHelper;->shouldRequireHlg(Ljava/lang/Object;)Z
                move-result v0
                return v0
            """
        )

        // 3. Patch RenderTargetConfig.require2020PQ() to dynamically reroute HLG to PQ
        // when the GPU lacks native HLG (e.g. NVIDIA Shield TV).
        RenderTargetConfigPqFingerprint.method.addInstructions(
            0,
            """
                invoke-static {p0}, Lapp/morphe/extension/f1tv/F1TvColorspaceHelper;->shouldRequire2020Pq(Ljava/lang/Object;)Z
                move-result v0
                return v0
            """
        )
    }
}

