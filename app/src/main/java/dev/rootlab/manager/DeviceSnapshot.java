package dev.rootlab.manager;

import android.os.Build;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/** Read-only device metadata. This is NOT evidence of root or installability. */
public final class DeviceSnapshot {
    public final String manufacturer, model, androidRelease, fingerprint, kernel, abi;
    private DeviceSnapshot(String manufacturer, String model, String androidRelease,
                           String fingerprint, String kernel, String abi) {
        this.manufacturer = manufacturer;
        this.model = model;
        this.androidRelease = androidRelease;
        this.fingerprint = fingerprint;
        this.kernel = kernel;
        this.abi = abi;
    }
    public static DeviceSnapshot read() {
        String primaryAbi = Build.SUPPORTED_ABIS.length > 0 ? Build.SUPPORTED_ABIS[0] : "unknown";
        return new DeviceSnapshot(Build.MANUFACTURER, Build.MODEL, Build.VERSION.RELEASE,
                Build.FINGERPRINT, readLine("/proc/sys/kernel/osrelease"), primaryAbi);
    }
    private static String readLine(String path) {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String value = reader.readLine();
            return value == null ? "unknown" : value;
        } catch (IOException | SecurityException ignored) {
            return "unavailable";
        }
    }
    public String display() {
        return "Manufacturer: " + manufacturer + "\nModel: " + model +
                "\nAndroid: " + androidRelease + "\nABI: " + abi +
                "\nKernel: " + kernel + "\nBuild fingerprint: " + fingerprint;
    }
}
