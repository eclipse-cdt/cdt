/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.core.build;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Platform;

public class QtInstallManager {

	public static final QtInstallManager instance = new QtInstallManager();

	private List<QtInstall> installs;

	private static boolean isWin = Platform.getOS().equals(Platform.OS_WIN32);
	
	public List<QtInstall> getInstalls() {
		if (installs == null) {
			installs = new ArrayList<>();
			// TODO hack to get going
			File qtDir = new File(System.getProperty("user.home"), "Qt/5.5");
			if (!qtDir.isDirectory() && isWin) {
				qtDir = new File("C:/Qt/5.5");
			}
			if (qtDir.isDirectory()) {
				for (File dir : qtDir.listFiles()) {
					Path qmakePath = dir.toPath().resolve(isWin ? "bin/qmake.exe" : "bin/qmake");
					if (qmakePath.toFile().canExecute()) {
						installs.add(new QtInstall(qmakePath));
					}
				}
			}
		}

		return installs;
	}

}
