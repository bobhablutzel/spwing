package com.hablutzel.spwing.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Objects;


@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceUtils {

    public static final String OS_NAME = SystemUtils.OS_NAME.replaceAll("\\s", "" );
    public static final String OS_VERSION = SystemUtils.OS_VERSION.replaceAll("[.]", "_" );

    public interface ResourcePathResolver {
        InputStream resolvePathToStream(Class<?> clazz, String name, String extension );
    }


    @Nullable
    private final ResourcePathResolver resourcePathResolver;

    public InputStream getPlatformResource( Class<?> clazz, String extension ) {
        return getPlatformResource(clazz, clazz.getSimpleName(), extension );
    }

    /**
     * Used to find a platform specific version of a resource file. For
     * example, this can be used to load a different frame description
     * file for Windows vs MacOS if appropriate. The routine will first
     * attempt to defer to a bean of type {@link ResourcePathResolver}
     * if such a bean exists and returns a non-null, non-blank answer. If
     * the bean does not exist or returns a null or blank, then the following
     * logic will be used.
     * <ul>
     *     <li>The operating system name and version will be loaded from {@link SystemUtils#OS_NAME}
     *     and {@link SystemUtils#OS_VERSION} respectively. Spaces will be dropped, and periods
     *     ('.', UTF8 0x2E) will be converted into underscopes ('_', UTF8 0x5F). For example,
     *     "Mac OS X" will become "MacOSX" and "13.4" will become "13_4".</li>
     *     <li>The routine will then attempt to find a resource with the base name
     *     appended with the converted name and version, plus the extension. For example, if looking for the
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
     * @param name The base name for the resource
     * @param extension The extension for the resource
     * @return The {@link InputStream} of the found resource, or null of no resource can be found.
     */
    public InputStream getPlatformResource( Class<?> clazz, String name, String extension ) {


        // If the application was configured with a resource path resolver bean, we will
        // first defer to that. It could return null. Note it returns a
        InputStream resolvedStream = Objects.nonNull(resourcePathResolver)
                ? resourcePathResolver.resolvePathToStream(clazz, name, extension)
                : null;

        if (Objects.nonNull(resolvedStream)) {
            return resolvedStream;
        } else {

            // First look for OS name + version. If found, use it.
            String baseNameVersion = String.format("%s_%s_%s.%s", name, OS_NAME, OS_VERSION, extension );
            InputStream baseNameVersionStream = clazz.getResourceAsStream(baseNameVersion);
            if (Objects.nonNull(baseNameVersionStream)) {
                return baseNameVersionStream;
            } else {
                // Not found, try just OS name
                String baseName =  String.format("%s_%s.%s", name, OS_NAME, extension );
                InputStream baseNameStream = clazz.getResourceAsStream(baseName);
                if (Objects.nonNull(baseNameStream)) {
                    return baseNameStream;
                } else {

                    // Nope. Last chance, either the base name only or null
                    String baseOnly = String.format( "%s.%s", name, extension );
                    return clazz.getResourceAsStream(baseOnly);
                }
            }
        }
    }
}
