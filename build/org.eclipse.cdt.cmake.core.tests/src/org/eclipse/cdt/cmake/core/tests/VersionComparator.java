package org.eclipse.cdt.cmake.core.tests;

public class VersionComparator {

	public static int compareVersions(String ver1, String ver2) {
		String[] ver1array = ver1.split("\\.");
		String[] ver2array = ver2.split("\\.");

		int n = Math.min(ver1array.length, ver2array.length);

		for (int i = 0; i < n; i++) {
			int v1 = Integer.valueOf(ver1array[i]);
			int v2 = Integer.valueOf(ver2array[i]);
			int cmp = Integer.compare(v1, v2);
			if (cmp != 0) {
				return cmp;
			}
		}

		// Versions are equal up to common prefix. Longest version
		// wins.
		return Integer.compare(ver2array.length, ver1array.length);
	}

}
