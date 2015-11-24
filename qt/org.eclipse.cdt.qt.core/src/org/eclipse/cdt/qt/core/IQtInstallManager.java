/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.qt.core;

import java.util.Collection;

import org.eclipse.cdt.build.core.IToolChain;
import org.eclipse.launchbar.core.target.ILaunchTarget;

/**
 * The manager for Qt installs.
 * 
 * @noimplement
 */
public interface IQtInstallManager {

	public Collection<IQtInstall> getInstalls();

	public void addInstall(IQtInstall install);

	public IQtInstall getInstall(String name);

	public void removeInstall(IQtInstall install);

	public boolean supports(IQtInstall install, ILaunchTarget target);

	public boolean supports(IQtInstall install, IToolChain toolChain);

}
