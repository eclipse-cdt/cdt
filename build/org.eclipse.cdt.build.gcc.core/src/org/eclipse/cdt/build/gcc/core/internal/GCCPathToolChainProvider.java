/*******************************************************************************
 * Copyright (c) 2015, 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.build.gcc.core.internal;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.build.gcc.core.GCCToolChain;
import org.eclipse.cdt.build.gcc.core.GCCToolChain.GCCInfo;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.cdt.core.build.IToolChainProvider;
import org.eclipse.core.runtime.Platform;

/**
 * Finds gcc and clang on the path.
 */
public class GCCPathToolChainProvider implements IToolChainProvider {

	public static final String ID = "org.eclipse.cdt.build.gcc.core.gccPathProvider"; //$NON-NLS-1$

	private static final Pattern gccPattern = Pattern.compile("(.*-)?((gcc|clang)(\\.exe)?)"); //$NON-NLS-1$

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void init(IToolChainManager manager) {
		String path = System.getenv("PATH"); //$NON-NLS-1$
		for (String dirStr : path.split(File.pathSeparator)) {
			File dir = new File(dirStr);
			if (dir.isDirectory()) {
				for (File file : dir.listFiles()) {
					Matcher matcher = gccPattern.matcher(file.getName());
					if (matcher.matches()) {
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
									}
									manager.addToolChain(gcc);
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
