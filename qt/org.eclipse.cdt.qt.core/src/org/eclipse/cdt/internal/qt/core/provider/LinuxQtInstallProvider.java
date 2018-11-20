/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.core.provider;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.internal.qt.core.QtInstall;
import org.eclipse.cdt.qt.core.IQtInstall;
import org.eclipse.cdt.qt.core.IQtInstallProvider;
import org.eclipse.core.runtime.Platform;

/**
 * Qt install provider that looks for qmake on /usr/bin
 */
public class LinuxQtInstallProvider implements IQtInstallProvider {

	@Override
	public Collection<IQtInstall> getInstalls() {
		if (Platform.getOS().equals(Platform.OS_LINUX)) {
			Path qmakePath = Paths.get("/usr/bin/qmake"); //$NON-NLS-1$
			if (Files.exists(qmakePath)) {
				QtInstall install = new QtInstall(qmakePath);
				install.setProperty(IToolChain.ATTR_PACKAGE, "system");
				return Arrays.asList(install);
			}
		}
		return Collections.emptyList();
	}

}
