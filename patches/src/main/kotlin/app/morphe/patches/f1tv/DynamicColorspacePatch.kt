package app.morphe.patches.f1tv

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.extensions.InstructionExtensions.returnEarly
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.f1tv.Constants.COMPATIBILITY_F1TV

private const val EXTENSION_CLASS = "Lapp/morphe/extension/f1tv/F1TvColorspaceHelper;"
private const val RENDER_TARGET_CONFIG_CLASS = "Lcom/tiledmedia/clearvrview/RenderTargetConfig;"
private const val TM_VIEW_MODEL_CLASS = "Lcom/tiledmedia/clearvrview/TMViewModel;"

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
        EglHdrExtensionFingerprint.methodOrNull?.returnEarly(true)

        // 2. Patch RenderTargetConfig.requireHLG() to query real hardware HLG support
        RenderTargetConfigHlgFingerprint.methodOrNull?.let { method ->
            method.addInstructions(
                0,
                """
                    iget-boolean v0, p0, $RENDER_TARGET_CONFIG_CLASS->_requireHLG:Z
                    invoke-static {v0}, $EXTENSION_CLASS;->shouldRequireHlg(Z)Z
                    move-result v0
                    return v0
                """
            )
        }

        // 3. Patch RenderTargetConfig.require2020PQ() to dynamically reroute HLG to PQ
        // when the GPU lacks native HLG (e.g. NVIDIA Shield TV).
        RenderTargetConfigPqFingerprint.methodOrNull?.let { method ->
            method.addInstructions(
                0,
                """
                    iget-boolean v0, p0, $RENDER_TARGET_CONFIG_CLASS->_require2020PQ:Z
                    iget-boolean v1, p0, $RENDER_TARGET_CONFIG_CLASS->_requireHLG:Z
                    invoke-static {v0, v1}, $EXTENSION_CLASS;->shouldRequire2020Pq(ZZ)Z
                    move-result v0
                    return v0
                """
            )
        }

        // 4. Patch TMViewModel surface capability report (ClearVR SDK >= SP164)
        // Newer ClearVR SDKs report per-view surface capabilities (supportsHDRHlg).
        // Reroute it to surfaceHlgRenderable() so devices without any EGL HDR extension
        // are routed by ClearVR core to the direct decoder video path.
        val tmVmClassDef = classDefByOrNull(TM_VIEW_MODEL_CLASS)
        tmVmClassDef?.let { classDef ->
            val mutableClass = mutableClassDefBy(classDef)
            mutableClass.methods.forEach { method ->
                method.instructions.forEachIndexed { index, instruction ->
                    if (instruction.toString().contains("getIsBt2020HlgExtensionSupported")) {
                        method.replaceInstruction(
                            index,
                            "invoke-static {}, $EXTENSION_CLASS;->surfaceHlgRenderable()Z"
                        )
                    }
                }
            }
        }
    }
}
