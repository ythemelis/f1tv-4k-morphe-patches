# F1TV 4K Morphe Patches 🏎️💨

> A native [Morphe](https://github.com/MorpheApp) patch bundle for **F1 TV (Android TV)** (`com.formulaone.production`), bringing the 4K UHD / HDR unlocking logic from [Alexvbp/f1tv-4k-patch](https://github.com/Alexvbp/f1tv-4k-patch) into the modern Morphe ecosystem.

---

## 🎯 Overview

Officially, F1 TV restricts 4K/UHD streaming to specific certified hardware profiles, leaving many powerful Android TV devices (such as NVIDIA Shield TV Pro, Chromecast with Google TV 4K, and high-end Android/Google TVs) limited to 1080p or 1620p SDR.

This repository packages the complete set of smali and bytecode patches from `Alexvbp/f1tv-4k-patch` into a modular **Morphe Patch Package (`.mpp`)**. 

Using Morphe, you can patch F1 TV directly on your device via **Morphe Manager** or on your computer using **Morphe CLI**, without needing custom bash scripts, Apktool, or signing pipelines.

---

## 🧩 Included Patches

### 1. `Unlock UHD / 4K Playback` (`unlock4kPlaybackPatch`)
* **Device Validator Bypass**: Overrides all 4 `DeviceSupportImpl` capability checks (`validateTmSdkSupport`, `validateLowRamDeviceSupport`, `validateApiLevelSupport`, `validateIsUhdSupportedDevice`) to always report `Pair(true, null)`.
* **4K Display Size Force**: Overrides `TrueTVDisplaySizeHelper.getDefaultDisplaySize()` to report a `3840x2160` panel. This eliminates TiledMedia's 1.5× display-size cap that normally restricts 1080p UI surfaces to 1620p streams.
* **Display HDR Capability Spoof**: Forces `DeviceParameters.doesDisplaySupport()` to `true`. Because F1TV's 2160p tier is delivered strictly as HLG HDR, spoofing HDR capability ensures the backend manifest offers the 2160p tier to HDR10-only panels (e.g., NVIDIA Shield).
* **NVIDIA Workaround Bypass**: Sets `Quirks.deviceNeedsNoPostProcessWorkaround()` to `false`, preventing ClearVR from degrading tile tiers on Tegra hardware.
* **Decoder Capability Spoof**: Sets `maxNumberOfSecureHEVCSamples = 16`, `maxTileRows = 5`, and `maxTileColumns = 5` in `DecoderCapability.getAsCoreProtobuf()`.

### 2. `Dynamic HDR / Colorspace Routing` (`dynamicColorspacePatch`)
* **EGL HDR Backend Advertise**: Advertises `getIsBt2020HlgExtensionSupported() -> true` to the F1TV server.
* **Per-Device Runtime Rerouting** (via `F1TvColorspaceHelper` extension):
  * **Native HLG**: Where GPU hardware has `EGL_EXT_gl_colorspace_bt2020_hlg` (Homatics, Mi Box, most native HLG TVs), renders with untouched HDR colors.
  * **HLG → PQ Reroute**: Where only `EGL_EXT_gl_colorspace_bt2020_pq` exists (NVIDIA Shield), reroutes HLG into PQ so the surface succeeds without black crush.
  * **Direct Video Decoder Path**: Devices lacking both EGL extensions (MediaTek / Amlogic TVs) route to the TV's direct hardware video decoder pipeline.

### 3. `Enable Video Quality Selector` (`videoQualitySelectorPatch`)
* Forces `DiagnosticsPreferenceManagerImpl.isVideoQualityEnabled() -> true`, exposing the manual resolution/bitrate quality picker in the in-app player settings.

### 4. `Adaptive Playback Decoder Hints` (`adaptiveDecoderPatch`)
* Injects `max-width` and `max-height` into `MediaFormat` inside `VideoDecoderBridgeShared.getMediaFormat()`. Pre-reserves decoder buffer allocations up to 4K up front to avoid mid-stream re-allocation failures (`TM4014` error) on MediaTek and Amlogic decoders.

---

## 🚀 How to Use

### Method 1: Morphe Manager (Recommended)

1. **Install Morphe Manager**:
   Install the latest [Morphe Manager](https://github.com/MorpheApp/morphe-manager) on your Android phone or Android TV.
2. **Add Custom Patch Source**:
   * In Morphe Manager, navigate to **Settings > Sources > Patches**.
   * Add this repository or load the downloaded `patches-*.mpp` bundle.
3. **Select F1 TV**:
   * Download the official F1 TV Android TV `.apkm` bundle (e.g. version `3.0.49.5` from APKMirror or Google Play).
   * Note: Ensure you choose the **Android TV** version (`com.formulaone.production`), **arm64-v8a** split.
4. **Patch & Install**:
   * Tap **Patch**, select the patches you want (all 4 are enabled by default), and let Morphe build your patched APK.
   * Install the generated APK on your Android TV using **SAI (Split APKs Installer)**, **Send Files to TV**, or **ADB**.

---

### Method 2: Morphe CLI (PC / Mac / Linux)

If you prefer patching from a terminal on your computer:

```bash
# 1. Download morphe-cli and the f1tv mpp bundle
java -jar morphe-cli.jar \
  -b f1tv-patches.mpp \
  -a com.formulaone.production.apkm \
  -o f1tv-4k-patched.apkm

# 2. Install to Android TV via ADB
adb uninstall com.formulaone.production
adb install-multiple base.apk config.arm64_v8a.apk config.en.apk config.xhdpi.apk
```

---

## 🛠️ Building from Source

### Prerequisites
* Java JDK 17 or 21+
* Git

### Build Instructions

```bash
# Clone the repository
git clone https://github.com/your-username/f1tv-4k-morphe-patches.git
cd f1tv-4k-morphe-patches

# Build the Morphe patch package (.mpp)
./gradlew :patches:buildAndroid
```

The compiled patch package will be generated at:
```
patches/build/libs/patches-<version>.mpp
```

---

## 📄 License & Credits

* Core patch logic and research courtesy of **Alexvbp** ([f1tv-4k-patch](https://github.com/Alexvbp/f1tv-4k-patch)).
* Patcher framework provided by **MorpheApp** ([morphe-patcher](https://github.com/MorpheApp/morphe-patcher)).
* Licensed under the GNU General Public License v3.0 (GPL-3.0).
