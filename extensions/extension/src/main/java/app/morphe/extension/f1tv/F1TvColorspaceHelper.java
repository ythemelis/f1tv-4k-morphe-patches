package app.morphe.extension.f1tv;

import java.lang.reflect.Field;
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

    /**
     * Overload for RenderTargetConfig instances: reads _requireHLG safely via reflection.
     */
    public static boolean shouldRequireHlg(Object renderTargetConfig) {
        if (renderTargetConfig == null) return false;
        try {
            Field field = renderTargetConfig.getClass().getDeclaredField("_requireHLG");
            field.setAccessible(true);
            return shouldRequireHlg(field.getBoolean(renderTargetConfig));
        } catch (Throwable ignored) {
            return false;
        }
    }

    /**
     * Overload for RenderTargetConfig instances: reads _require2020PQ and _requireHLG safely via reflection.
     */
    public static boolean shouldRequire2020Pq(Object renderTargetConfig) {
        if (renderTargetConfig == null) return false;
        try {
            Class<?> clazz = renderTargetConfig.getClass();
            Field fPq = clazz.getDeclaredField("_require2020PQ");
            fPq.setAccessible(true);
            boolean origPq = fPq.getBoolean(renderTargetConfig);

            Field fHlg = clazz.getDeclaredField("_requireHLG");
            fHlg.setAccessible(true);
            boolean origHlg = fHlg.getBoolean(renderTargetConfig);

            return shouldRequire2020Pq(origPq, origHlg);
        } catch (Throwable ignored) {
            return false;
        }
    }
}
