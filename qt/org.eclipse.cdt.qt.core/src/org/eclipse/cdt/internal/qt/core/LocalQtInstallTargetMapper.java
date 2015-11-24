/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.core;

import org.eclipse.cdt.build.core.IToolChain;
import org.eclipse.cdt.build.gcc.core.GCCToolChainType;
import org.eclipse.cdt.qt.core.IQtInstall;
import org.eclipse.cdt.qt.core.IQtInstallTargetMapper;
import org.eclipse.core.runtime.Platform;
import org.eclipse.launchbar.core.target.ILaunchTarget;

public class LocalQtInstallTargetMapper implements IQtInstallTargetMapper {

	@Override
	public boolean supported(IQtInstall qtInstall, ILaunchTarget launchTarget) {
		String os = Platform.getOS();
		String arch = Platform.getOSArch();

		switch (qtInstall.getSpec()) {
		case "macx-clang": //$NON-NLS-1$
			return Platform.OS_MACOSX.equals(os) && Platform.ARCH_X86_64.equals(arch);
		case "win32-g++": //$NON-NLS-1$
			return Platform.OS_WIN32.equals(os);
		default:
			return false;
		}
	}

	@Override
	public boolean supported(IQtInstall qtInstall, IToolChain toolChain) {
		if (toolChain.getType().equals(GCCToolChainType.ID)) {
			String spec = qtInstall.getSpec();
			return spec.endsWith("-clang") || spec.endsWith("-g++"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return false;
	}

}
