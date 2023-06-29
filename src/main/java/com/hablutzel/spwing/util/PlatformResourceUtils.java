package com.hablutzel.spwing.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@RequiredArgsConstructor
@Slf4j
public class PlatformResourceUtils {


    public record OSInfo( String name, String major, String minor ) {}

    public static final String OS_NAME;
    public static final String OS_MAJOR_VERSION;

    public static final String OS_MINOR_VERSION;

    static {
        OSInfo osInfo = getOSInfo();
        OS_NAME = osInfo.name;
        OS_MAJOR_VERSION = osInfo.major;
        OS_MINOR_VERSION = osInfo.minor;
    }


    /**
     * Get better granularity information than the {@link SystemUtils}
     * class does.
     *
     * @return Information about the OS name and version
     */
    public static OSInfo getOSInfo() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return windowInfo();
        } else {
            return getDefaultOSInfo();
        }
    }

    private static OSInfo getDefaultOSInfo() {
        Pattern majorMinor = Pattern.compile("(\\d)+\\.(\\d+)");
        Matcher matcher = majorMinor.matcher(SystemUtils.OS_VERSION);
        if (matcher.find()) {
            return new OSInfo(SystemUtils.OS_NAME, matcher.group(1), matcher.group(2));
        } else {
            return new OSInfo(SystemUtils.OS_NAME, SystemUtils.OS_VERSION, "");
        }
    }


    /**
     * Windows doesn't do a great job of reporting
     * the version, especially for Windows 11. In order
     * to get the actual OS version, we use a command line
     * to get the version, and get the OS version from that.
     * @return The system info
     */
    private static OSInfo windowInfo() {
        try {
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec(new String[] { "cmd.exe", "/c", "ver" });
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                StringBuilder stringBuilder = new StringBuilder();
                String stdOutLine;
                while ((stdOutLine = bufferedReader.readLine()) != null) {
                    stringBuilder.append(stdOutLine);
                }
                Pattern versionPattern = Pattern.compile("\\[Version (\\d+)\\.\\d+\\.(\\d+).(\\d+)]");
                Matcher matcher = versionPattern.matcher(stringBuilder);
                if (matcher.find()) {
                    int minorVersion = Integer.parseInt(matcher.group(2));
                    String version = "10".equals(matcher.group(1)) && minorVersion > 22000
                            ? "11"
                            : matcher.group(1);
                    return new OSInfo(SystemUtils.OS_NAME, version, matcher.group(2));

                } else {
                    return getDefaultOSInfo();
                }
            } catch (IOException e) {
                return getDefaultOSInfo();
            }
        } catch (IOException ex) {
            return getDefaultOSInfo();
        }
    }

    public static String withPlatformAndFullVersion(String baseName ) {
        return String.format("%s_%s_%s_%s", baseName, OS_NAME, OS_MAJOR_VERSION, OS_MINOR_VERSION);
    }
    public static String withPlatformAndMajorVersion(String baseName ) {
        return String.format("%s_%s_%s", baseName, OS_NAME, OS_MAJOR_VERSION);
    }

    public static boolean hasMajorVersion() {
        return Objects.nonNull(OS_MAJOR_VERSION);
    }

    public static String withPlatformOnly(String baseName) {
        return String.format("%s_%s", baseName, OS_NAME);
    }


    public static List<String> platformNames(String baseName) {
        if (hasMajorVersion()) {
            return List.of( withPlatformAndFullVersion(baseName),
                    withPlatformAndMajorVersion(baseName),
                    withPlatformOnly(baseName));
        } else {
            return List.of( withPlatformAndFullVersion(baseName),
                    withPlatformOnly(baseName));
        }
    }


    public static List<String> platformAndBaseNames(String baseName) {
        if (hasMajorVersion()) {
            return List.of( withPlatformAndFullVersion(baseName),
                    withPlatformAndMajorVersion(baseName),
                    withPlatformOnly(baseName),
                    baseName);
        } else {
            return List.of( withPlatformAndFullVersion(baseName),
                    withPlatformOnly(baseName),
                    baseName);
        }
    }




    /**
     * Used to find a platform specific version of a resource file. For
     * example, this can be used to load a different frame description
     * file for Windows vs MacOS if appropriate.<br>
     * <ul>
     *     <li>The operating system baseName and version will be loaded from {@link SystemUtils#OS_NAME}
     *     and {@link SystemUtils#OS_VERSION} respectively. Spaces will be dropped, and periods
     *     ('.', UTF8 0x2E) will be converted into underscopes ('_', UTF8 0x5F). For example,
     *     "Mac OS X" will become "MacOSX" and "13.4" will become "13_4".</li>
     *     <li>The routine will then attempt to find a resource with the base baseName
     *     appended with the converted baseName and version, plus the extension. For example, if looking for the
     *     resource named "Hello World", extension "swvf" and operating system values as
     *     above, the resource "Hello World_MacOSX_13_4.swvf" will be searched for. If
     *     found, it will be used as the resource</li>
     *     <li>If that resource is not found, the resource without the version number will
     *     be looked for - in this example, "Hello World_MacOSX.swvf". Again, if found that
     *     resource will be used.</li>
     *     <lI>If neither of the OS specific files are found, the resource with no additional
     *     information will be looked for, in this example "Hello World.swvf". If found, that
     *     will be used.</lI>
     *     <li>If none of these resources are found, the routine will return null.</li>
     * </ul>>
     * @param clazz The class for the resource. {@link Class#getResourceAsStream(String)} will be
     *              used based on this class.
     * @param baseName The base name for the resource
     * @param extension The extension for the resource
     * @return The {@link InputStream} of the found resource, or null of no resource can be found.
     */
    public static InputStream getPlatformResource(@NonNull final Class<?> clazz,
                                                  @NonNull final String baseName,
                                                  @NonNull final String extension ) {
        return platformAndBaseNames(baseName).stream()
                .map(name -> String.format("%s.%s", name, extension))
                .map(clazz::getResourceAsStream)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }
}
