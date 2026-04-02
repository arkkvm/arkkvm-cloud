package com.ensurebit.ark.kvm.util;

public class VersionConverter {

    public static long versionToLong(String version) {
        if (version == null || version.trim().isEmpty()) {
            throw new IllegalArgumentException("Version string cannot be null or empty");
        }

        String[] parts = version.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid version format: " + version);
        }

        int major = Integer.parseInt(parts[0]);
        int minor = Integer.parseInt(parts[1]);
        int build = Integer.parseInt(parts[2]);


        return (long) major * 1_000_000_000L
                + (long) minor * 1_000_000L
                + (long) build;
    }
}
