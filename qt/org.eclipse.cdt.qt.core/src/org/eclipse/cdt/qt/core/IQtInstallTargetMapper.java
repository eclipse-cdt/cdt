/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.qt.core;

import org.eclipse.cdt.build.core.IToolChain;
import org.eclipse.launchbar.core.target.ILaunchTarget;

public interface IQtInstallTargetMapper {

	/**
	 * Does the Qt install support the given target.
	 * 
	 * @param qtInstall
	 *            Qt install
	 * @param launchTarget
	 *            launch target
	 * @return does the Qt install support the target
	 */
	public boolean supported(IQtInstall qtInstall, ILaunchTarget launchTarget);

	/**
	 * Does the Qt install build using the given toolchain?
	 * 
	 * @param qtInstall
	 *            Qt install
	 * @param toolChain
	 *            ToolChain
	 * @return does the Qt install build with the toolchain
	 */
	public boolean supported(IQtInstall qtInstall, IToolChain toolChain);

}
