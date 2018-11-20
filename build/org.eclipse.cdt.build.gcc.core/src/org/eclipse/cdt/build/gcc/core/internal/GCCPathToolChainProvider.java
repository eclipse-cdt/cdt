/*******************************************************************************
 * Copyright (c) 2015, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.build.gcc.core.internal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.build.gcc.core.ClangToolChain;
import org.eclipse.cdt.build.gcc.core.GCCToolChain;
import org.eclipse.cdt.build.gcc.core.GCCToolChain.GCCInfo;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.cdt.core.build.IToolChainProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;

/**
 * Finds gcc and clang on the path.
 */
public class GCCPathToolChainProvider implements IToolChainProvider {

	public static final String ID = "org.eclipse.cdt.build.gcc.core.gccPathProvider"; //$NON-NLS-1$

	private static final Pattern gccPattern = Pattern.compile("gcc(\\.exe)?"); //$NON-NLS-1$
	private static final Pattern tupledGccPattern = Pattern.compile("(.*-)?gcc(\\.exe)?"); //$NON-NLS-1$
	private static final Pattern clangPattern = Pattern.compile("clang(\\.exe)?"); //$NON-NLS-1$

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void init(IToolChainManager manager) {
		String path = System.getenv("PATH"); //$NON-NLS-1$
		List<IToolChain> tupledList = new ArrayList<>();
		for (String dirStr : path.split(File.pathSeparator)) {
			File dir = new File(dirStr);
			if (dir.isDirectory()) {
				for (File file : dir.listFiles()) {
					if (file.isDirectory()) {
						continue;
					}
					Matcher matcher = gccPattern.matcher(file.getName());
					Matcher matcher2 = tupledGccPattern.matcher(file.getName());
					if (matcher.matches() || matcher2.matches()) {
						try {
							GCCInfo info = new GCCInfo(file.toString());
							if (info.target != null && info.version != null) {
								String[] tuple = info.target.split("-"); //$NON-NLS-1$
								if (tuple.length > 2) {
									GCCToolChain gcc = new GCCToolChain(this, file.toPath(), tuple[0], null);

									// OS
									switch (tuple[1]) {
									case "w64": //$NON-NLS-1$
										gcc.setProperty(IToolChain.ATTR_OS, Platform.OS_WIN32);
										break;
									case "linux": //$NON-NLS-1$
										gcc.setProperty(IToolChain.ATTR_OS, Platform.OS_LINUX);
										break;
									case "apple": //$NON-NLS-1$
										gcc.setProperty(IToolChain.ATTR_OS, Platform.OS_MACOSX);
										break;
									default:
										switch (tuple[2]) {
										case "linux":
											gcc.setProperty(IToolChain.ATTR_OS, Platform.OS_LINUX);
											break;
										case "elf":
											gcc.setProperty(IToolChain.ATTR_OS, tuple[1]);
											break;
										}
									}
									try {
										if (manager.getToolChain(gcc.getTypeId(), gcc.getId()) == null) {
											// Only add if another provider hasn't already added it
											if (matcher.matches()) {
												manager.addToolChain(gcc);
											} else {
												// add to tupled list to register later
												tupledList.add(gcc);
											}
										}
									} catch (CoreException e) {
										CCorePlugin.log(e.getStatus());
									}
								}
							}
						} catch (IOException e) {
							Activator.log(e);
						}
					} else {
						matcher = clangPattern.matcher(file.getName());
						if (matcher.matches()) {
							// TODO only support host clang for now, need to figure out multi
							ClangToolChain clang = new ClangToolChain(this, file.toPath(), Platform.getOSArch(), null);
							clang.setProperty(IToolChain.ATTR_OS, Platform.getOS());
							manager.addToolChain(clang);
						}
					}
				}
			}
		}
		// add tupled toolchains last so we won't find any local compiler as a tupled chain before the normal
		// defaults (e.g. don't want to find x86_64-redhat-linux-gcc before gcc if looking for a local toolchain)
		for (IToolChain t : tupledList) {
			manager.addToolChain(t);
		}
	}

}
