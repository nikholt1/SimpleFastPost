package org.example.simplefastpost.cli.OSService;

public class OSService {

    public enum OSType {
        WINDOWS,
        MAC,
        LINUX,
        OTHER
    }


    public static OSType getOperatingSystemType() {
        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("win")) {
            return OSType.WINDOWS;
        } else if (osName.contains("mac")) {
            return OSType.MAC;
        } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
            return OSType.LINUX;
        } else {
            return OSType.OTHER;
        }
    }


    public static boolean isWindows() {
        return getOperatingSystemType() == OSType.WINDOWS;
    }

    public static boolean isMac() {
        return getOperatingSystemType() == OSType.MAC;
    }

    public static boolean isLinux() {
        return getOperatingSystemType() == OSType.LINUX;
    }
}
