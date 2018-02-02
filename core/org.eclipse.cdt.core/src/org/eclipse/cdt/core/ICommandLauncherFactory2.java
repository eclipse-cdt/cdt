/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core;

import org.eclipse.cdt.core.build.ICBuildConfiguration;

/**
 * @since 6.5
 */
public interface ICommandLauncherFactory2 {

	public static final String CONTAINER_BUILD_ENABLED = "container.build.enabled"; //$NON-NLS-1$

	/**
	 * Get a Command Launcher for a build configuration descriptor
	 * @param cfg - ICBuildConfiguration to get command launcher for
	 * @return ICommandLauncher or null
	 */
	public default ICommandLauncher getCommandLauncher(ICBuildConfiguration cfg) {
		return null;
	}

}
