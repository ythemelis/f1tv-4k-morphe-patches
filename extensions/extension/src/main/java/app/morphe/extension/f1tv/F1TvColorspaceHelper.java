package app.morphe.extension.f1tv;

import java.lang.reflect.Method;

@SuppressWarnings("unused")
public final class F1TvColorspaceHelper {
    private static boolean pqDetected = false;
    private static boolean pqSupported = false;

    private F1TvColorspaceHelper() {}

    /**
     * Checks if the EGL implementation on this device supports a given extension string
     * by querying ClearVR's internal EGLRenderTarget._getIsExtensionSupported.
     */
    public static boolean checkExtension(String name) {
        try {
            Class<?> clazz = Class.forName("com.tiledmedia.clearvrview.EGLRenderTarget");
            Method method = clazz.getDeclaredMethod("_getIsExtensionSupported", String.class);
            method.setAccessible(true);
            return Boolean.TRUE.equals(method.invoke(null, name));
        } catch (Throwable ignored) {
            return false;
        }
    }

    /**
     * Real probe for native EGL BT.2020 HLG support (EGL_EXT_gl_colorspace_bt2020_hlg).
     */
    public static boolean realHlgSupported() {
        return checkExtension("EGL_EXT_gl_colorspace_bt2020_hlg");
    }

    /**
     * Real probe for EGL BT.2020 PQ support (EGL_EXT_gl_colorspace_bt2020_pq).
     * Shields support PQ on Android 11+ without needing API 33+.
     */
    public static boolean realPqSupported() {
        if (!pqDetected) {
            pqDetected = true;
            pqSupported = checkExtension("EGL_EXT_gl_colorspace_bt2020_pq");
        }
        return pqSupported;
    }

    /**
     * True if the GL surface can render HLG content accurately (either natively or via PQ reroute).
     */
    public static boolean surfaceHlgRenderable() {
        return realHlgSupported() || realPqSupported();
    }

    /**
     * Decides whether to create a native HLG EGL surface.
     * Only true when the content requires HLG AND the GPU hardware actually supports it.
     */
    public static boolean shouldRequireHlg(boolean originalRequireHlg) {
        if (!originalRequireHlg) return false;
        return realHlgSupported();
    }

    /**
     * Decides whether to route through a PQ surface.
     * When content is HLG but GPU lacks HLG extension (like NVIDIA Shield), reroute to PQ.
     */
    public static boolean shouldRequire2020Pq(boolean originalRequirePq, boolean originalRequireHlg) {
        if (originalRequirePq) return true;
        if (!originalRequireHlg) return false;
        return !realHlgSupported();
    }
}
