/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer
 *******************************************************************************/
package org.eclipse.cdt.launchbar.ui.internal;

import org.eclipse.cdt.launchbar.core.ILaunchBarManager;
import org.eclipse.cdt.launchbar.core.ILaunchConfigurationDescriptor;
import org.eclipse.cdt.launchbar.core.ILaunchTarget;
import org.eclipse.cdt.launchbar.ui.IHoverProvider;
import org.eclipse.jface.viewers.ILabelProvider;

public class LaunchBarUIManager {

	ILaunchBarManager manager;
	
	public LaunchBarUIManager(ILaunchBarManager manager) {
		this.manager = manager;
	}

	public ILaunchBarManager getManager() {
		return manager;
	}
	
	public ILabelProvider getLabelProvider(ILaunchConfigurationDescriptor configDesc) {
		// TODO
		return null;
	}

	public ILabelProvider getLabelProvider(ILaunchTarget target) {
		// TODO
		return null;
	}

	public String getEditCommand(ILaunchTarget target) {
		// TODO
		return null;
	}

	public IHoverProvider getHoverProvider(ILaunchTarget target) {
		// TODO
		return null;
	}

	public String getAddTargetCommand(ILaunchConfigurationDescriptor configDesc) {
		// TODO
		return null;
	}
}
