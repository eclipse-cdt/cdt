/*******************************************************************************
 * Copyright (c) 2015, 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.build.gcc.core.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.build.gcc.core.GCCToolChain;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.cdt.core.build.IToolChainProvider;
import org.eclipse.core.runtime.Platform;

/**
 * Finds gcc and clang on the path.
 */
public class GCCPathToolChainProvider implements IToolChainProvider {

	private static final String ID = "org.eclipse.cdt.build.gcc.core.gccPathProvider"; //$NON-NLS-1$

	private static final Pattern gccPattern = Pattern.compile("(.*-)?(gcc|g\\+\\+|clang|clang\\+\\+)"); //$NON-NLS-1$
	private static final Pattern versionPattern = Pattern.compile(".*(gcc|LLVM) version .*"); //$NON-NLS-1$
	private static final Pattern targetPattern = Pattern.compile("Target: (.*)"); //$NON-NLS-1$

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void init(IToolChainManager manager) {
		Set<String> names = new HashSet<>();

		String path = System.getenv("PATH"); //$NON-NLS-1$
		for (String dirStr : path.split(File.pathSeparator)) {
			File dir = new File(dirStr);
			if (dir.isDirectory()) {
				for (String file : dir.list()) {
					Matcher matcher = gccPattern.matcher(file);
					if (matcher.matches()) {
						String prefix = matcher.group(1);
						String command = dirStr + File.separatorChar + file;
						try {
							Process proc = new ProcessBuilder(new String[] { command, "-v" }).redirectErrorStream(true) //$NON-NLS-1$
									.start();
							String version = null;
							String target = null;
							try (BufferedReader reader = new BufferedReader(
									new InputStreamReader(proc.getInputStream()))) {
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
							if (target != null && version != null) {
								String name = target + " - " + version; //$NON-NLS-1$
								if (!names.contains(name)) {
									names.add(name);
									GCCToolChain toolChain = new GCCToolChain(this, target, version,
											new Path[] { dir.toPath() }, prefix);
									String[] tuple = target.split("-"); //$NON-NLS-1$
									if (tuple.length > 2) {
										// Arch
										if ("x86_64".equals(tuple[0])) {
											toolChain.setProperty(IToolChain.ATTR_ARCH, tuple[0]);
										} else {
											toolChain.setProperty(IToolChain.ATTR_ARCH, "x86"); // default
										}
										
										// OS
										switch (tuple[1]) {
										case "w64":
											toolChain.setProperty(IToolChain.ATTR_OS, Platform.OS_WIN32);
											break;
										case "linux":
											toolChain.setProperty(IToolChain.ATTR_OS, Platform.OS_LINUX);
											break;
										case "apple":
											toolChain.setProperty(IToolChain.ATTR_OS, Platform.OS_MACOSX);
											break;
										}
									}
									toolChain.setProperty(IToolChain.ATTR_PACKAGE, "system");
									manager.addToolChain(toolChain);
								}
							}
						} catch (IOException e) {
							Activator.log(e);
						}
					}
				}
			}
		}
	}

}
