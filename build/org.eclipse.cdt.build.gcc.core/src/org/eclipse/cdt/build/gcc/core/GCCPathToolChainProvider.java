/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.build.gcc.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.build.core.IToolChain;
import org.eclipse.cdt.build.core.IToolChainManager;
import org.eclipse.cdt.build.core.IToolChainProvider;
import org.eclipse.cdt.build.core.IToolChainType;
import org.eclipse.cdt.build.gcc.core.internal.Activator;

/**
 * Finds gcc and clang on the path.
 */
public class GCCPathToolChainProvider implements IToolChainProvider {

	private static Pattern gccPattern = Pattern.compile("(.*-)?(gcc|g\\+\\+|clang|clang\\+\\+)(-[0-9].*)?"); //$NON-NLS-1$

	@Override
	public Collection<IToolChain> getToolChains() {
		IToolChainManager manager = Activator.getService(IToolChainManager.class);
		IToolChainType type = null;

		List<IToolChain> toolChains = new ArrayList<>();

		String path = null;
		for (Entry<String, String> entry : System.getenv().entrySet()) {
			if (entry.getKey().equalsIgnoreCase("PATH")) { //$NON-NLS-1$
				path = entry.getValue();
				break;
			}
		}

		if (path != null) {
			Map<String, List<String>> installs = new HashMap<>();

			for (String dirStr : path.split(File.pathSeparator)) {
				File dir = new File(dirStr);
				for (String file : dir.list()) {
					Matcher matcher = gccPattern.matcher(file);
					if (matcher.matches()) {
						String prefix = matcher.group(1);
						String suffix = matcher.group(3);
						String command = dirStr + File.separatorChar + file;
						String version = getVersion(command);
						if (version != null) {
							List<String> commands = installs.get(version);
							if (commands == null) {
								commands = new ArrayList<>();
								installs.put(version, commands);
							}
							commands.add(command);
						}
					}
				}
			}

			for (Entry<String, List<String>> entry : installs.entrySet()) {
				String version = entry.getKey();
				String searchStr;
				if (version.contains("LLVM")) {
					searchStr = "clang++";
				} else {
					searchStr = "g++";
				}

				for (String command : entry.getValue()) {
					if (command.contains(searchStr)) {
						if (type == null) {
							type = manager.getToolChainType(GCCToolChainType.ID);
						}
						Path commandPath = Paths.get(command);
						toolChains.add(
								new GCCToolChain(type, commandPath.getParent(), commandPath.getFileName().toString()));
						break;
					}
				}
			}
		}

		return toolChains;
	}

	private static Pattern versionPattern = Pattern.compile(".*(gcc|LLVM) version .*"); //$NON-NLS-1$
	private static Pattern targetPattern = Pattern.compile("Target: (.*)"); //$NON-NLS-1$

	private String getVersion(String command) {
		try {
			Process proc = new ProcessBuilder(new String[] { command, "-v" }).redirectErrorStream(true) //$NON-NLS-1$
					.start();
			String version = null;
			String target = null;
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
				for (String line = reader.readLine(); line != null; line = reader.readLine()) {
					Matcher versionMatcher = versionPattern.matcher(line);
					if (versionMatcher.matches()) {
						version = line.trim();
						continue;
					}
					Matcher targetMatcher = targetPattern.matcher(line);
					if (targetMatcher.matches()) {
						target = targetMatcher.group(1);
						continue;
					}
				}
			}
			if (version != null) {
				if (target != null) {
					return version + " " + target; // $NON-NLS-1$
				} else {
					return version;
				}
			} else {
				return null;
			}
		} catch (IOException e) {
			return null;
		}
	}

}
