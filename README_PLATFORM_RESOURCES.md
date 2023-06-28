<p style="text-align: center; font-size: 48px; font-style: italic">
Spwing
</p>

<div style="text-align: center;">
    <img src="https://github.com/bobhablutzel/spwing/blob/main/src/main/resources/com/hablutzel/spwing/component/Spwing.png?raw=true"
         alt="Spwing logo: A daffodil"
         height="100">
    <!-- Spwing logo attribution: CortoFrancese, CC BY-SA 4.0 <https://creativecommons.org/licenses/by-sa/4.0>, via Wikimedia Commons -->
</div>

<p style="text-align: center; font-size: 36px">
Guide to platform resources
</p>

Spwing allows resources to be retrieved with platform sensitivity. This is similar to
the Locale sensitivity for property files. With resource sensitivity, the resource name is modified
to look for a platform specific resource file if available, as documented below.

## Base names
Resources are considered to have *base names*. These names are the name of the file, without
the extension of one is provided
- Myresource → Basename: Myresource
- image.jpg → Basename: image, extension: jpg

When looking for a resource, the base name will be appended with platform specific values in order
- First the platform name and full version
- Then the platform name and major version
- Then the platform name alone
- Then the basename alone

The platform name and version are obtained from the Apache Commons Lang3 module SystemUtils.OS_NAME
and SystemUtils.OS_VERSION respectively. The major version will be derived from the OS_VERSION by 
taking the first part of the version number: e.g. 13 from 13.4. The platform name and version will be
separated from the base name and each other with underscore characters ```(_)```.  If an extension is 
provided, it will be added after the base name thus modified. In most cases, if you provide
the resource name with extension, Swping will break the name and apply the logic above.

Examples:

Under MacOS version 13.4, a platform resource request for "myView.svwf" will search for

- myView_MacOS_13_4.svwf
- myView_MacOS_13.svwf
- myView_MacOS_13.svwf
- myView.svwf

Note that platform sensitivity is separate from locale sensitivity. Spwing assumes that 
string localization will be handled through standard property file techniques, not through
platform sensitivity.




