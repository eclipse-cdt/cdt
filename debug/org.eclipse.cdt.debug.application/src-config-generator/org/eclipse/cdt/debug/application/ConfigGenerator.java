/*******************************************************************************
 * Copyright (c) 2016 Ericsson.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.debug.application;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * A "script" that generates the config.ini used by the stand-alone debugger
 * script. This is done by parsing the debug.product file to find the plug-ins.
 */
public class ConfigGenerator {
	private static final Pattern PLUGIN_LINE_PATTERN = Pattern.compile("\\s*<plugin id=\"(\\S*)\".*"); //$NON-NLS-1$

	/** Plug-ins requiring a start level for things to work correctly */
	private static final Map<String, String> PLUGINS_WITH_START_LEVEL = new HashMap<>();
	/** Plug-ins that we don't want to load when using the script VS using the product */
	private static final Set<String> PLUGINS_NOT_IN_SCRIPT_VERSION = new HashSet<>();

	static {
		PLUGINS_WITH_START_LEVEL.put("org.apache.felix.scr", "@1\\:start"); //$NON-NLS-1$ //$NON-NLS-2$
		PLUGINS_WITH_START_LEVEL.put("org.eclipse.equinox.common", "@2\\:start"); //$NON-NLS-1$ //$NON-NLS-2$
		PLUGINS_WITH_START_LEVEL.put("org.eclipse.core.runtime", "@start"); //$NON-NLS-1$ //$NON-NLS-2$
		// We don't want the user to do "Check for updates", etc.
		PLUGINS_NOT_IN_SCRIPT_VERSION.add("org.eclipse.update.configurator"); //$NON-NLS-1$
		PLUGINS_NOT_IN_SCRIPT_VERSION.add("org.eclipse.equinox.p2.ui"); //$NON-NLS-1$
		PLUGINS_NOT_IN_SCRIPT_VERSION.add("org.eclipse.equinox.p2.ui.sdk"); //$NON-NLS-1$
	}

	public static void main(String[] args) {
		if (args.length < 4) {
			printUsage();
			System.exit(1);
		}

		String productFilePath = args[1];
		List<String> pluginList = parsePluginList(productFilePath);
		if (pluginList.isEmpty()) {
			System.err.println("No plugins. Something must have gone wrong."); //$NON-NLS-1$
			System.exit(1);
		}

		Path configOutputPath = Paths.get(args[3]);
		generateConfigIni(pluginList, configOutputPath);
		System.exit(0);
	}

	private static void generateConfigIni(List<String> pluginList, Path configOutputPath) {
		try (FileWriter r = new FileWriter(configOutputPath.toFile())) {
			r.write("osgi.install.area=file\\:$eclipse.home$\n"); //$NON-NLS-1$
			r.write("osgi.framework=file\\:$eclipse.home$/plugins/$osgi.jar$\n"); //$NON-NLS-1$
			r.write("osgi.bundles="); //$NON-NLS-1$

			// Write all plug-in names
			for (int i = 0; i < pluginList.size(); i++) {
				String pluginName = pluginList.get(i);
				if (PLUGINS_NOT_IN_SCRIPT_VERSION.contains(pluginName)) {
					continue;
				}

				r.write(pluginName);

				// Add start level if necessary
				if (PLUGINS_WITH_START_LEVEL.containsKey(pluginName)) {
					r.write(PLUGINS_WITH_START_LEVEL.get(pluginName));
				}

				r.write(',');
			}
			r.write('\n');
			r.write("osgi.configuration.cascaded=false\n"); //$NON-NLS-1$
			r.write("osgi.bundles.defaultStartLevel=4\n"); //$NON-NLS-1$
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private static List<String> parsePluginList(String productPath) {
		List<String> pluginList = new ArrayList<>();
		try (Stream<String> stream = Files.lines(Paths.get(productPath))) {
			stream.forEach((line) -> {
				Matcher m = PLUGIN_LINE_PATTERN.matcher(line);
				if (m.matches()) {
					pluginList.add(m.group(1));
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return pluginList;
	}

	private static void printUsage() {
		System.err.println("Usage:"); //$NON-NLS-1$
		System.err.println("  ConfigGenerator -product /path/to/foo.product -out /path/to/config.ini"); //$NON-NLS-1$
	}
}
