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
    name = "deviceNeedsNoPostProcessWorkaround",
    returnType = "Z",
    custom = { method, classDef ->
        method.implementation != null && classDef.type.contains("Quirks")
    }
)

object DisplayHdrSpoofFingerprint : Fingerprint(
    name = "doesDisplaySupport",
    returnType = "Z",
    custom = { method, classDef ->
        method.implementation != null && classDef.type.contains("DeviceParameters")
    }
)

object EglHdrExtensionFingerprint : Fingerprint(
    name = "getIsBt2020HlgExtensionSupported",
    returnType = "Z",
    custom = { method, classDef ->
        method.implementation != null && classDef.type.contains("EGLRenderTarget")
    }
)

object RenderTargetConfigHlgFingerprint : Fingerprint(
    name = "requireHLG",
    returnType = "Z",
    custom = { method, classDef ->
        method.implementation != null && classDef.type.contains("RenderTargetConfig")
    }
)

object RenderTargetConfigPqFingerprint : Fingerprint(
    name = "require2020PQ",
    returnType = "Z",
    custom = { method, classDef ->
        method.implementation != null && classDef.type.contains("RenderTargetConfig")
    }
)

object DecoderMediaFormatFingerprint : Fingerprint(
    name = "getMediaFormat",
    filters = listOf(
        string("height"),
        methodCall(
            definingClass = "Landroid/media/MediaFormat;",
            name = "setInteger"
        )
    ),
    custom = { method, classDef ->
        method.implementation != null && classDef.type.contains("VideoDecoderBridgeShared")
    }
)
