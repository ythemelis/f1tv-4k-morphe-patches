package app.morphe.patches.f1tv

import app.morphe.patcher.Fingerprint

object QualitySelectorFingerprint : Fingerprint(
    name = "isVideoQualityEnabled",
    returnType = "Z"
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
