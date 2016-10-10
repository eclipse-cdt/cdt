/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.launchbar.ui;

import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.launchbar.core.target.ILaunchTarget;

/**
 * The edit dialog for launch configurations created by the launch bar. Allows tabs to get the
 * target associated with the edit session.
 *
 * @since 2.1
 */
public interface ILaunchBarLaunchConfigDialog extends ILaunchConfigurationDialog {

	/**
	 * The target associated with the edit session, usually the active target when the session was
	 * started.
	 * 
	 * @return launch target
	 */
	ILaunchTarget getLaunchTarget();

}
