package app.morphe.patches.f1tv

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string

object QualitySelectorFingerprint : Fingerprint(
    name = "isVideoQualityEnabled",
    returnType = "Z",
    custom = { method, classDef ->
        method.implementation != null && classDef.type.contains("DiagnosticsPreferenceManager")
    }
)

object QuirksWorkaroundFingerprint : Fingerprint(
    definingClass = "Lcom/tiledmedia/clearvrdecoder/Quirks;",
    name = "deviceNeedsNoPostProcessWorkaround",
    returnType = "Z"
)

object DisplayHdrSpoofFingerprint : Fingerprint(
    definingClass = "Lcom/tiledmedia/clearvrview/DeviceParameters;",
    name = "doesDisplaySupport",
    returnType = "Z"
)

object EglHdrExtensionFingerprint : Fingerprint(
    definingClass = "Lcom/tiledmedia/clearvrview/EGLRenderTarget;",
    name = "getIsBt2020HlgExtensionSupported",
    returnType = "Z"
)

object RenderTargetConfigHlgFingerprint : Fingerprint(
    definingClass = "Lcom/tiledmedia/clearvrview/RenderTargetConfig;",
    name = "requireHLG",
    returnType = "Z"
)

object RenderTargetConfigPqFingerprint : Fingerprint(
    definingClass = "Lcom/tiledmedia/clearvrview/RenderTargetConfig;",
    name = "require2020PQ",
    returnType = "Z"
)

object DecoderMediaFormatFingerprint : Fingerprint(
    definingClass = "Lcom/tiledmedia/clearvrdecoder/video/VideoDecoderBridgeShared;",
    name = "getMediaFormat",
    filters = listOf(
        string("height"),
        methodCall(
            definingClass = "Landroid/media/MediaFormat;",
            name = "setInteger"
        )
    )
)
