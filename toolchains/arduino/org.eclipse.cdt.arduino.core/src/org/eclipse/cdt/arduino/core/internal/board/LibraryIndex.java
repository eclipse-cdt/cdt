package org.eclipse.cdt.arduino.core.internal.board;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LibraryIndex {

	private List<ArduinoLibrary> libraries;

	private Map<String, ArduinoLibrary> latestLibs;
	// category name to library name
	private Map<String, Set<String>> categories;

	public void resolve() {
		latestLibs = new HashMap<>();

		for (ArduinoLibrary library : libraries) {
			String name = library.getName();
			ArduinoLibrary current = latestLibs.get(name);
			if (current != null) {
				if (ArduinoPackage.compareVersions(library.getVersion(), current.getVersion()) > 0) {
					latestLibs.put(name, library);
				}
			} else {
				latestLibs.put(name, library);
			}

			String category = library.getCategory();
			Set<String> categoryLibs = categories.get(category);
			if (categoryLibs == null) {
				categoryLibs = new HashSet<>();
				categories.put(category, categoryLibs);
			}
			categoryLibs.add(name);
		}
	}

	public ArduinoLibrary getLibrary(String name) {
		return latestLibs.get(name);
	}

	public Collection<String> getCategories() {
		return Collections.unmodifiableCollection(categories.keySet());
	}

	public Collection<ArduinoLibrary> getLibraries(String category) {
		Set<String> categoryLibs = categories.get(category);
		if (categoryLibs == null) {
			return new ArrayList<>(0);
		}

		List<ArduinoLibrary> libs = new ArrayList<>(categoryLibs.size());
		for (String name : categoryLibs) {
			libs.add(latestLibs.get(name));
		}
		return libs;
	}

}
