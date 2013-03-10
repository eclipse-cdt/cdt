MinGW export fix patch is needed to get mingw path from MingwEnvironmentVariableSupplier in LlvmEnvironmentSupplier class.
 
Bug 321040 patch adds getLibraryPaths method to IOption and Option CDT classes and is used in LlvmPathUtil class.

Bug 318581 patch adds support for older CDT versions <~7.1.0

Stack overflow bug patch prevents a typical CDT 7.0.0 error message. This has been fixed in the CDT 7.0.1 version.

